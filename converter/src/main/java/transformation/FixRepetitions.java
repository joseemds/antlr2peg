package transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


import peg.PegGrammar;
import peg.node.And;
import peg.node.Ident;
import peg.node.Node;
import peg.node.OrderedChoice;
import peg.node.Rule;
import peg.node.Sequence;
import peg.node.Term;

public class FixRepetitions implements Transformation {
  private final PegGrammar grammar;
  private static volatile int counter = 0;
  private final ArrayList<Rule> newRules = new ArrayList<>();

  public FixRepetitions(PegGrammar grammar) {
    this.grammar = grammar;
  }

  private String genName() {
    return "FixedRepetition_" + counter++;
  }

  private void addRule(Rule rule, Node context) {
    System.out.printf("FixedRepetition: Rule added for %s: \n %s\n", context, rule);
    newRules.add(rule);
  }

  @Override
  public Node apply(Node node) {
    return switch (node) {
      case Sequence seq -> fixSequence(seq);
      case OrderedChoice oc -> {
        for (Node n : oc.nodes()) {
          apply(n);
        }
        ;

        yield oc;
      }
      case Term term -> fixTerm(term);
      default -> node;
    };
  }

  private Node fixTerm(Term term) {
    List<Node> newChildren = new ArrayList<>();
    if (term.op().isEmpty()) return term;

    var pFirst = grammar.firstOf(term.node());
    var repFollow = grammar.localFollowOf(term.node(), term);
    boolean hasIntersection = !Collections.disjoint(pFirst, repFollow);

    if (hasIntersection) {
      
      String newRuleName = genName();
      Node newNode = fixRepetition(term, pFirst, new ArrayList<>(repFollow), newRuleName);
      Rule r = new Rule(newRuleName, newNode);
      addRule(r, term);
    } else {
      newChildren.add(term);
    }

    return term;
  }

  private Node fixSequence(Sequence seq) {
    List<Node> newChildren = new ArrayList<>();
    List<Node> currentChildren = seq.nodes();

    for (int i = 0; i < currentChildren.size(); i++) {
      Node current = currentChildren.get(i);

      if (current instanceof Term term && term.op().isPresent()) {

        List<Node> firstOfBody = grammar.firstOf(term.node());

        List<Node> followSet;
        if (i + 1 < currentChildren.size()) {
          followSet = grammar.firstOf(currentChildren.get(i + 1));
        } else {
          followSet = Collections.emptyList();
        }

        boolean hasIntersection = !Collections.disjoint(firstOfBody, followSet);

        if (hasIntersection) {
          String ruleName = this.genName();
          var followOfTerm = grammar.mkSequence(followSet);
          var recursiveSeq = grammar.mkSequence(List.of(term.node(), grammar.mkIdent(ruleName)));
          List<Node> newList = List.of(recursiveSeq, new And(followOfTerm));
          var oc = grammar.mkOrderedChoice(newList);
          newChildren.add(grammar.mkIdent(ruleName));
          Rule r = grammar.mkParsingRule(ruleName, oc);
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
        Node termFollowSeq = grammar.mkSequence(termFollow);
        Node lhs = grammar.mkSequence(List.of(t.node(), id));
        Node resultNode = grammar.mkOrderedChoice(List.of(lhs, termFollowSeq));
        yield resultNode;
      }
      case PLUS -> {
        termFollow.addFirst(t.node());
        Node termFollowSeq = grammar.mkSequence(termFollow);
        Node lhs = grammar.mkSequence(List.of(t.node(), id));
        Node fixedNode = new And(termFollowSeq);
        Node resultNode = grammar.mkOrderedChoice(List.of(lhs, fixedNode));
        yield resultNode;
      }
      case STAR -> {
        Node termFollowSeq = grammar.mkSequence(termFollow);
        Node lhs = grammar.mkSequence(List.of(t.node(), id));
        Node fixedNode = grammar.mkSequence(List.of(lhs, new And(termFollowSeq)));
        Node resultNode = grammar.mkOrderedChoice(List.of(fixedNode, grammar.mkEmpty()));
        yield resultNode;
      }
    };
  }
}
