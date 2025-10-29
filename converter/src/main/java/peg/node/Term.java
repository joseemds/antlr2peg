package peg.node;

import java.util.Optional;

public record Term(Node node, Optional<Operator> op) implements Node {
  @Override
  public final String toString() {
    return op.isPresent() ? "(" + node + ")" + op.get() : node.toString();
  }
}
