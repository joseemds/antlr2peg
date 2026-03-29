package peg.node;

public record Not(Node node, boolean consumeInput) implements Node {

  public Not(Node node) {
    this(node, true);
  }

  @Override
  public final String toString() {
    return "!" + node.toString();
  }
}
;
