package backend;

import java.util.List;
import java.util.stream.Collectors;
import peg.node.*;
import peg.node.Node;
import utils.Utils;

public class LpegBackend {
  private RuleKind currentRuleKind;

  public String convert(List<Rule> rules) {
    return String.format(
        """
		local lpeg = require "lpeglabel"
		local re = require "relabel"
		local P, S, V = lpeg.P, lpeg.S, lpeg.V
    local EMPTY = P''
    local neg = function (pat)
     return P(1) - pat
    end
		local regex = function (s)
			return re.compile(s)
		end
		local tk = function (s)
			return P(s) * V"WS"^0
		end
		local EOF = P(-1)

		local grammar = {
			\"start_\",
      start_ = V"WS"^0 * V\"%s\" * V"EOF",
			%s
			EOF = EOF,
      EMPTY = EMPTY,
      %s
		}

		local parse = function (input)
			local result, label, errpos = lpeg.match(grammar, input)
			if result then
			  print("Parsed: ", result)
			else
        local line, col = re.calcline(input, errpos)
			  print("LPEG Parsing failed at " .. line .. ":" .. col)
			  os.exit(1)
			end
			return lpeg.match(grammar, input)
		end

	 local input = io.read("*a")
	 print(parse(input))
		""",
        getFirstRule(rules), printRules(rules), getKeywords(rules));
  }
  ;

  public String getFirstRule(List<Rule> rules) {
    Rule startRule = rules.getFirst();
    return startRule.name();
  }

  public String printRules(List<Rule> rules) {
    StringBuilder sb = new StringBuilder();
    for (Rule rule : rules) {
      sb.append("  " + printRule(rule));
      sb.append(",\n");
    }
    return sb.toString().trim();
  }

  public String printNode(Node node) {
    return switch (node) {
      case Term term -> printTerm(term);
      case Ident ident -> printIdent(ident);
      case Sequence seq -> printSequence(seq);
      case OrderedChoice choice -> printOrderedChoice(choice);
      case Charset charset -> printCharset(charset);
      case Literal lit -> printLiteral(lit);
      case Empty e -> "EMPTY";
      case Not term -> "neg(" + printNode(term.node()) + ")";
      case Wildcard w -> "P(1)"; // Fetch next token && make wildcard = !nextToken;
      case EOF e -> "EOF";
    };
  }

  private String printRule(Rule rule) {
    String ws = rule.kind() == RuleKind.LEXING ? " * V\"WS\"^0" : "";
    this.currentRuleKind = rule.kind();
    return rule.name() + " = " + printNode(rule.rhs()) + ws;
  }

  private String printLiteral(Literal lit) {
    String fn = this.currentRuleKind == RuleKind.PARSING ? "tk" : "P";
    return fn + "(" + lit.content() + ")";
  }

  private String printCharset(Charset c) {
    String out = c.content().substring(1, c.content().length() - 1);
    return "regex\"" + Utils.sanitizeString(out) + "\"";
  }

  private String printTerm(Term term) {
    String nodeStr = printNode(term.node());
    if (term.op().isPresent()) {
      return "(" + nodeStr + ")" + printOperator(term.op().get());
    }
    return nodeStr;
  }

  private String printOperator(Operator op) {
    return switch (op) {
      case STAR -> "^0";
      case PLUS -> "^1";
      case OPTIONAL -> "^-1";
    };
  }

  private String printIdent(Ident ident) {
    return "V\"" + ident.name() + "\"";
  }

  private String printSequence(Sequence seq) {
    if (seq.nodes().isEmpty()) {
      return "";
    }
    if (seq.nodes().size() == 1) {
      return printNode(seq.nodes().get(0));
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < seq.nodes().size(); i++) {
      if (i > 0) {
        sb.append(" * ");
      }
      Node node = seq.nodes().get(i);
      String nodeStr = printNode(node);

      if (node instanceof Wildcard && i + 1 < seq.nodes().size()) {
        Node nextNode = seq.nodes().get(i + 1);
        nodeStr = "neg(" + printNode(nextNode) + ")";
      }

      if (node instanceof Term t && t.node() instanceof Wildcard && i + 1 < seq.nodes().size()) {
        Node nextNode = seq.nodes().get(i + 1);
        String op = t.op().isPresent() ? printOperator(t.op().get()) : "";
        nodeStr = "neg(" + printNode(nextNode) + ")" + op;
      }

      if (node instanceof OrderedChoice && seq.nodes().size() > 1) {
        nodeStr = "(" + nodeStr + ")";
      }

      sb.append(nodeStr);
    }
    return sb.toString();
  }

  private String printOrderedChoice(OrderedChoice choice) {
    if (choice.nodes().isEmpty()) {
      return "";
    }
    if (choice.nodes().size() == 1) {
      return printNode(choice.nodes().get(0));
    }

    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < choice.nodes().size(); i++) {
      if (i > 0) {
        sb.append(" + ");
      }
      var node = choice.nodes().get(i);
      String nodeStr = printNode(node);
      if (node instanceof Sequence && ((Sequence) node).nodes().size() > 1) {
        nodeStr = "(" + nodeStr + ")";
      }

      sb.append(nodeStr);
    }
    return sb.toString();
  }

  public String getKeywords(List<Rule> rules) {
    String keywords = rules.stream()
        .filter(r -> r.kind() == RuleKind.LEXING)
        .filter(r -> r.rhs() instanceof Literal)
        .map(r -> "P" + ((Literal) r.rhs()).toString())
        .collect(Collectors.joining(" + "));
      

      return keywords.isBlank() ? "" : "KEYWORDS = " + keywords + ",";

  }
}
