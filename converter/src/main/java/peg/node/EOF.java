package peg.node;

public record EOF() implements Node {
  @Override
  public final String toString() {
    return "EOF";
  }
}
