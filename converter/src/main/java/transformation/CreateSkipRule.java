package transformation;

import java.util.List;
import java.util.stream.Collectors;
import peg.PegGrammar;
import peg.node.Node;
import peg.node.Operator;
import peg.node.Rule;

public class CreateSkipRule {

  private final PegGrammar grammar;

  public CreateSkipRule(PegGrammar grammar) {
    this.grammar = grammar;
    addSkipRule();
  }

  public void addSkipRule() {
    if (grammar.getOptions().skipRules.isEmpty()) {
      return;
    }

    List<Node> skipRules =
        grammar.getOptions().skipRules.stream()
            .map(grammar::mkIdent)
            .map(i -> (Node) i)
            .collect(Collectors.toList());
    Node rhs = grammar.mkRepetition(grammar.mkOrderedChoice(skipRules), Operator.PLUS);

    Rule r = grammar.mkParsingRule("SKIP_", rhs);

    grammar.addRule(r);
  }
}
