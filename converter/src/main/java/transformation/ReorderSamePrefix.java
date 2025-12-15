package transformation;

import java.util.*;
import peg.PegGrammar;
import peg.node.*;

public class ReorderSamePrefix implements Transformation {

  public ReorderSamePrefix(PegGrammar grammar) {}

  @Override
  public Node apply(Node n) {
    return reorderNode(n);
  }

  private Node reorderNode(Node node) {
    return switch (node) {
      case OrderedChoice oc -> {
        List<Node> children = new ArrayList<>();
        for (Node child : oc.nodes()) {
          children.add(reorderNode(child));
        }
        reorderByPrefix(children);
        yield new OrderedChoice(children);
      }
      case Sequence seq -> {
        List<Node> children = new ArrayList<>();
        for (Node child : seq.nodes()) {
          children.add(reorderNode(child));
        }
        yield new Sequence(children);
      }
      case Not not -> new Not(reorderNode(not.node()));
      case Term term -> new Term(reorderNode(term.node()), term.op());
      default -> node;
    };
  }

  private void reorderByPrefix(List<Node> alternatives) {
    if (alternatives.size() <= 1) return;

    // alternatives.sort(
    //     (a, b) -> {
    //       // int prefixComparison = compareByPrefix(a, b);
    //       // if (prefixComparison != 0) return prefixComparison;
    //     });

    alternatives.clear();
  }

  private int compareByPrefix(List<Node> a, List<Node> b) {
    int sharedLen = sharedPrefixLength(a, b);

    if (sharedLen > 0) {
      boolean aIsPrefixOfB = sharedLen == a.size() && b.size() > a.size();
      boolean bIsPrefixOfA = sharedLen == b.size() && a.size() > b.size();

      if (aIsPrefixOfB) return 1; // b (mais longo) antes de a
      if (bIsPrefixOfA) return -1; // a (mais longo) antes de b
    }

    return 0;
  }

  private boolean isTerminal(Node node) {
    return node instanceof Literal || node instanceof Charset;
  }

  private int sharedPrefixLength(List<Node> a, List<Node> b) {
    int len = Math.min(a.size(), b.size());
    for (int i = 0; i < len; i++) {
      if (!nodesEqual(a.get(i), b.get(i))) {
        return i;
      }
    }
    return len;
  }

  private boolean nodesEqual(Node a, Node b) {
    if (a.getClass() != b.getClass()) return false;

    return switch (a) {
      case Literal litA -> {
        Literal litB = (Literal) b;
        yield litA.content().equals(litB.content());
      }
      case Charset csA -> {
        Charset csB = (Charset) b;
        yield csA.equals(csB); // Implement proper Charset equality
      }
      case Ident idA -> {
        Ident idB = (Ident) b;
        yield idA.name().equals(idB.name());
      }
      default -> a.equals(b);
    };
  }
}
