package peg.node;

import charset.CharacterSet;
import java.util.List;

public record Charset(List<CharacterSet> content) implements Node {

  @Override
  public final String toString() {
    return content.toString();
  }
}
