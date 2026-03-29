package peg.grammar;

import exception.SemanticActionNotAllowedException;
import java.util.List;

public class GrammarOptions {
  public boolean caseInsensitive;
  public String identifierRule;
  public List<String> skipRules;

  public void setOption(String key, String value) {
    switch (key) {
      case "caseInsensitive" -> this.caseInsensitive = Boolean.parseBoolean(value);
      case "tokenVocab" -> {}
      default -> throw new SemanticActionNotAllowedException("Option is not supported");
    }
  }
}
