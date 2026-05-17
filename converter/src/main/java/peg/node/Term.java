package peg.node;

public record Term(Node node, Operator op) implements Node {
  @Override
  public final String toString() {
    return "(" + node + ")" + printOperator(op);
  }

  private String printOperator(Operator op) {
    return switch (op) {
      case STAR -> "*";
      case PLUS -> "+";
      case OPTIONAL -> "?";
    };
  }
}
