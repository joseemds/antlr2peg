package peg.grammar;

import exception.SemanticActionNotAllowedException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GrammarOptions {
  public boolean caseInsensitive;
  public Optional<String> startRule = Optional.empty();
  public String identifierRule;
  public Set<String> skipRules = new HashSet<String>();

  public void setOption(String key, String value) {
    switch (key) {
      case "caseInsensitive" -> this.caseInsensitive = Boolean.parseBoolean(value);
      case "tokenVocab" -> {}
      default -> throw new SemanticActionNotAllowedException("Option is not supported");
    }
  }
}
