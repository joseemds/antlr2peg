package peg.node;

public record Rule(String name, Node rhs, RuleKind kind) {
  public Rule(String name, Node rhs) {
    this(name, rhs, RuleKind.PARSING);
  }

  @Override
  public final String toString() {
    return "name <-" + rhs.toString();
  }
}
