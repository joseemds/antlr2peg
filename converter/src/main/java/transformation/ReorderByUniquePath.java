package transformation;

import java.util.*;
import peg.PegGrammar;
import peg.grammar.UniqueTokenTracker;
import peg.node.*;

public class ReorderByUniquePath implements Transformation {

  private final UniqueTokenTracker uniqueTokenTracker;

  public ReorderByUniquePath(PegGrammar grammar) {
    this.uniqueTokenTracker = new UniqueTokenTracker(grammar);
    this.uniqueTokenTracker.analyzeGrammar();
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
    if (a instanceof Ident aIdent && b instanceof Ident bIdent) {
      boolean aUniquePath = uniqueTokenTracker.hasUniquePath(aIdent.name());
      boolean bUniquePath = uniqueTokenTracker.hasUniquePath(bIdent.name());
      if (aUniquePath && !bUniquePath) {
        System.out.printf("Swaping %s and %s, %s is uniquePath\n", aIdent, bIdent, aIdent);
        return -1;
      }
      if (!aUniquePath && bUniquePath) {

        System.out.printf("Swaping %s and %s, %s is uniquePath\n", aIdent, bIdent, bIdent);
        return 1;
      }
    }

    return 0;
  }
}
