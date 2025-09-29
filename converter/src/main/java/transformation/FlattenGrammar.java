package transformation;

import java.util.List;
import java.util.stream.Collectors;
import peg.node.*;

public class FlattenGrammar implements Transformation {

  public FlattenGrammar() {}

  @Override
  public Node apply(Node n) {
    return flattenNode(n);
  }

  private Node flattenNode(Node node) {
    return switch (node) {
      case OrderedChoice oc -> {
        List<Node> children =
            oc.nodes().stream().map(this::flattenNode).collect(Collectors.toList());

        if (children.size() == 1) {
          yield children.get(0);
        }

        yield new OrderedChoice(children);
      }
      case Sequence seq -> {
        List<Node> children =
            seq.nodes().stream().map(this::flattenNode).collect(Collectors.toList());

        if (children.size() == 1) {
          yield children.get(0);
        }

        yield new Sequence(children);
      }

      case Not not -> new Not(flattenNode(not.node()));
      case Term term -> {
        Node innerNode = flattenNode(term.node());

        if (term.op().isEmpty()) {
          yield innerNode;
        }

        yield new Term(innerNode, term.op());
      }
      default -> node;
    };
  }
}
