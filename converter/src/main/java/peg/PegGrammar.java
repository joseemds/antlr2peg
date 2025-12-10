package peg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

  public Map<String, Set<Node>> getFollows() {
    return this.followSets;
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
            .collect(Collectors.toCollection(ArrayList::new));
    return this;
  }

  public void computeFirst() {
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
    List<Node> result = new ArrayList<>();

    switch (node) {
      case Literal lit -> result.add(lit);
      case Charset cs -> result.add(cs);
      case Wildcard w -> result.add(w);
      case Empty e -> result.add(e);
      case EOF eof -> result.add(eof);
      case Not n -> result.add(new Empty());
      case And and -> {}
      case Ident ident -> {
        Rule r = findRuleByName(ident.name());
        if (!isSyntacticRule(r)) {
          result.add(ident);
        } else {
          result.addAll(firstSets.getOrDefault(ident.name(), Set.of()));
        }
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
 

public Set<Node> localFollowOf(Node target, Node parent) {
    Set<Node> result = new HashSet<>();

    switch (parent) {
        case Sequence seq -> {
            List<Node> nodes = seq.nodes();
            int i = nodes.indexOf(target);

            if (i >= 0) {
                for (int j = i + 1; j < nodes.size(); j++) {
                    Node next = nodes.get(j);
                    List<Node> nextFirst = firstOf(next);

                    nextFirst.stream()
                             .filter(n -> !(n instanceof Empty))
                             .forEach(result::add);

                    if (!isPossiblyEmpty(next)) {
                        return result; 
                    }
                }

                result.add(new Empty());
                return result;
            }

            for (Node child : nodes) {
                result.addAll(localFollowOf(target, child));
            }
        }

        case OrderedChoice oc -> {
            for (Node alt : oc.nodes()) {
                result.addAll(localFollowOf(target, alt));
            }
        }

        case Term t -> {
            if (t.node() == target) {
                if (t.op().isPresent()) {
                    switch (t.op().get()) {
                        case STAR, PLUS -> {
                            var first = firstOf(target);
                            first.stream()
                                 .filter(n -> !(n instanceof Empty))
                                 .forEach(result::add);
                            
                            result.add(new Empty()); 
                        }
                        case OPTIONAL -> {
                            result.add(new Empty());
                        }
                    }
                } else {
                     result.add(new Empty());
                }
                return result;
            }

            result.addAll(localFollowOf(target, t.node()));
        }

        default -> {}
    }
    return result;
}

  public void computeFollowSets() {
    for (Rule r : rules) {
      if (!isSyntacticRule(r)) continue;
      followSets.putIfAbsent(r.name(), new HashSet<>());
    }

    if (!rules.isEmpty()) {
      followSets.get(rules.get(0).name()).add(new EOF());
    }

    boolean changed;

    do {
      changed = false;

      for (Rule rule : rules) {
        if (!isSyntacticRule(rule)) continue;
        String ruleName = rule.name();
        Node rhs = rule.rhs();

        changed |= propagateFollow(rhs, followSets.get(ruleName));
      }

    } while (changed);
  }

  private boolean propagateFollow(Node node, Set<Node> followOfParent) {
    boolean changed = false;

    switch (node) {
      case Ident id -> {
        Rule r = findRuleByName(id.name());
        if (!isSyntacticRule(r)) {
          break;
        }
        Set<Node> idFollows = followSets.computeIfAbsent(id.name(), k -> new HashSet<>());
        changed |= idFollows.addAll(followOfParent);
      }
      case Sequence seq -> {
        List<Node> nodes = seq.nodes();
        for (int i = 0; i < nodes.size(); i++) {
          Node current = nodes.get(i);
          Set<Node> currFollow = new HashSet<>();

          boolean restIsNullable = true;
          for (int j = i + 1; j < nodes.size(); j++) {
            Node next = nodes.get(j);
            List<Node> nextFirst = firstOf(next);
            currFollow.addAll(nextFirst.stream().filter(x -> !(x instanceof Empty)).toList());

            if (!isPossiblyEmpty(next)) {
              restIsNullable = false;
              break;
            }
          }

          if (restIsNullable) {
            currFollow.addAll(followOfParent);
          }

          changed |= propagateFollow(current, currFollow);
        }
      }
      case OrderedChoice oc -> {
        for (Node alternative : oc.nodes()) {
          changed |= propagateFollow(alternative, followOfParent);
        }
      }
      case Term t -> {
        Set<Node> innerFollow = new HashSet<>(followOfParent);
        if (t.op().isPresent()) {
          switch (t.op().get()) {
            case STAR, PLUS -> {
              List<Node> selfFirst = firstOf(t.node());
              innerFollow.addAll(selfFirst.stream().filter(x -> !(x instanceof Empty)).toList());
            }
            case OPTIONAL -> {}
          }
        }
        changed |= propagateFollow(t.node(), innerFollow);
      }
        // NO-OPs
      case Not not -> {}
      case And and -> {}
      case Literal lit -> {}
      case Charset cs -> {}
      case Empty e -> {}
      case Wildcard w -> {}
      case EOF e -> {}
    }

    return changed;
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
      case And and -> true;
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
