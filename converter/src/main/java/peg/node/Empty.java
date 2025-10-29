package peg.node;

public record Empty() implements Node {
  @Override
  public final String toString() {
    return "ε";
  }
}
;
