package transformation;

import peg.node.Node;

@FunctionalInterface
public interface SyntaticTransformation {
  public Node apply(Node node);
}
