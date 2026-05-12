package peg;

import charset.CharacterSet;
import charset.LiteralNode;
import charset.RangeNode;
import exception.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import peg.grammar.GrammarOptions;
import peg.node.*;
import transformation.RuleTransformation;
import transformation.Transformation;

public class PegGrammar {
  private GrammarOptions grammarOptions;
  private List<Rule> rules = new ArrayList<>();
  private Map<String, Set<Node>> firstSets = new HashMap<>();
  private Map<String, Set<Node>> followSets = new HashMap<>();
  private Map<Node, Set<Node>> nodeFollowSets = new IdentityHashMap<>();
  public Map<String, Node> nonTerminals = new HashMap<>();

  public PegGrammar() {
    this.grammarOptions = new GrammarOptions();
  }

  public PegGrammar(GrammarOptions grammarOptions) {
    this.grammarOptions = grammarOptions;
  }

  public void setGrammarOptions(GrammarOptions grammarOptions) {
    this.grammarOptions = grammarOptions;
  }

  public GrammarOptions getOptions() {
    return this.grammarOptions;
  }

  public Repetition mkRepetition(Node node, Operator op) {
    return new Repetition(node, op);
  }

  public CharacterSet mkRange(String to, String from) {
    return new RangeNode(to, from);
  }

  public List<Rule> getRules() {
    return this.rules;
  }

  public void mergeGrammars(PegGrammar lexer) {
    this.rules.addAll(lexer.getRules());
  }

  public Map<String, Set<Node>> getFirsts() {
    return this.firstSets;
  }

  public Map<String, Set<Node>> getFollows() {
    return this.followSets;
  }

  public Map<String, Node> getNonTerminals() {
    return this.nonTerminals;
  }

  public Operator operatorOfString(String op) {
    switch (op) {
      case "?":
      case "??":
        return Operator.OPTIONAL;
      case "+":
      case "+?":
        return Operator.PLUS;
      case "*":
      case "*?":
        return Operator.STAR;
      default:
        throw new Error("Unexpected operator " + op);
    }
  }

  public Ident mkIdent(String name) {
    return new Ident(name);
  }

  public Literal mkLiteral(String content) {
    return new Literal(content);
  }

  public LiteralNode mkCharsetLiteral(String content) {
    return new LiteralNode(content);
  }

  // public Charset mkCharset(String content) {
  //   return new Charset(content);
  // }

  public Charset mkCharset(List<CharacterSet> cs) {
    return new Charset(cs);
  }

  public Charset mkCharset(CharacterSet... cs) {
    return new Charset(List.of(cs));
  }

  public Rule mkRule(String lhs, Node rhs, RuleKind kind) {
    return new Rule(lhs, rhs, kind);
  }

  public Rule mkLexicalRule(String lhs, Node rhs) {
    return new Rule(lhs, rhs, RuleKind.LEXING);
  }

  public Rule mkParsingRule(String lhs, Node rhs) {
    return new Rule(lhs, rhs, RuleKind.PARSING);
  }

  public Rule mkFragmentRule(String lhs, Node rhs) {
    return new Rule(lhs, rhs, RuleKind.FRAGMENT);
  }

  public OrderedChoice mkOrderedChoice(List<Node> nodes) {
    return new OrderedChoice(nodes);
  }

  public OrderedChoice mkOrderedChoice(Node... nodes) {
    return new OrderedChoice(List.of(nodes));
  }

  public Sequence mkSequence(Node... nodes) {
    return new Sequence(List.of(nodes));
  }

  public Sequence mkSequence(List<Node> nodes) {
    return new Sequence(nodes);
  }

  public Empty mkEmpty() {
    return new Empty();
  }

  public Not mkNot(Node node) {
    return new Not(node);
  }

  public Not mkNot(Node node, boolean consumeInput) {
    return new Not(node, consumeInput);
  }

  public Wildcard mkWildcard() {
    return new Wildcard();
  }

  public And mkAnd(Node node) {
    return new And(node);
  }

  public void addRule(Rule rule) {
    this.rules.add(rule);
  }

  public PegGrammar transform(RuleTransformation transformation) {
    List<Rule> newRules = this.rules.stream().map(transformation::apply).toList();

    this.rules.clear();
    this.rules.addAll(newRules);
    return this;
  }

  public PegGrammar transform(Transformation transformation) {
    this.rules =
        this.rules.stream()
            .map(
                rule ->
                    isSyntacticRule(rule)
                        ? new Rule(rule.name(), transformation.apply(rule.rhs()), rule.kind())
                        : rule)
            .collect(Collectors.toCollection(ArrayList::new));
    return this;
  }

  public void computeFirst() {
    for (Rule rule : rules) {
      if (isSyntacticRule(rule)) {
        firstSets.putIfAbsent(rule.name(), new HashSet<>());
      }
    }

    boolean changed;
    do {
      changed = false;
      for (Rule rule : rules) {
        if (!isSyntacticRule(rule)) continue;
        Set<Node> firstSet = firstSets.computeIfAbsent(rule.name(), k -> new HashSet<>());
        List<Node> rhsFirst = firstOf(rule.rhs());
        if (firstSet.addAll(rhsFirst)) {
          changed = true;
        }
      }
    } while (changed);
  }

  public List<Node> firstOf(Node node) {
    return firstOf(node, false);
  }

  public List<Node> firstOf(Node node, boolean expandLexical) {
    return firstOf(node, expandLexical, new HashSet<>());
  }

  private List<Node> firstOf(Node node, boolean expandLexical, Set<String> visited) {
    List<Node> result = new ArrayList<>();

    switch (node) {
      case Literal lit -> result.add(lit);
      case Charset cs -> result.add(cs);
      case Wildcard w -> result.add(w);
      case Empty e -> result.add(e);
      case EOF eof -> result.add(eof);
      case Not n -> result.add(new Empty());
      case And and -> result.add(new Empty());

      case Ident ident -> {
        Rule r = findRuleByName(ident.name());

        if (!expandLexical && !isSyntacticRule(r)) {
          result.add(ident);
        } else {
          if (visited.add(ident.name())) {
            if (!expandLexical) {
              result.addAll(firstSets.getOrDefault(ident.name(), Set.of()));
            } else {
              if (r.rhs() != null) {
                result.addAll(firstOf(r.rhs(), true, visited));
              }
            }
            visited.remove(ident.name());
          }
        }
      }

      case Sequence seq -> {
        for (Node part : seq.nodes()) {
          List<Node> partFirst = firstOf(part, expandLexical, visited);
          result.addAll(partFirst.stream().filter(x -> !(x instanceof Empty)).toList());
          if (!isPossiblyEmpty(part)) break;
        }
        if (seq.nodes().stream().allMatch(this::isPossiblyEmpty)) {
          result.add(new Empty());
        }
      }

      case OrderedChoice oc -> {
        for (Node option : oc.nodes()) {
          result.addAll(firstOf(option, expandLexical, visited));
        }
      }

      case Repetition r -> {
        switch (r.op()) {
          case OPTIONAL, STAR -> {
            result.addAll(firstOf(r.node(), expandLexical, visited));
            result.add(new Empty());
          }
          case PLUS -> {
            result.addAll(firstOf(r.node(), expandLexical, visited));
          }
        }
      }
    }

    return result;
  }

  public boolean isPossiblyEmpty(Node n) {
    return isPossiblyEmpty(n, new HashSet<>());
  }

  public boolean isPossiblyEmpty(Node n, Set<String> visited) {
    return switch (n) {
      case Repetition r -> {
        if (r.op() == Operator.OPTIONAL || r.op() == Operator.STAR) yield true;
        yield isPossiblyEmpty(r.node(), visited);
      }
      case Ident ident -> {
        if (!visited.add(ident.name())) yield false;

        Rule r = findRuleByName(ident.name());

        boolean empty = isPossiblyEmpty(r.rhs(), visited);

        visited.remove(ident.name());
        yield empty;
      }
      case Sequence seq -> seq.nodes().stream().allMatch(node -> isPossiblyEmpty(node, visited));
      case OrderedChoice choice ->
          choice.nodes().stream().anyMatch(node -> isPossiblyEmpty(node, visited));
      case Literal lit -> lit.content().isEmpty();
      case Charset charset -> false;
      case Not not -> true;
      case And and -> true;
      case Empty e -> true;
      case Wildcard w -> false;
      case EOF e -> false;
    };
  }

  public void computeFollowSets() {

    for (Rule rule : rules) {
      if (isSyntacticRule(rule)) {
        followSets.putIfAbsent(rule.name(), new HashSet<>());
      }
    }

    if (rules.isEmpty()) return;

    Rule startRule =
        this.grammarOptions
            .startRule
            .flatMap(name -> rules.stream().filter(r -> r.name().equals(name)).findFirst())
            .orElse(rules.getFirst());

    if (!isSyntacticRule(startRule)) {
      throw new WrongStartRuleException(
          "%s may not be the starting rule".formatted(startRule.name()));
    }
    followSets.get(startRule.name()).add(new EOF());

    boolean changed;
    do {
      changed = false;
      for (Rule rule : rules) {
        if (!isSyntacticRule(rule)) continue;

        Set<Node> ruleFollow = followSets.get(rule.name());
        changed |= pushFollow(rule.rhs(), ruleFollow);
      }
    } while (changed);
  }

  private boolean pushFollow(Node node, Set<Node> follow) {
    boolean changed = false;

    Set<Node> thisFollow = nodeFollowSets.computeIfAbsent(node, k -> new HashSet<>());
    changed |= thisFollow.addAll(follow);

    switch (node) {
      case Ident ident -> {
        Rule r = findRuleByName(ident.name());
        if (!isSyntacticRule(r)) break; // lexical idents: nothing to propagate

        Set<Node> dest = followSets.computeIfAbsent(ident.name(), k -> new HashSet<>());
        changed |= dest.addAll(follow);
      }

      case Sequence seq -> {
        List<Node> nodes = seq.nodes();

        for (int i = 0; i < nodes.size(); i++) {
          Set<Node> innerFollow = firstOfSequenceTail(nodes, i + 1);

          boolean tailNullable = innerFollow.remove(new Empty()); // strip ε marker

          if (tailNullable) {
            innerFollow.addAll(follow);
          }

          changed |= pushFollow(nodes.get(i), innerFollow);
        }
      }

      case OrderedChoice oc -> {
        for (Node alternative : oc.nodes()) {
          changed |= pushFollow(alternative, follow);
        }
      }

      case Repetition r -> {
        Set<Node> innerFollow = new HashSet<>(follow);

        switch (r.op()) {
          case STAR, PLUS -> {
            List<Node> selfFirst = firstOf(r.node());
            selfFirst.stream().filter(n -> !(n instanceof Empty)).forEach(innerFollow::add);
          }
          case OPTIONAL -> {}
        }

        changed |= pushFollow(r.node(), innerFollow);
      }

      case Not not -> {}
      case And and -> {}

      case Literal lit -> {}
      case Charset cs -> {}
      case Wildcard w -> {}
      case Empty e -> {}
      case EOF eof -> {}
    }

    return changed;
  }

  private Set<Node> firstOfSequenceTail(List<Node> nodes, int from) {
    Set<Node> result = new HashSet<>();

    for (int i = from; i < nodes.size(); i++) {
      List<Node> fi = firstOf(nodes.get(i));

      fi.stream().filter(n -> !(n instanceof Empty)).forEach(result::add);

      if (!isPossiblyEmpty(nodes.get(i))) {
        return result;
      }
    }

    result.add(new Empty());
    return result;
  }

  public void computeNonTerminals() {
    for (Rule r : rules) {
      nonTerminals.put(r.name(), r.rhs());
    }
  }

  public Rule findRuleByName(String name) {
    if (name.equals("EOF"))
      return new Rule("EOF", new EOF(), RuleKind.LEXING); // TODO: is this correct?
    for (Rule r : rules) {
      if (r.name().equals(name)) return r;
    }
    throw new RuleNotFoundException("Rule with name " + name + " not found");
  }

  public boolean isSyntacticRule(Rule r) {
    return r.kind() == RuleKind.PARSING;
  }

  public boolean isLexicalRule(Rule r) {
    return r.kind() == RuleKind.LEXING;
  }

  public boolean isFragment(Rule r) {
    return r.kind() == RuleKind.FRAGMENT;
  }

  public boolean isTerminal(Node n) {
    return switch (n) {
      case Literal lit -> true;
      case Charset charset -> true;
      default -> false;
    };
  }
}
