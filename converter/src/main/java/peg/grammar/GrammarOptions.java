package peg.grammar;
import exception.SemanticActionNotAllowedException;

public class GrammarOptions {
  public boolean caseInsensitive;

  public void setOption(String key, String value) {
    switch (key) {
      case "caseInsensitive" -> this.caseInsensitive = Boolean.parseBoolean(value);
      default -> throw new SemanticActionNotAllowedException("Option is not supported");
    }
  }
}
