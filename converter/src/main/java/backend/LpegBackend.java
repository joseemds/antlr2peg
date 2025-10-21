package backend;

import java.util.List;
import peg.node.*;
import peg.node.Node;
import utils.Utils;

public class LpegBackend {

  public String convert(List<Rule> rules) {
    return String.format(
        """
		local lpeg = require "lpeg"
		local re = require "re"
		local P, S, V = lpeg.P, lpeg.S, lpeg.V
    local EMPTY = P''
    local neg = function (pat)
     return P(1) - pat
    end
		local regex = function (s)
			return re.compile(s)
		end
		local rule = function (s)
			return V(s) * V"WS"^0
		end
		local tk = function (s)
			return P(s) * V"WS"^0
		end
		local EOF = P(-1)

		local grammar = {
			\"%s\",
			%s
			EOF = EOF,
      EMPTY = EMPTY,
		}

		local parse = function (input)
			local result = lpeg.match(grammar, input)
			if result then
			  print("Parsed: ", result)
			else
			  print("LPEG Parsing failed")
			  os.exit(1)
			end
			return lpeg.match(grammar, input)
		end

	 local input = io.read("*a")
	 print(parse(input))
		""",
        getFirstRule(rules), printRules(rules));
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
      case Wildcard w -> "P(1)";
    };
  }

  private String printRule(Rule rule) {
    return rule.name() + " = " + printNode(rule.rhs());
  }

  private String printLiteral(Literal lit) {
    return "tk(" + lit.content() + ")";
  }

  private String printCharset(Charset c) {
    String out = c.content().substring(1, c.content().length() - 1);
    return "regex\"" + Utils.sanitizeString(out) + "\"";
  }

  private String printTerm(Term term) {
    String nodeStr = printNode(term.node());
    // if (term.node() instanceof Term term2){
    // 	System.out.println("here");
    //     nodeStr = "(" + printNode(term2) + ")";
    // } else {
    //    nodeStr = printNode(term.node());
    //   }

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
}
