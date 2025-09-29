package utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import peg.node.*;
import peg.node.Rule;

public class PrefixTree {
  private final Map<Node, PrefixTree> children = new HashMap<>();
  private boolean isEnd;

  public PrefixTree() {}
  ;

  public PrefixTree(List<Rule> rules) {
    for (Rule r : rules) {
      List<Node> choices = extractChoices(r.rhs());
      for (Node choice : choices) {
        List<Node> sequence = flattenSeq(choice);
        insert(sequence);
      }
    }
  }

  public void insert(List<Node> sequence) {
    if (sequence.isEmpty()) {
      this.isEnd = true;
      return;
    }
    Node first = sequence.get(0);
    List<Node> rest = sequence.subList(1, sequence.size());
    PrefixTree child = children.get(first);
    if (child == null) {
      child = new PrefixTree();
      children.put(first, child);
    }
    child.insert(rest);
  }

  private List<Node> flattenSeq(Node node) {
    if (node instanceof Sequence seq) {
      List<Node> newSeq = new ArrayList<>();
      for (var child : seq.nodes()) {
        newSeq.addAll(flattenSeq(child));
      }
      return newSeq;
    } else {
      return List.of(node);
    }
  }

  private List<Node> extractChoices(Node node) {
    if (node instanceof OrderedChoice choice) {
      List<Node> newChoice = new ArrayList<>();
      for (var child : choice.nodes()) {
        newChoice.addAll(extractChoices(child));
      }
      return newChoice;
    } else {
      return List.of(node);
    }
  }

  public String prettyPrint() {
    return prettyPrintHelper(this, 0);
  }

  private String prettyPrintHelper(PrefixTree node, int indentLevel) {
    StringBuilder sb = new StringBuilder();
    String indent = "  ".repeat(indentLevel);

    if (node.children == null || node.children.isEmpty()) {
      sb.append(indent).append("(end)\n");
      return sb.toString();
    }

    for (Map.Entry<Node, PrefixTree> entry : node.children.entrySet()) {
      sb.append(indent).append(entry.getKey().toString()).append("\n");
      sb.append(prettyPrintHelper(entry.getValue(), indentLevel + 1));
    }
    return sb.toString();
  }
}
