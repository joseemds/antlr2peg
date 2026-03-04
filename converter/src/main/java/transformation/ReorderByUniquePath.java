package transformation;

import java.util.*;
import peg.PegGrammar;
import peg.grammar.UniqueTokenTracker;
import peg.node.*;
import utils.StatsTracker;

public class ReorderByUniquePath implements Transformation {

  private final UniqueTokenTracker uniqueTokenTracker;
  private final StatsTracker statsTracker;

  public ReorderByUniquePath(PegGrammar grammar, StatsTracker statsTracker) {
    this.uniqueTokenTracker = new UniqueTokenTracker(grammar);
    this.statsTracker = statsTracker;
  }

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
        reorderByUniquePath(children);
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

  private void reorderByUniquePath(List<Node> alternatives) {
    if (alternatives.size() <= 1) return;
    alternatives.sort(this::compareByUniquePath);
  }

  private int compareByUniquePath(Node a, Node b) {
    boolean aUniquePath = getUniquePath(a);
    boolean bUniquePath = getUniquePath(b);

    if (aUniquePath && !bUniquePath) {
      System.out.printf("Swapping %s and %s, %s has unique path\n", a, b, a);
      statsTracker.bumpUniquePathSwaps();
      return -1;
    }
    if (!aUniquePath && bUniquePath) {
      System.out.printf("Swapping %s and %s, %s has unique path\n", a, b, b);
      statsTracker.bumpUniquePathSwaps();
      return 1;
    }
    return 0;
  }

  private boolean getUniquePath(Node n) {
    return switch (n) {
      case Ident i -> uniqueTokenTracker.hasUniquePath(i.name());
      default -> uniqueTokenTracker.hasUniquePathForNode(n, new HashSet<>());
    };
  }
}
