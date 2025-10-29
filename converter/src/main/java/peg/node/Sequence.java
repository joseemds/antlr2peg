package peg.node;

import java.util.List;

public record Sequence(List<Node> nodes) implements Node {
  @Override
  public final java.lang.String toString() {
    String out = nodes.get(0).toString();
    for (int i = 1; i < nodes.size(); i++) {
      out += " " + nodes.get(i).toString();
    }

    return out;
  }
}
