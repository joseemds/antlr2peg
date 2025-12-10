package peg.node;

public record And(Node node) implements Node {

  @Override
  public final String toString() {
    return "&" + "(" + node.toString() + ")";
  }
}
