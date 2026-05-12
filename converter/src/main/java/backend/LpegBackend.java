package backend;

import charset.CharacterSet;
import charset.LiteralNode;
import charset.RangeNode;
import charset.UTF8RangeNode;
import exception.SemanticActionNotAllowedException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import peg.PegGrammar;
import peg.node.*;
import peg.node.Node;
import utils.Utils;

public class LpegBackend {
  private PegGrammar grammar;
  private final Map<String, Set<Node>> firstSets;
  private final Map<String, Set<Node>> followSets;
  private final boolean hasSkipRules;
  private static final Set<String> LUA_KEYWORDS =
      Set.of(
          "and",
          "break",
          "do",
          "else",
          "elseif",
          "end",
          "false",
          "for",
          "function",
          "goto",
          "if",
          "in",
          "local",
          "nil",
          "not",
          "or",
          "repeat",
          "return",
          "then",
          "true",
          "until",
          "while");

  public LpegBackend(PegGrammar grammar) {
    this.grammar = grammar;
    this.firstSets = grammar.getFirsts();
    this.followSets = grammar.getFirsts();
    this.hasSkipRules = !grammar.getOptions().skipRules.isEmpty();
  }

  private RuleKind currentRuleKind;

  public String convert(List<Rule> rules) {
    String skipRuleAfter = this.hasSkipRules ? "* V\"SKIP_\"^0" : "";
    String skipRuleBefore = this.hasSkipRules ? "V\"SKIP_\"^0 * " : "";

    return String.format(
        """
		local lpeg = require "lpeglabel"
		local re = require "relabel"
		local P, S, V, R, utfR = lpeg.P, lpeg.S, lpeg.V, lpeg.R, lpeg.utfR
    local EMPTY = P''
		local _idRest = R"az" + R"AZ" + R"09" + P"_"
    local neg = function (pat)
     return P(1) - pat
    end
		local regex = function (s)
			return re.compile(s)
		end
		local tk = function (s)
			return P(s) %s
		end

		local keyword = function (s)
			return P(s) * -_idRest

		end
		local EOF = P(-1)

    local ci =  function (s)
      local pat = P""
      for i = 1, #s do
        local ch = s:sub(i, i)
        local lower = ch:lower()
        local upper = ch:upper()
        if lower == upper then
         pat = pat * P(ch)
        else
         pat = pat * S(lower .. upper)
        end
      end
      return pat
    end

		local grammar = {
			\"start_\",
      start_ = %s V\"%s\",
			%s
			EOF = EOF,
      EMPTY = EMPTY,
		}

		local parse = function (input)
			lpeg.setmaxstack(6400)
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
        skipRuleAfter, skipRuleBefore, getFirstRule(rules), printRules(rules));
  }
  ;

  public String getFirstRule(List<Rule> rules) {
    if (grammar.getOptions().startRule.isPresent()) {
      return grammar.findRuleByName(grammar.getOptions().startRule.get()).name();
    }
    Rule startRule = rules.getFirst();
    return startRule.name();
  }

  public String printRules(List<Rule> rules) {
    StringBuilder sb = new StringBuilder();
    for (Rule rule : rules) {
      if (rule.name().equals("_keywords")) {
        sb.append(printKeywords(rule.rhs()));
				continue;
      }
      sb.append("  " + printRule(rule));
      sb.append(",\n");
    }
    return sb.toString().trim();
  }

  public String printNode(Node node) {
    return switch (node) {
      case Repetition rep -> printRepetition(rep);
      case Ident ident -> printIdent(ident);
      case Sequence seq -> printSequence(seq);
      case OrderedChoice choice -> printOrderedChoice(choice);
      case Charset charset -> printCharset(charset);
      case Literal lit -> printLiteral(lit);
      case Empty e -> "EMPTY";
      case Not not -> {
        if (not.consumeInput()) {
          yield "neg(" + printNode(not.node()) + ")";
        } else {
          yield "-(" + printNode(not.node()) + ")";
        }
      }
      case And term -> "#(" + printNode(term.node()) + ")";
      case Wildcard w -> "P(1)"; // Fetch next token && make wildcard = !nextToken;
      case EOF e -> "EOF";
    };
  }

  private String printRule(Rule rule) {
    if (rule.name().equals("_keywords")) {}

    this.currentRuleKind = rule.kind();
    String name = LUA_KEYWORDS.contains(rule.name()) ? "[\"" + rule.name() + "\"]" : rule.name();

    if (rule.kind() == RuleKind.LEXING && this.hasSkipRules) {
      return name + " = " + "(" + printNode(rule.rhs()) + ")" + " * V\"SKIP_\"^0";
    }
    return name + " = " + printNode(rule.rhs());
  }

  private String printLiteral(Literal lit) {
    String content = Utils.sanitizeString(lit.content());
    if (grammar.getOptions().caseInsensitive) {
      content = "ci(" + content + ")";
    }
    ;
    String fn = this.currentRuleKind == RuleKind.PARSING ? "tk" : "P";
    return fn + "(" + content + ")";
  }

  private String printCharset(Charset c) {
    String out = c.content().stream().map(this::printCharset).collect(Collectors.joining(" + "));

    if (c.content().size() > 1 || grammar.getOptions().caseInsensitive) {
      return "(" + out + ")";
    }

    return out;
  }

  private String printCharset(CharacterSet cs) {
    return switch (cs) {
      case RangeNode range -> printCharset(range);
      case LiteralNode literal -> printCharset(literal);
      case UTF8RangeNode utf8Range -> printUTF8Range(utf8Range);
    };
  }

  private String printUTF8Range(UTF8RangeNode utf8RangeNode) {
    return "utfR(%s,%s)"
        .formatted(escapeUTF8(utf8RangeNode.from()), escapeUTF8(utf8RangeNode.to()));
  }

  private int escapeUTF8(String s) {
    if (s.startsWith("\\u")) {
      s = s.substring(2);

      if (s.length() > 4)
        throw new SemanticActionNotAllowedException("Unsupported UTF8 with more then 4 chars");

      return Integer.parseInt(s.substring(2), 16);
    }
    return s.codePointAt(0);
  }

  private String printCharset(RangeNode range) {
    String base = "R('%s%s')".formatted(range.from(), range.to());
    if (grammar.getOptions().caseInsensitive) {
      if (isLowerRange(range))
        return base
            + " + R('%s%s')".formatted(range.from().toUpperCase(), range.to().toUpperCase());
      if (isUpperRange(range))
        return base
            + " + R('%s%s')".formatted(range.from().toLowerCase(), range.to().toLowerCase());
    }
    return base;
  }

  private String printCharset(LiteralNode literal) {
    String ch = Utils.sanitizeChar(literal.ch());
    String base = "P(%s)".formatted(ch);
    if (grammar.getOptions().caseInsensitive) {
      if (isLower(ch)) return base + " + P(%s)".formatted(ch.toUpperCase());
      if (isUpper(ch)) return base + " + P(%s)".formatted(ch.toLowerCase());
    }
    return base;
  }

  private boolean isLowerRange(RangeNode range) {
    return isLower(range.from()) && isLower(range.to());
  }

  private boolean isUpperRange(RangeNode range) {
    return isUpper(range.from()) && isUpper(range.to());
  }

  private boolean isLower(String token) {
    return token.length() == 1 && Character.isLowerCase(token.charAt(0));
  }

  private boolean isUpper(String token) {
    return token.length() == 1 && Character.isUpperCase(token.charAt(0));
  }

  private String printRepetition(Repetition rep) {
    String nodeStr = printNode(rep.node());
    return "(" + nodeStr + ")" + printOperator(rep.op());
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

      if (node instanceof Repetition rep
          && rep.node() instanceof Wildcard
          && i + 1 < seq.nodes().size()) {
        Node nextNode = seq.nodes().get(i + 1);
        String op = printOperator(rep.op());
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

  private String printKeywords(Node keywords) {
    OrderedChoice kws = (OrderedChoice) keywords;
    return "_keywords = "
        + kws.nodes().stream().map(l -> "keyword(" + (l) + ")").collect(Collectors.joining(" + ")) + ",";
  }
}
