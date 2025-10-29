package peg.node;

public record Ident(String name) implements Node {
  @Override
  public final String toString() {
    return name;
  }
}
