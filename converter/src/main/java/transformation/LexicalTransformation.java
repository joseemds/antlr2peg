package transformation;

import peg.node.Node;

@FunctionalInterface
public interface LexicalTransformation {
  public Node apply(Node node);
}
