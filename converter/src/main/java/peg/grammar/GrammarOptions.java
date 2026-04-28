package peg.grammar;

import exception.SemanticActionNotAllowedException;
import java.util.List;
import java.util.Optional;

public class GrammarOptions {
  public boolean caseInsensitive;
  public Optional<String> startRule = Optional.empty();
  public String identifierRule;
  public Optional<List<String>> skipRules = Optional.empty();

  public void setOption(String key, String value) {
    switch (key) {
      case "caseInsensitive" -> this.caseInsensitive = Boolean.parseBoolean(value);
      case "tokenVocab" -> {}
      default -> throw new SemanticActionNotAllowedException("Option is not supported");
    }
  }
}
