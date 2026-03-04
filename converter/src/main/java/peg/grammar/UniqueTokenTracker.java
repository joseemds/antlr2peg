package peg.grammar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import peg.PegGrammar;
import peg.node.*;

public class UniqueTokenTracker {
  private final Map<String, TokenInfo> tokenOccurrences = new HashMap<>();
  private final Map<String, Boolean> uniquePaths = new HashMap<>();
  private final Map<String, Set<String>> ruleUniqueTokens = new HashMap<>();
  private final Set<String> uniqueTokens = new HashSet<>();
  private final PegGrammar grammar;

  public UniqueTokenTracker(PegGrammar grammar) {
    this.grammar = grammar;
    analyzeGrammar();
  }

  private void analyzeGrammar() {
    for (Rule rule : grammar.getRules()) {
      if (grammar.isSyntacticRule(rule)) {
        Set<String> tokens = extractDirectLexicalTokens(rule.rhs());
        for (String token : tokens) {
          tokenOccurrences.computeIfAbsent(token, k -> new TokenInfo()).addOccurrence(rule.name());
        }
      }
    }

    for (Rule rule : grammar.getRules()) {
      if (!grammar.isSyntacticRule(rule)) continue;

      String ruleName = rule.name();
      uniquePaths.put(ruleName, hasUniquePathInternal(rule, new HashSet<>()));

      Set<String> tokens = extractDirectLexicalTokens(rule.rhs());
      Set<String> ruleSpecificUniqueTokens = new HashSet<>();
      for (String token : tokens) {
        if (isUnique(token)) {
          ruleSpecificUniqueTokens.add(token);
          uniqueTokens.add(token);
        }
      }
      if (!ruleSpecificUniqueTokens.isEmpty()) {
        ruleUniqueTokens.put(ruleName, ruleSpecificUniqueTokens);
      }
    }
  }

  public boolean hasUniquePath(String ruleName) {
    return uniquePaths.getOrDefault(ruleName, false);
  }

  private boolean hasUniquePathInternal(Rule rule, Set<String> visited) {
    if (visited.contains(rule.name())) return false;
    visited.add(rule.name());
    return hasUniquePathForNode(rule.rhs(), new HashSet<>(visited));
  }

  public boolean hasUniquePathForNode(Node node, Set<String> visited) {
    return switch (node) {
      case Ident i -> {
        Rule refRule = grammar.findRuleByName(i.name());
        yield refRule != null
            && grammar.isSyntacticRule(refRule)
            && hasUniquePathInternal(refRule, visited);
      }
      case Literal lit -> isUnique(lit.content());
      case Sequence s ->
          s.nodes().stream().anyMatch(child -> hasUniquePathForNode(child, new HashSet<>(visited)));
      case OrderedChoice oc ->
          !oc.nodes().isEmpty()
              && oc.nodes().stream()
                  .allMatch(alt -> hasUniquePathForNode(alt, new HashSet<>(visited)));
      case Term t -> {
        if (t.op().isPresent()) {
          Operator op = t.op().get();
          if (op == Operator.OPTIONAL || op == Operator.STAR) yield false;
        }
        yield hasUniquePathForNode(t.node(), visited);
      }
      default -> false;
    };
  }

  public boolean isUnique(String tokenContent) {
    TokenInfo tokenInfo = tokenOccurrences.get(tokenContent);
    return tokenInfo != null && tokenInfo.isUnique();
  }

  public boolean hasUniqueToken(String ruleName) {
    Set<String> tokens = ruleUniqueTokens.get(ruleName);
    return tokens != null && !tokens.isEmpty();
  }

  public Set<String> getUniqueTokens() {
    return Collections.unmodifiableSet(uniqueTokens);
  }

  public Set<String> getUniqueTokensForRule(String ruleName) {
    return ruleUniqueTokens.getOrDefault(ruleName, Set.of());
  }

  public void printUniqueTokens() {
    System.out.println("=== Unique Tokens ===");
    System.out.println("Unique tokens are: " + new ArrayList<>(uniqueTokens));
  }

  public void printUniquePaths() {
    List<String> rulesWithUniquePaths =
        uniquePaths.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).toList();
    System.out.println("Unique paths are: " + rulesWithUniquePaths);
  }

  private Set<String> extractDirectLexicalTokens(Node n) {
    Set<String> tokens = new HashSet<>();
    switch (n) {
      case Ident i -> {
        Rule r = grammar.findRuleByName(i.name());
        if (r != null && r.rhs() != null && grammar.isLexicalRule(r)) {
          tokens.addAll(extractAllLexicalTokens(r.rhs(), new HashSet<>()));
        }
      }
      case Literal lit -> tokens.add(lit.content());
      case Sequence s ->
          s.nodes().forEach(child -> tokens.addAll(extractDirectLexicalTokens(child)));
      case OrderedChoice oc ->
          oc.nodes().forEach(child -> tokens.addAll(extractDirectLexicalTokens(child)));
      case Term t -> tokens.addAll(extractDirectLexicalTokens(t.node()));
      default -> {}
    }
    return tokens;
  }

  private Set<String> extractAllLexicalTokens(Node n, Set<String> visited) {
    Set<String> tokens = new HashSet<>();
    switch (n) {
      case Ident i -> {
        if (visited.contains(i.name())) break;
        visited.add(i.name());
        Rule r = grammar.findRuleByName(i.name());
        if (r == null || r.rhs() == null) break;
        tokens.addAll(extractAllLexicalTokens(r.rhs(), new HashSet<>(visited)));
      }
      case Literal lit -> tokens.add(lit.content());
      case Sequence s ->
          s.nodes()
              .forEach(
                  child -> tokens.addAll(extractAllLexicalTokens(child, new HashSet<>(visited))));
      case OrderedChoice oc ->
          oc.nodes()
              .forEach(
                  child -> tokens.addAll(extractAllLexicalTokens(child, new HashSet<>(visited))));
      case Term t -> tokens.addAll(extractAllLexicalTokens(t.node(), new HashSet<>(visited)));
      default -> {}
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
