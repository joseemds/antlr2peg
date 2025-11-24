package peg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import peg.grammar.GrammarOptions;
import peg.node.*;
import transformation.Transformation;

public class PegGrammar {
  private GrammarOptions grammarOptions;
  private List<Rule> rules = new ArrayList<>();
  private Map<String, Set<Node>> firstSets = new HashMap<>();
  private Map<String, Set<Node>> followSets = new HashMap<>();
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

  public Term mkTerm(Node node, Optional<Operator> op) {
    return new Term(node, op);
  }

  public List<Rule> getRules() {
    return this.rules;
  }

  public Map<String, Set<Node>> getFirsts() {
    return this.firstSets;
  }

  public Map<String, Node> getNonTerminals() {
    return this.nonTerminals;
  }

  public Operator operatorOfString(String op) {
    switch (op) {
      case "?":
        return Operator.OPTIONAL;
      case "+":
        return Operator.PLUS;
      case "*":
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

  public Charset mkCharset(String content) {
    return new Charset(content);
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

  public Sequence mkSequence(List<Node> nodes) {
    return new Sequence(nodes);
  }

  public Empty mkEmpty() {
    return new Empty();
  }

  public Not mkNot(Node node) {
    return new Not(node);
  }

  public Wildcard mkWildcard() {
    return new Wildcard();
  }

  public void addRule(Rule rule) {
    this.rules.add(rule);
  }

  public PegGrammar transform(Transformation transformation) {
    this.rules =
        this.rules.stream()
            .map(rule -> new Rule(rule.name(), transformation.apply(rule.rhs()), rule.kind()))
            .toList();
    return this;
  }

  public void computeFirst() {
    boolean changed;
    do {
      changed = false;
      for (Rule rule : rules) {
        Set<Node> firstSet = firstSets.computeIfAbsent(rule.name(), k -> new HashSet<>());
        List<Node> rhsFirst = firstOf(rule.rhs());
        if (firstSet.addAll(rhsFirst)) {
          changed = true;
        }
      }
    } while (changed);
  }

  public void computeFollow() {
    boolean changed = false;
    do {
      for (Rule rule : rules) {
        Set<Node> followsSet = followSets.computeIfAbsent(rule.name(), k -> new HashSet<>());
        List<Node> rhsFollow = followsOf(rule.rhs());
        changed = followsSet.addAll(rhsFollow);
      }
    } while (changed);
  }

  public List<Node> followsOf(Node node) {
    List<Node> result = new ArrayList<>();
    switch (node) {
      case Literal l -> result.add(l);
      case Wildcard w -> result.add(w);
      case Charset cs -> result.add(cs);
      case Empty e -> result.add(e);
      case Term t -> result.addAll(followsOf(t.node()));
      case Not t -> result.addAll(followsOf(t.node())); // FIXME
      case Ident ident -> {
        result.addAll(firstSets.getOrDefault(ident, Set.of()));
      }
      case Sequence s -> {
        for (Node n : s.nodes()) {
          result.addAll(followsOf(n));
        }
      }
      case OrderedChoice c -> {
        List<Node> elements = c.nodes();
        if (elements.size() == 1) throw new Error("Single element sequence should not exists");

        for (int i = 0; i < elements.size(); i++) {
          Node current = elements.get(i);
          for (int j = i + 1; j < elements.size(); j++) {
            Node next = elements.get(j);
            result.addAll(firstSets.getOrDefault(next, Set.of()));
          }
        }
      }
      case EOF eof -> result.add(eof);
    }

    return result;
  }

  public List<Node> firstOf(Node node) {
    List<Node> result = new ArrayList<>();

    switch (node) {
      case Literal lit -> result.add(lit);
      case Charset cs -> result.add(cs);
      case Wildcard w -> result.add(w);
      case Empty e -> result.add(e);
      case EOF eof -> result.add(eof);
      case Not n -> result.add(new Empty());
      case Ident ident -> {
        result.addAll(firstSets.getOrDefault(ident.name(), Set.of()));
      }

      case Sequence seq -> {
        for (Node part : seq.nodes()) {
          List<Node> partFirst = firstOf(part);
          result.addAll(partFirst.stream().filter(x -> !(x instanceof Empty)).toList());
          if (!isPossiblyEmpty(part)) break;
        }
        if (seq.nodes().stream().allMatch(this::isPossiblyEmpty)) {
          result.add(new Empty());
        }
      }

      case OrderedChoice oc -> {
        for (Node option : oc.nodes()) {
          result.addAll(firstOf(option));
        }
      }

      case Term t -> {
        if (t.op().isPresent()) {
          switch (t.op().get()) {
            case OPTIONAL, STAR -> {
              result.addAll(firstOf(t.node()));
              result.add(new Empty());
            }
            case PLUS -> {
              result.addAll(firstOf(t.node()));
            }
          }
        } else {
          result.addAll(firstOf(t.node()));
        }
      }
    }

    return result;
  }

  public void computeFollowSets() {
    for (Rule rule : rules) {
      followSets.putIfAbsent(rule.name(), new HashSet<>());
    }

    if (!rules.isEmpty()) {
      followSets.get(rules.get(0).name()).add(new EOF());
    }

    boolean changed;
    do {
      changed = false;
      for (Rule rule : rules) {
        String A = rule.name();
        Node rhs = rule.rhs();

        List<Ident> idents = collectIdents(rhs);
        for (Ident B : idents) {
          Set<Node> followB = followSets.computeIfAbsent(B.name(), k -> new HashSet<>());
          Set<Node> before = new HashSet<>(followB);

          List<Node> tail = tailAfter(rhs, B);
          if (!tail.isEmpty()) {
            Set<Node> firstTail = new HashSet<>();
            for (Node t : tail) {
              firstTail.addAll(firstOf(t));
              if (!isPossiblyEmpty(t)) break;
            }
            followB.addAll(firstTail.stream().filter(n -> !(n instanceof Empty)).toList());
            if (tail.stream().allMatch(this::isPossiblyEmpty)) {
              followB.addAll(followSets.getOrDefault(A, Set.of()));
            }
          } else {
            followB.addAll(followSets.getOrDefault(A, Set.of()));
          }

          if (!followB.equals(before)) changed = true;
        }
      }
    } while (changed);
  }

  private List<Ident> collectIdents(Node node) {
    List<Ident> list = new ArrayList<>();
    if (node instanceof Ident ident) {
      list.add(ident);
    } else if (node instanceof Sequence seq) {
      for (Node n : seq.nodes()) list.addAll(collectIdents(n));
    } else if (node instanceof OrderedChoice oc) {
      for (Node n : oc.nodes()) list.addAll(collectIdents(n));
    } else if (node instanceof Term t) {
      list.addAll(collectIdents(t.node()));
    } else if (node instanceof Not not) {
      list.addAll(collectIdents(not.node()));
    }
    return list;
  }

  private List<Node> tailAfter(Node root, Ident target) {
    if (root instanceof Sequence seq) {
      List<Node> nodes = seq.nodes();
      for (int i = 0; i < nodes.size(); i++) {
        if (nodes.get(i) instanceof Ident ident && ident.name().equals(target.name())) {
          return nodes.subList(i + 1, nodes.size());
        }
      }
    } else if (root instanceof OrderedChoice oc) {
      for (Node n : oc.nodes()) {
        List<Node> t = tailAfter(n, target);
        if (!t.isEmpty()) return t;
      }
    } else if (root instanceof Term t) {
      return tailAfter(t.node(), target);
    } else if (root instanceof Not not) {
      return tailAfter(not.node(), target);
    }
    return List.of();
  }

  private boolean isPossiblyEmpty(Node n) {
    return switch (n) {
      case Term t -> {
        if (t.op().isPresent()
            && (t.op().get() == Operator.OPTIONAL || t.op().get() == Operator.STAR)) {
          yield true;
        }
        yield isPossiblyEmpty(t.node());
      }
      case Ident ident -> false;
      case Sequence seq -> seq.nodes().stream().allMatch(this::isPossiblyEmpty);
      case OrderedChoice choice -> choice.nodes().stream().anyMatch(this::isPossiblyEmpty);
      case Literal lit -> lit.content().isEmpty();
      case Charset charset -> false;
      case Not not -> true;
      case Empty e -> true;
      case Wildcard w -> false;
      case EOF e -> false;
    };
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
    throw new Error("Rule with name " + name + "not found");
  }

  public boolean isSyntacticRule(Rule r) {
    return r.kind() == RuleKind.PARSING;
  }

  public boolean isLexicalRule(Rule r) {
    return r.kind() == RuleKind.LEXING;
  }

  public boolean isTerminal(Node n) {
    return switch (n) {
      case Literal lit -> true;
      case Charset charset -> true;
      default -> false;
    };
  }
}
