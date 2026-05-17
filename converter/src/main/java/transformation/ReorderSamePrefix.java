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
      case Repetition rep -> new Repetition(reorderNode(rep.node()), rep.op());
      default -> node;
    };
  }

  private List<Node> toElements(Node node) {
    return node instanceof Sequence seq ? seq.nodes() : List.of(node);
  }

  private void reorderByPrefix(List<Node> alternatives) {
    if (alternatives.size() <= 1) return;

    alternatives.sort(
        (a, b) -> {
          List<Node> ea = toElements(a);
          List<Node> eb = toElements(b);

          int shared = sharedPrefixLength(ea, eb);
          if (shared == 0) return 0;

          if (shared == ea.size() && eb.size() > ea.size()) return 1;

          if (shared == eb.size() && ea.size() > eb.size()) return -1;

          return 0;
        });
  }

  private int sharedPrefixLength(List<Node> a, List<Node> b) {
    int len = Math.min(a.size(), b.size());
    for (int i = 0; i < len; i++) {
      if (!nodesEqual(a.get(i), b.get(i))) return i;
    }
    return len;
  }

  private boolean nodesEqual(Node a, Node b) {
    if (a.getClass() != b.getClass()) return false;
    return switch (a) {
      case Literal litA -> litA.content().equals(((Literal) b).content());
      case Charset csA -> csA.equals(b); // TODO: proprely charset implementation
      case Ident idA -> idA.name().equals(((Ident) b).name());
      default -> a.equals(b);
    };
  }
}
