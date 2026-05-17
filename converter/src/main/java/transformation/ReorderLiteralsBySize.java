package transformation;

import java.util.List;
import java.util.stream.Collectors;
import peg.node.*;

public class ReorderLiteralsBySize implements Transformation {

  @Override
  public Node apply(Node node) {
    return switch (node) {
      case Repetition rep -> new Repetition(apply(rep.node()), rep.op());
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
        boolean allLiterals = nodes.stream().allMatch(n -> n instanceof Literal);
        if (allLiterals) {
          List<Node> sorted =
              nodes.stream()
                  .sorted(
                      (a, b) ->
                          Integer.compare(
                              ((Literal) b).content().length(), ((Literal) a).content().length()))
                  .collect(Collectors.toList());
          yield new OrderedChoice(sorted);
        } else {
          yield new OrderedChoice(nodes.stream().map(this::apply).collect(Collectors.toList()));
        }
      }
      case Not n -> new Not(apply(n.node()));
      case And n -> new And(apply(n.node()));
      default -> node;
    };
  }
}
