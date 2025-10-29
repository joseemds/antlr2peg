package peg.node;

public record Not(Node node) implements Node {

  @Override
  public final String toString() {
    return "!" + node.toString();
  }
}
;
