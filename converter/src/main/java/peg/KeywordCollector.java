package peg;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import peg.node.*;

public class KeywordCollector {

  private final PegGrammar grammar;
  Set<String> keywords = new HashSet<>();

  public KeywordCollector(PegGrammar grammar) {
    this.grammar = grammar;
  }

  public List<String> collectKeywords() {
    for (Rule r : this.grammar.getRules()) {
      this.keywords.addAll(getKeyword(r.rhs()));
    }

    return new ArrayList<>(this.keywords);
  }

  private Set<String> getKeyword(Node node) {
    return switch (node) {
      case Sequence seq ->
          seq.nodes().stream()
              .map(this::getKeyword)
              .flatMap(Set::stream)
              .collect(Collectors.toSet());

      case OrderedChoice choice ->
          choice.nodes().stream()
              .map(this::getKeyword)
              .flatMap(Set::stream)
              .collect(Collectors.toSet());
      case Literal lit when isWord(lit.content()) -> Set.of(lit.content());
      default -> Set.of();
    };
  }

  private boolean isWord(String s) {
		String sanitizedString = s.replaceAll("'", "");
    return !sanitizedString.isBlank() && sanitizedString.length() > 1&& sanitizedString.chars().allMatch(Character::isLetter);
  }
}
