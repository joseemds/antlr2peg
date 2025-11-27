package transformation;

import java.util.Map;
import java.util.Set;
import peg.PegGrammar;
import peg.node.Charset;
import peg.node.EOF;
import peg.node.Empty;
import peg.node.Ident;
import peg.node.Literal;
import peg.node.Node;
import peg.node.Not;
import peg.node.OrderedChoice;
import peg.node.Sequence;
import peg.node.Term;
import peg.node.Wildcard;

public class RemoveFirstFollowConflict implements Transformation {
  private final Map<String, Set<Node>> firstSets;
  private final Map<String, Set<Node>> followSets;

  public RemoveFirstFollowConflict(PegGrammar grammar) {
    this.followSets = grammar.getFirsts();
    this.firstSets = grammar.getFirsts();
  }

  /** FIRST(X) ^ FOLLOW(X) != empty -> */
  public Node apply(Node node) {
    switch (node) {
      case Term term -> {}
      case Ident ident -> {}
      case Sequence s -> {}
      case OrderedChoice oc -> {}
      case Literal lit -> {}
      case Charset cs -> {}
      case Not n -> {}
      case Empty e -> {}
      case Wildcard w -> {}
      case EOF e -> {}
    }
    ;

    return node;
  }
}
