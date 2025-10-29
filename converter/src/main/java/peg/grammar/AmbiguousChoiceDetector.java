package peg.grammar;

import java.util.List;
import peg.PegGrammar;
import peg.node.*;

public class AmbiguousChoiceDetector {
  private PegGrammar grammar;

  public AmbiguousChoiceDetector(PegGrammar grammar) {
    this.grammar = grammar;
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
        for (int i = 0; i < choices.size(); i++) {
          Node nodeI = choices.get(i);
          List<Node> firstI = grammar.firstOf(nodeI);
          for (int j = i + 1; j < choices.size(); j++) {
            Node nodeJ = choices.get(j);
            List<Node> firstJ = grammar.firstOf(nodeJ);
            List<Node> intersection = firstI;
            intersection.retainAll(firstJ);
            if (!intersection.isEmpty()) {
              System.err.printf(
                  "Warn: At Rule %s, choice %d (%s) and %d (%s) may match the same input\n",
                  ruleName, i + 1, nodeI, j + 1, nodeJ);
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

      default -> {}
    }
    ;
  }
}
