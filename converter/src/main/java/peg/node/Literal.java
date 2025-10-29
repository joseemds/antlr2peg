package peg.node;

public record Literal(String content) implements Node {
  @Override
  public final String toString() {
    return content;
  }
}
