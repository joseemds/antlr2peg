package transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import peg.PegGrammar;
import peg.node.And;
import peg.node.Ident;
import peg.node.Node;
import peg.node.OrderedChoice;
import peg.node.Rule;
import peg.node.Sequence;
import peg.node.Term;

public class FixRepetitions implements RuleTransformation {
  private final PegGrammar grammar;
  private static volatile int counter = 0;
  private final ArrayList<Rule> newRules = new ArrayList<>();
  private final Map<String, Set<Node>> followsSets;

  public FixRepetitions(PegGrammar grammar) {
    this.grammar = grammar;
    this.followsSets = grammar.getFollows();
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
    // System.out.printf("Follow %s = %s\n",rule.name() ,followsSets.get(rule.name()));
    return new Rule(rule.name(), fix(rule.rhs(), rule.name()), rule.kind());
  }

  public Node fix(Node node, String parentRule) {
    return switch (node) {
      case Sequence seq -> fixSequence(seq, parentRule);
      case OrderedChoice oc -> {
        for (Node n : oc.nodes()) {
          fix(n, parentRule);
        }
        ;

        yield oc;
      }
      case Term term -> fixTerm(term, parentRule);
      default -> node;
    };
  }

  private Node fixTerm(Term term, String parentRule) {
    if (term.op().isEmpty()) return term;

    var pFirst = grammar.firstOf(term.node());
    var repFollow = calculateFollow(term, parentRule);
    boolean hasIntersection = !Collections.disjoint(pFirst, repFollow);

    if (hasIntersection) {
      String newRuleName = genName();
      Node newNode = fixRepetition(term, pFirst, new ArrayList<>(repFollow), newRuleName);
      Rule r = new Rule(newRuleName, newNode);
      addRule(r, term);
      return new Ident(newRuleName);
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

        Set<Node> followOfTerm = calculateFollow(term, parentRule);

        boolean hasIntersection = !Collections.disjoint(firstOfBody, new ArrayList<>(followOfTerm));

        if (hasIntersection) {
          String ruleName = this.genName();
          Node newNode =
              fixRepetition(term, firstOfBody, new ArrayList<Node>(followOfTerm), ruleName);
          Rule r = grammar.mkParsingRule(ruleName, newNode);
          addRule(r, seq);

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
        Node lhs = grammar.mkSequence(t.node(), id);
        Node resultNode = grammar.mkOrderedChoice(List.of(lhs, termFollowSeq));
        yield resultNode;
      }
      case PLUS -> {
        Node termFollowSeq = grammar.mkSequence(t.node(), grammar.mkOrderedChoice(termFollow));
        Node lhs = grammar.mkSequence(t.node(), id);
        Node fixedNode = new And(termFollowSeq);
        Node resultNode = grammar.mkOrderedChoice(lhs, fixedNode);
        yield resultNode;
      }
      case STAR -> {
        Node termFollowSeq = grammar.mkOrderedChoice(termFollow);
        Node lhs = grammar.mkSequence(List.of(t.node(), id));
        Node fixedNode = grammar.mkSequence(List.of(lhs, new And(termFollowSeq)));
        Node resultNode = grammar.mkOrderedChoice(List.of(fixedNode, grammar.mkEmpty()));
        yield resultNode;
      }
    };
  }

  private Set<Node> calculateFollow(Term term, String parentRule) {
    if (term.node() instanceof Ident ident) {
      Rule r = grammar.findRuleByName(ident.name());
      if(!grammar.isSyntacticRule(r)) return followsSets.get(parentRule);
      return followsSets.get(ident.name());
    }
    
    var result = followsSets.get(parentRule);
    return result;
  }
}
