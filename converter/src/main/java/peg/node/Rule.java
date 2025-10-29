package peg.node;

public record Rule(String name, Node rhs, RuleKind kind) {
  @Override
  public final String toString() {
    return "name <-" + rhs.toString();
  }
}
