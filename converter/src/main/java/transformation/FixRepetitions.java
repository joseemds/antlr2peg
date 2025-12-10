package transformation;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


import peg.PegGrammar;
import peg.node.And;
import peg.node.Node;
import peg.node.Sequence;
import peg.node.Term;
import peg.node.Operator;
import peg.node.OrderedChoice;
import peg.node.Rule;

public class FixRepetitions implements Transformation {
    private final PegGrammar grammar;
    private static volatile int counter = 0;
    private final ArrayList<Rule> newRules = new ArrayList<>();

    public FixRepetitions(PegGrammar grammar) {
        this.grammar = grammar;
    }


    private String genName(){
     return "FixedRepetition_" + counter++;
   }

    private void addRule(Rule rule) {
        System.out.printf("Rule added: \n %s\n", rule);
        newRules.add(rule);
    }

    @Override
    public Node apply(Node node) {
        return switch (node) {
            case Sequence seq -> fixSequence(seq);
            case Term term -> term;
            default -> node;
        };
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
                    addRule(r);
                    
                } else {
                    newChildren.add(term);
                }
            } else {
                newChildren.add(current);
            }
        }

        return grammar.mkSequence(newChildren);
    }
}
