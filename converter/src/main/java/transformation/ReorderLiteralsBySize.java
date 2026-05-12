package transformation;

import java.util.List;
import java.util.stream.Collectors;
import peg.PegGrammar;
import peg.node.*;
import utils.StatsTracker;

public class ReorderLiteralsBySize implements Transformation {

  private final PegGrammar grammar;
  private final StatsTracker statsTracker;

  public ReorderLiteralsBySize(PegGrammar grammar, StatsTracker statsTracker) {
    this.grammar = grammar;
    this.statsTracker = statsTracker;
  }

  private boolean isLiteralOrLexicalRule(Node n) {
    return switch (n) {
      case Literal l -> true;
      case Ident i -> {
        Rule r = grammar.findRuleByName(i.name());
        yield grammar.isLexicalRule(r) && r.rhs() instanceof Literal;
      }
      default -> false;
    };
  }

  private Literal expand(Node a) {
    return switch (a) {
      case Ident i ->
          (Literal)
              grammar
                  .findRuleByName(i.name())
                  .rhs(); // safe cast because of the isLiteralOrLexicalRule check
      case Literal l -> l;
      default -> throw new IllegalStateException("This should be unreachable");
    };
  }

  private int compare(Node a, Node b) {
    Literal a_ = expand(a);
    Literal b_ = expand(b);
    return Integer.compare(b_.content().length(), a_.content().length());
  }

  @Override
  public Node apply(Node node) {
    return switch (node) {
      case Repetition rep -> new Repetition(apply(rep.node()), rep.op(), rep.kind());
      case Sequence seq -> {
        List<Node> transformed =
            seq.nodes().stream()
                .map(this::apply)
                .filter(x -> !(x instanceof Empty))
                .collect(Collectors.toList());
        yield new Sequence(transformed);
      }
      case OrderedChoice choice -> {
        List<Node> nodes = choice.nodes();
        boolean allLiterals = nodes.stream().allMatch(this::isLiteralOrLexicalRule);
        if (allLiterals) {
          List<Node> sorted = nodes.stream().sorted(this::compare).collect(Collectors.toList());
          statsTracker.bumpChoiceOfLiteralsReordered();
          yield new OrderedChoice(sorted);
        } else {
          yield new OrderedChoice(nodes.stream().map(this::apply).collect(Collectors.toList()));
        }
      }
      case Not n -> new Not(apply(n.node()), n.consumeInput());
      case And n -> new And(apply(n.node()));
      default -> node;
    };
  }
}
