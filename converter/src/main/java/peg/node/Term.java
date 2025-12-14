package peg.node;

import java.util.Optional;

public record Term(Node node, Optional<Operator> op) implements Node {
  @Override
  public final String toString() {
    return op.isPresent() ? "(" + node + ")" + printOperator(op.get()) : node.toString();
  }

  private String printOperator(Operator op) {
    return switch (op) {
      case STAR -> "*";
      case PLUS -> "+";
      case OPTIONAL -> "?";
    };
  }
}
