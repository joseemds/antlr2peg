package peg.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import peg.PegGrammar;
import peg.node.*;

public class UniqueTokenTracker {
  private final Map<Node, TokenInfo> tokenOccurrences = new HashMap<>();
  private PegGrammar grammar;

  public UniqueTokenTracker(PegGrammar grammar) {
    this.grammar = grammar;
  }

  public void analyzeGrammar() {
    for (Rule rule : grammar.getRules()) {
      if (grammar.isSyntacticRule(rule)) {
        List<Node> tokens = extractLexicalTokens(rule.rhs());
        for (Node token : tokens) {
          tokenOccurrences.computeIfAbsent(token, k -> new TokenInfo()).addOccurrence(rule.name());
        }
      }
    }
  }

  public boolean isUnique(Node n) {
    return tokenOccurrences.get(n).isUnique();
  }

  private List<Node> extractLexicalTokens(Node n) {
    List<Node> tokens = new ArrayList<>();
    switch (n) {
      case Ident i -> {
        Rule r = grammar.findRuleByName(i.name());
        if (grammar.isLexicalRule(r)) {
          tokens.addAll(extractLexicalTokens(r.rhs()));
        }
      }
      case Literal lit -> tokens.add(n);
      case Charset cs -> tokens.add(n);
      case Sequence s -> {
        for (Node children : s.nodes()) {
          tokens.addAll(extractLexicalTokens(children));
        }
      }
      case OrderedChoice oc -> {
        for (Node children : oc.nodes()) {
          tokens.addAll(extractLexicalTokens(children));
        }
      }
      case Term t -> tokens.addAll(extractLexicalTokens(t.node()));
      default -> {} // noop
    }

    return tokens;
  }

  private static class TokenInfo {
    private final Set<String> rules = new HashSet<>();
    private final Map<String, Integer> ruleCounts = new HashMap<>();

    public void addOccurrence(String ruleName) {
      rules.add(ruleName);
      ruleCounts.merge(ruleName, 1, Integer::sum);
    }

    public boolean isUnique() {
      return rules.size() == 1 && ruleCounts.values().iterator().next() == 1;
    }
  }
}
