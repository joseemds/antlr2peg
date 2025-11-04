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
  private final Map<String, TokenInfo> tokenOccurrences = new HashMap<>();
  private final Map<String, Boolean> uniquePaths = new HashMap<>();
  private final Map<String, Set<String>> ruleUniqueTokens = new HashMap<>();
  private PegGrammar grammar;

  public UniqueTokenTracker(PegGrammar grammar) {
    this.grammar = grammar;
  }

  public void analyzeGrammar() {
    for (Rule rule : grammar.getRules()) {
      if (grammar.isSyntacticRule(rule)) {
        Set<String> tokens = extractDirectLexicalTokens(rule.rhs());
        for (String token : tokens) {
          tokenOccurrences.computeIfAbsent(token, k -> new TokenInfo()).addOccurrence(rule.name());
        }
      }
    }

    for (Rule rule : grammar.getRules()) {
      if (grammar.isSyntacticRule(rule)) {
        uniquePaths.put(rule.name(), hasUniquePath(rule, new HashSet<>()));
      }
    }
  }

  public boolean hasUniquePath(String ruleName) {
    return uniquePaths.getOrDefault(ruleName, false);
  }

  private boolean hasUniquePath(Rule rule, Set<String> visited) {
    if (visited.contains(rule.name())) return false;
    visited.add(rule.name());

    return hasUniquePath(rule.rhs(), new HashSet<>(visited));
  }

  private boolean hasUniquePath(Node node, Set<String> visited) {
    switch (node) {
      case Ident i -> {
        Rule refRule = grammar.findRuleByName(i.name());
        if (refRule != null && grammar.isSyntacticRule(refRule)) {
          return hasUniquePath(refRule, visited);
        }
        return false;
      }
      case Literal lit -> {
        return isUnique(lit.content());
      }
      case Sequence s -> {
        for (Node child : s.nodes()) {
          if (hasUniquePath(child, new HashSet<>(visited))) {
            return true;
          }
        }
        return false;
      }
      case OrderedChoice oc -> {
        if (oc.nodes().isEmpty()) return false;
        for (Node alternative : oc.nodes()) {
          if (!hasUniquePath(alternative, new HashSet<>(visited))) {
            return false;
          }
        }
        return true;
      }
      case Term t -> {
        if (t.op().isPresent()) {
          Operator op = t.op().get();
          if (op == Operator.OPTIONAL || op == Operator.STAR) return false;
        }
        return hasUniquePath(t.node(), visited);
      }
      default -> {
        return false;
      }
    }
  }

  private Set<String> findUniqueTokensInRule(Node n, Set<String> visited) {
    Set<String> uniqueTokens = new HashSet<>();
    switch (n) {
      case Ident i -> {
        if (visited.contains(i.name())) return uniqueTokens;
        visited.add(i.name());

        Rule r = grammar.findRuleByName(i.name());
        if (r != null && grammar.isSyntacticRule(r)) {
          uniqueTokens.addAll(findUniqueTokensInRule(r.rhs(), new HashSet<>(visited)));
        }
      }
      case Literal lit -> {
        if (isUnique(lit.content())) {
          uniqueTokens.add(lit.content());
        }
      }
      case Sequence s -> {
        for (Node child : s.nodes()) {
          uniqueTokens.addAll(findUniqueTokensInRule(child, new HashSet<>(visited)));
        }
      }
      case OrderedChoice oc -> {
        // For ordered choice, we need to check each alternative
        for (Node child : oc.nodes()) {
          uniqueTokens.addAll(findUniqueTokensInRule(child, new HashSet<>(visited)));
        }
      }
      case Term t -> {
        uniqueTokens.addAll(findUniqueTokensInRule(t.node(), new HashSet<>(visited)));
      }
      default -> {}
    }
    return uniqueTokens;
  }

  private boolean hasUniqueToken(Node node) {
    return switch (node) {
      case Term term -> {
        if (term.op().isPresent()) {
          Operator op = term.op().get();
          if (op == Operator.OPTIONAL || op == Operator.STAR) yield false;
        }
        yield hasUniqueToken(term.node());
      }
      case Ident ident -> {
        Rule r = grammar.findRuleByName(ident.name());
        yield hasUniqueToken(r.rhs());
      }
      case Sequence seq -> {
        for (var alternative : seq.nodes()) {
          if (hasUniqueToken(alternative)) yield true;
        }
        yield false;
      }
      case OrderedChoice choice -> {
        for (var alternative : choice.nodes()) {
          if (!hasUniqueToken(alternative)) yield false;
        }
        yield true;
      }
      case Charset charset -> false;
      case Literal lit -> isUnique(lit.content());
      case Empty e -> false;
      case Not term -> false; // TODO: how to evaluate
      case Wildcard w -> false;
      case EOF e -> false;
    };
  }

  public void printUniqueTokens() {
    System.out.println("=== Unique Tokens ===");
    List<String> uniqueTokens = new ArrayList<>();
    for (Map.Entry<String, TokenInfo> entry : tokenOccurrences.entrySet()) {
      String token = entry.getKey();
      TokenInfo info = entry.getValue();
      if (info.isUnique()) {
        uniqueTokens.add(token);
      }
    }
    System.out.println("Unique tokens are: " + uniqueTokens);
  }

  public void printUniquePaths() {
    System.out.println("Unique Paths");
    List<String> uniques = new ArrayList<>();
    for (Map.Entry<String, Boolean> entry : uniquePaths.entrySet()) {
      String rule = entry.getKey();
      Boolean isUnique = entry.getValue();
      if (isUnique) {
        uniques.add(rule);
      }
    }
    System.out.println("Unique paths are: " + uniques);
  }

  public boolean isUnique(String tokenContent) {
    TokenInfo tokenInfo = tokenOccurrences.get(tokenContent);
    return tokenInfo != null && tokenInfo.isUnique();
  }

  public boolean hasUniqueToken(String ruleName) {
    Set<String> uniqueTokens = ruleUniqueTokens.get(ruleName);
    return uniqueTokens != null && !uniqueTokens.isEmpty();
  }

  private Set<String> extractDirectLexicalTokens(Node n) {
    Set<String> tokens = new HashSet<>();
    switch (n) {
      case Ident i -> {
        Rule r = grammar.findRuleByName(i.name());
        if (r != null && grammar.isLexicalRule(r)) {
          tokens.addAll(extractAllLexicalTokens(r.rhs(), new HashSet<>()));
        }
      }
      case Literal lit -> tokens.add(lit.content());
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

  private Set<String> extractAllLexicalTokens(Node n, Set<String> visited) {
    Set<String> tokens = new HashSet<>();
    switch (n) {
      case Ident i -> {
        if (visited.contains(i.name())) break;
        visited.add(i.name());

        Rule r = grammar.findRuleByName(i.name());
        if (r == null) break;

        tokens.addAll(extractAllLexicalTokens(r.rhs(), new HashSet<>(visited)));
      }
      case Literal lit -> tokens.add(lit.content());
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
