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
        List<Node> tokens = extractDirectLexicalTokens(rule.rhs());
        for (Node token : tokens) {
          tokenOccurrences.computeIfAbsent(token, k -> new TokenInfo()).addOccurrence(rule.name());
        }
      }
    }
  }

  public void printUniqueTokens() {
    System.out.println("=== Unique Tokens ===");
    List<Node>  uniqueTokens = new ArrayList<>();
    for (Map.Entry<Node, TokenInfo> entry : tokenOccurrences.entrySet()) {
      Node token = entry.getKey();
      TokenInfo info = entry.getValue();
      if (info.isUnique()) {
        uniqueTokens.add(token);
      }
    }
    System.out.println("Unique tokens are: " + uniqueTokens);
  }

  public boolean isUnique(Node n) {
    TokenInfo tokenInfo = tokenOccurrences.get(n);
    if (tokenInfo == null) return false;
    return tokenInfo.isUnique();
  }

  public boolean containsUniqueToken(Node n) {
    List<Node> tokens = extractAllLexicalTokens(n, new HashSet<>());
    for (Node token : tokens) {
      if (isUnique(token)) {
        return true;
      }
    }
    return false;
  }

  private List<Node> extractDirectLexicalTokens(Node n) {
    List<Node> tokens = new ArrayList<>();
    switch (n) {
      case Ident i -> {
        Rule r = grammar.findRuleByName(i.name());
        if (r != null && grammar.isLexicalRule(r)) {
          tokens.addAll(extractAllLexicalTokens(r.rhs(), new HashSet<>()));
        }
      }
      case Literal lit -> tokens.add(n);
      case Sequence s -> {
        for (Node children : s.nodes()) {
          tokens.addAll(extractDirectLexicalTokens(children));
        }
      }
      case OrderedChoice oc -> {
        for (Node children : oc.nodes()) {
          tokens.addAll(extractDirectLexicalTokens(children));
        }
      }
      case Term t -> tokens.addAll(extractDirectLexicalTokens(t.node()));
      default -> {}
    }
    return tokens;
  }

  private List<Node> extractAllLexicalTokens(Node n, Set<String> visited) {
    List<Node> tokens = new ArrayList<>();
    switch (n) {
      case Ident i -> {
        if (visited.contains(i.name())) break;
        visited.add(i.name());

        Rule r = grammar.findRuleByName(i.name());
        if (r == null) break;

        tokens.addAll(extractAllLexicalTokens(r.rhs(), new HashSet<>(visited)));
      }
      case Literal lit -> tokens.add(n);
      case Sequence s -> {
        for (Node children : s.nodes()) {
          tokens.addAll(extractAllLexicalTokens(children, new HashSet<>(visited)));
        }
      }
      case OrderedChoice oc -> {
        for (Node children : oc.nodes()) {
          tokens.addAll(extractAllLexicalTokens(children, new HashSet<>(visited)));
        }
      }
      case Term t -> tokens.addAll(extractAllLexicalTokens(t.node(), new HashSet<>(visited)));
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
