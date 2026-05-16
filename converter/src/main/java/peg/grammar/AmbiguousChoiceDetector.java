package peg.grammar;

import java.util.ArrayList;
import java.util.List;
import peg.PegGrammar;
import peg.node.*;
import utils.StatsTracker;

public class AmbiguousChoiceDetector {
  private PegGrammar grammar;
  private StatsTracker statsTracker;

  public AmbiguousChoiceDetector(PegGrammar grammar, StatsTracker tracker) {
    this.grammar = grammar;
    this.statsTracker = tracker;
  }

  public void checkAmbiguity() {
    for (Rule r : grammar.getRules()) {
      checkNode(r.rhs(), r.name());
    }
  }

  private void checkNode(Node node, String ruleName) {
    switch (node) {
      case OrderedChoice oc -> {
        List<Node> choices = oc.nodes();
        if (choices.size() < 2) return;
        for (int i = 0; i < choices.size(); i++) {
          Node nodeI = choices.get(i);
          List<Node> firstI = grammar.firstOf(nodeI);
          for (int j = i + 1; j < choices.size(); j++) {
            Node nodeJ = choices.get(j);
            List<Node> firstJ = grammar.firstOf(nodeJ);
            List<Node> intersection = new ArrayList<Node>(firstI);
            intersection.retainAll(firstJ);
            if (!intersection.isEmpty()) {
              System.err.printf(
                  "Warning: At Rule %s, choice (%s) and (%s) may match the same input\n",
                  ruleName, nodeI, nodeJ);
              statsTracker.bumpChoiceAmbiguities();
            }
          }
        }
      }
      case Sequence s -> {
        for (Node n : s.nodes()) {
          checkNode(n, ruleName);
        }
      }
      case Term t -> {
        checkNode(t.node(), ruleName);
      }
      case Not not -> {
        checkNode(not.node(), ruleName);
      }

      case And and -> {
        checkNode(and.node(), ruleName);
      }

      default -> {}
    }
    ;
  }
}
