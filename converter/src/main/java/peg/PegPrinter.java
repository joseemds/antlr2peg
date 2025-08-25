package peg;

import peg.node.*;

public class PegPrinter {

  public String print(PegAst ast) {
    StringBuilder sb = new StringBuilder();
    for (Node node : ast.getAst()) {
      sb.append(printNode(node));
      sb.append("\n");
    }
    return sb.toString().trim();
  }

  public String printNode(Node node) {
    return switch (node) {
      case Rule rule -> printRule(rule);
      case Literal lit -> lit.content();
      case Term term -> printTerm(term);
      case Ident ident -> printIdent(ident);
      case Sequence seq -> printSequence(seq);
      case OrderedChoice choice -> printOrderedChoice(choice);
      case Charset charset -> charset.content();
      case Not term -> "~" + printNode(term);
      case Empty e -> "ε";
      case Wildcard w -> "*";
    };
  }

  private String printRule(Rule rule) {
    return rule.name() + " <- " + printNode(rule.rhs());
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
      case STAR -> "*";
      case PLUS -> "+";
      case OPTIONAL -> "?";
    };
  }

  private String printIdent(Ident ident) {
    return ident.name();
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
        sb.append(" ");
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
        sb.append(" / ");
      }
      sb.append(printNode(choice.nodes().get(i)));
    }
    return sb.toString();
  }
}
