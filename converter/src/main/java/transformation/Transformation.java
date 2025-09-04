package transformation;

import java.util.List;
import peg.node.Node;

@FunctionalInterface
public interface Transformation {
  public List<Node> apply(List<Node> nodes);
}
