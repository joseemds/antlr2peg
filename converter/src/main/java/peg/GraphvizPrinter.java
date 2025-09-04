package peg;

import java.util.List;
import peg.node.*;

public class GraphvizPrinter {

  public String print(List<Node> nodes) {
    StringBuilder dotBuilder = new StringBuilder();

    dotBuilder.append("digraph peg_ast {\n");
    dotBuilder.append("  rankdir=TB;\n");
    dotBuilder.append("  node [shape=box, style=rounded, fontname=\"Helvetica\"];\n");
    dotBuilder.append("  edge [fontname=\"Helvetica\"];\n\n");

    int[] counter = {0};

    for (Node node : nodes) {
      printNode(node, dotBuilder, counter);
    }

    dotBuilder.append("}\n");
    return dotBuilder.toString();
  }

  private String printNode(Node node, StringBuilder dotBuilder, int[] counter) {
    final String nodeId = "node" + (counter[0]++);

    String label =
        switch (node) {
          case Rule rule -> {
            String childId = printNode(rule.rhs(), dotBuilder, counter);
            dotBuilder.append(String.format("  %s -> %s;\n", nodeId, childId));
            yield "Rule: " + sanitize(rule.name());
          }
          case Sequence seq -> {
            for (int i = 0; i < seq.nodes().size(); i++) {
              String childId = printNode(seq.nodes().get(i), dotBuilder, counter);
              dotBuilder.append(String.format("  %s -> %s [label=\"%d\"];\n", nodeId, childId, i));
            }
            yield "Sequence";
          }
          case OrderedChoice choice -> {
            for (int i = 0; i < choice.nodes().size(); i++) {
              String childId = printNode(choice.nodes().get(i), dotBuilder, counter);
              dotBuilder.append(String.format("  %s -> %s [label=\"%d\"];\n", nodeId, childId, i));
            }
            yield "Choice";
          }
          case Term term -> {
            String childId = printNode(term.node(), dotBuilder, counter);
            dotBuilder.append(String.format("  %s -> %s;\n", nodeId, childId));
            String op = term.op().map(this::printOperator).orElse("");
            yield op.isEmpty() ? "Term" : "Term " + op;
          }
          case Not not -> {
            String childId = printNode(not.node(), dotBuilder, counter);
            dotBuilder.append(String.format("  %s -> %s;\n", nodeId, childId));
            yield "Not ~";
          }
          case Literal lit -> "Literal: " + sanitize(lit.content());
          case Ident ident -> "Ident: " + sanitize(ident.name());
          case Charset charset -> "Charset: " + sanitize(charset.content());
          case Empty e -> "ε";
          case Wildcard w -> "•";
        };

    dotBuilder.append(String.format("  %s [label=\"%s\"];\n", nodeId, label));
    return nodeId;
  }

  private String printOperator(Operator op) {
    return switch (op) {
      case STAR -> "*";
      case PLUS -> "+";
      case OPTIONAL -> "?";
    };
  }

  private String sanitize(String s) {
    if (s == null) return "";
    return s.replace("\\", "\\\\").replace("\"", "\\\"");
  }
}
