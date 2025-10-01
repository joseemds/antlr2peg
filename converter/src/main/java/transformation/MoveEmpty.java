package transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import peg.node.*;

public class MoveEmpty implements Transformation {

  @Override
  public Node apply(Node node) {
    return this.transformNode(node);
  }

  private Node transformNode(Node n) {
    return switch (n) {
      case Term t -> new Term(transformNode(t.node()), t.op());
      case Sequence seq -> {
        List<Node> transformed =
            seq.nodes().stream()
                .map(this::transformNode)
                .filter(x -> !(x instanceof Empty))
                .collect(Collectors.toList());
        yield new Sequence(transformed);
      }
      case OrderedChoice choice -> {
        List<Node> transformed = new ArrayList<>();
        List<Node> empties = new ArrayList<>();
        for (Node alt : choice.nodes()) {
          Node tAlt = transformNode(alt);
          if (isPossiblyEmpty(tAlt)) {
            empties.add(tAlt);
          } else {
            transformed.add(tAlt);
          }
        }
        transformed.addAll(empties);
        yield new OrderedChoice(transformed);
      }
      case Ident ident -> ident;
      case Literal lit -> lit;
      case Charset charset -> charset;
      case Not not -> new Not(transformNode(not.node()));
      case Empty e -> e;
      case Wildcard w -> w;
    };
  }

  private boolean isPossiblyEmpty(Node n) {
    return switch (n) {
      case Term t -> {
        if (t.op().isPresent()
            && (t.op().get() == Operator.OPTIONAL || t.op().get() == Operator.STAR)) {
          yield true;
        }
        yield isPossiblyEmpty(t.node());
      }
      case Ident ident -> false;
      case Sequence seq -> seq.nodes().stream().allMatch(this::isPossiblyEmpty);
      case OrderedChoice choice -> choice.nodes().stream().anyMatch(this::isPossiblyEmpty);
      case Literal lit -> lit.content().isEmpty();
      case Charset charset -> false;
      case Not not -> true;
      case Empty e -> true;
      case Wildcard w -> false;
    };
  }
}
