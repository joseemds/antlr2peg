package peg.node;

public record Wildcard() implements Node {
  @Override
  public final String toString() {
    return "*";
  }
}
