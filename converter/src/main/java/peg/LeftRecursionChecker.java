package peg;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import peg.node.Ident;
import peg.node.Node;
import peg.node.OrderedChoice;
import peg.node.Sequence;
import peg.node.Term;

public class LeftRecursionChecker {
  private final PegGrammar grammar;
  public Set<String> visited = new HashSet<>();

  public LeftRecursionChecker(PegGrammar grammar) {
    this.grammar = grammar;
  }

  public boolean check() {
    for (var rule : grammar.getRules()) {
      visited.clear();
      visited.add(rule.name());

      if (check(rule.rhs(), rule.name(), visited)) {
        return true;
      }
      ;
    }

    return false;
  }

  public boolean check(Node node, String currRule, Set<String> visited) {
    return switch (node) {
      case Term term -> check(term.node(), currRule, visited);
      case Ident id -> {
        if (id.name().equals(currRule)) yield true;
        if (visited.contains(id.name())) yield true;
        visited.add(id.name());
        var rule = grammar.findRuleByName(id.name()).rhs();
        yield check(rule, currRule, visited);
      }
      case Sequence seq -> {
        List<Node> nodes = seq.nodes();
        int i = 0;
        boolean result = false;
        while (i < nodes.size()) {
          boolean notEmpty = !grammar.isPossiblyEmpty(nodes.get(i));
          result |= check(nodes.get(i), currRule, new HashSet<>(visited));
          i++;
          if (notEmpty) {
            yield result;
          }
          ;
        }
        ;

        yield result;
      }
      case OrderedChoice oc -> {
        boolean result = false;
        for (var alternative : oc.nodes()) {
          result |= check(alternative, currRule, new HashSet<>(visited));
        }

        yield result;
      }
      default -> false;
    };
  }
}
