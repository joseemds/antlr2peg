package peg.node;

public record Charset(String content) implements Node {

  @Override
  public final String toString() {
    return "[" + this.content + "]";
  }
}
