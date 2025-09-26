package transformation;

import peg.node.Node;

@FunctionalInterface
public interface Transformation {
  public Node apply(Node node);
}
