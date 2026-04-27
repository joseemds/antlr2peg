package transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import peg.PegGrammar;
import peg.node.And;
import peg.node.Empty;
import peg.node.Ident;
import peg.node.Node;
import peg.node.OrderedChoice;
import peg.node.Rule;
import peg.node.Sequence;
import peg.node.Term;
import utils.StatsTracker;

public class FixRepetitions implements RuleTransformation {
  private final PegGrammar grammar;
  private static volatile int counter = 0;
  private final ArrayList<Rule> newRules = new ArrayList<>();
  private final Map<String, Set<Node>> followsSets;
  private final Map<String, String> fixedRepetition = new HashMap<>();
  private StatsTracker statsTracker;

  public FixRepetitions(PegGrammar grammar, StatsTracker tracker) {
    this.grammar = grammar;
    this.followsSets = grammar.getFollows();
    this.statsTracker = tracker;
  }

  private String genName() {
    return "FixedRepetition_" + counter++;
  }

  public List<Rule> getNewRules() {
    return this.newRules;
  }

  private void addRule(Rule rule, Node context) {
    System.out.printf("FixedRepetition: Rule added for %s: \n %s\n", context, rule);
    newRules.add(rule);
  }

  @Override
  public Rule apply(Rule rule) {
    if (!grammar.isSyntacticRule(rule)) return rule;
    return new Rule(rule.name(), fix(rule.rhs(), rule.name()), rule.kind());
  }

  public Node fix(Node node, String parentRule) {
    return switch (node) {
      case Sequence seq -> fixSequence(seq, parentRule);
      case OrderedChoice oc -> {
        for (Node n : oc.nodes()) {
          fix(n, parentRule);
        }
        yield oc;
      }

      case Term term -> fixTerm(term, parentRule, List.of());
      default -> node;
    };
  }

  private String createKey(Term term, Set<Node> followOfTerm) {
    char[] chars = followOfTerm.toString().toCharArray();
    Arrays.sort(chars);
    return term + "|" + new String(chars);
  }

  private String getOrCreateFixedRule(
      Term term, List<Node> firstOfBody, Set<Node> followOfTerm, Node context) {
    String cacheKey = createKey(term, followOfTerm);
    if (fixedRepetition.containsKey(cacheKey)) {
      return fixedRepetition.get(cacheKey);
    }

    String ruleName = genName();
    fixedRepetition.put(cacheKey, ruleName);
    Node newNode = fixRepetition(term, firstOfBody, new ArrayList<>(followOfTerm), ruleName);
    statsTracker.bumpRepetitionsTransformed();
    Rule r = grammar.mkParsingRule(ruleName, newNode);
    addRule(r, context);
    return ruleName;
  }

  private Node fixTerm(Term term, String parentRule, List<Node> tailNodes) {
    if (term.op().isEmpty()) return term;

    var pFirst = grammar.firstOf(term.node());
    var repFollow = calculateFollow(term, parentRule, tailNodes);
    boolean hasIntersection = !Collections.disjoint(pFirst, repFollow);

    if (hasIntersection) {
      return new Ident(getOrCreateFixedRule(term, pFirst, repFollow, term));
    }
    return term;
  }

  private Node fixSequence(Sequence seq, String parentRule) {
    List<Node> newChildren = new ArrayList<>();
    List<Node> currentChildren = seq.nodes();

    for (int i = 0; i < currentChildren.size(); i++) {
      Node current = currentChildren.get(i);

      if (current instanceof Term term && term.op().isPresent()) {

        List<Node> firstOfBody = grammar.firstOf(term.node());

        List<Node> tail = currentChildren.subList(i + 1, currentChildren.size());
        Set<Node> followOfTerm = calculateFollow(term, parentRule, tail);

        boolean hasIntersection = !Collections.disjoint(firstOfBody, new ArrayList<>(followOfTerm));

        if (hasIntersection) {
          newChildren.add(
              grammar.mkIdent(getOrCreateFixedRule(term, firstOfBody, followOfTerm, seq)));
        } else {
          newChildren.add(term);
        }
      } else {
        newChildren.add(current);
      }
    }

    return grammar.mkSequence(newChildren);
  }

  private Node fixRepetition(Term t, List<Node> termFirst, List<Node> termFollow, String rulename) {
    if (t.op().isEmpty())
      throw new IllegalStateException("term should have an operator when called here");
    Ident id = new Ident(rulename);
    return switch (t.op().get()) {
      case OPTIONAL -> {
        Node termFollowSeq = grammar.mkOrderedChoice(termFollow);
        Node lhs = grammar.mkSequence(t.node(), grammar.mkAnd(termFollowSeq));
        Node resultNode = grammar.mkOrderedChoice(lhs, grammar.mkEmpty());
        yield resultNode;
      }
      case PLUS -> {
        termFollow.add(t.node());
        Node termFollowSeq = grammar.mkOrderedChoice(termFollow);
        Node lhs = grammar.mkSequence(t.node(), id);
        Node fixedNode = new And(termFollowSeq);
        Node resultNode = grammar.mkOrderedChoice(lhs, fixedNode);
        yield resultNode;
      }
      case STAR -> {
        Node termFollowSeq = grammar.mkOrderedChoice(termFollow);
        Node lhs = grammar.mkSequence(t.node(), id);
        Node resultNode = grammar.mkOrderedChoice(lhs, grammar.mkAnd(termFollowSeq));
        yield resultNode;
      }
    };
  }

  private Set<Node> calculateFollow(Term term, String parentRule, List<Node> tailNodes) {
    Set<Node> parentFollow = followsSets.getOrDefault(parentRule, Set.of());

    if (tailNodes.isEmpty()) {
      if (term.node() instanceof Ident ident) {
        Rule r = grammar.findRuleByName(ident.name());
        if (grammar.isSyntacticRule(r)) {
          return followsSets.getOrDefault(ident.name(), parentFollow);
        }
      }
      return parentFollow;
    }

    Set<Node> follow = new HashSet<>(firstOfTail(tailNodes));
    follow.remove(new peg.node.Empty()); // strip ε — we add parentFollow explicitly below

    boolean tailNullable = tailNodes.stream().allMatch(grammar::isPossiblyEmpty);
    if (tailNullable) {
      follow.addAll(parentFollow);
    }

    return follow;
  }

  private Set<Node> firstOfTail(List<Node> nodes) {
    Set<Node> result = new HashSet<>();
    for (Node n : nodes) {
      List<peg.node.Node> fi = grammar.firstOf(n);
      fi.stream().filter(x -> !(x instanceof Empty)).forEach(result::add);
      if (!grammar.isPossiblyEmpty(n)) {
        return result;
      }
    }
    result.add(new Empty());
    return result;
  }
}
