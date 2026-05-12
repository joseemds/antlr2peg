package peg.node;

public record Repetition(Node node, Operator op, Kind kind) implements Node {

  public enum Kind {
    EAGER,
    LAZY
  }

  public Repetition(Node node, Operator op) {
    this(node, op, Kind.EAGER);
  }

  @Override
  public final String toString() {
    return "(" + node + ")" + printOperator(op);
  }

  public final boolean isLazy() {
    return kind == Kind.LAZY;
  }

  private String printOperator(Operator op) {
    return switch (op) {
      case STAR -> "*";
      case PLUS -> "+";
      case OPTIONAL -> "?";
    };
  }
}
