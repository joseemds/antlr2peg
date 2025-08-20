package backend;

import java.util.List;
import peg.node.*;
import peg.node.Node;

public class LpegBackend {

  public String convert(List<Node> rules) {
    return String.format(
        """
		local lpeg = require "lpeg"
		local re = require "re"
		local P, S, V = lpeg.P, lpeg.S, lpeg.V
		local regex = function (s)
			return re.compile(s)
		end
		local tk = function (s)
			return P(s) * V"WS"^0
		end
		local EOF = P(-1)

		local grammar = {
			\"%s\",
			%s
			EOF = EOF,
		}

		local parse = function (input)
			return lpeg.match(grammar, input)
		end
		""",
        getFirstRule(rules), printRules(rules));
  }
  ;

  public String getFirstRule(List<Node> nodes) {
    Node node = nodes.getFirst();
    if (node instanceof Rule rule) {
      return rule.name();
    }

    throw new Error("Top-level nodes must be rules");
  }

  public String printRules(List<Node> nodes) {
    StringBuilder sb = new StringBuilder();
    for (Node node : nodes) {
      sb.append(printNode(node));
      sb.append(",\n");
    }
    return sb.toString().trim();
  }

  public String printNode(Node node) {
    return switch (node) {
      case Rule rule -> printRule(rule);
      case Term term -> printTerm(term);
      case Ident ident -> printIdent(ident);
      case Sequence seq -> printSequence(seq);
      case OrderedChoice choice -> printOrderedChoice(choice);
      case Charset charset -> printCharset(charset);
      case Literal lit -> printLiteral(lit);
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
    return "regex\"" + out + "\"";
  }

  private String printTerm(Term term) {
    String nodeStr = printNode(term.node());
    if (term.op().isPresent()) {
      return nodeStr + printOperator(term.op().get());
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
