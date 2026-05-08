package transformation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import peg.PegGrammar;
import peg.node.Ident;
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
      List<String> possibleSkipRules =
          List.of(
              "WS",
              "WHITESPACE",
              "COMMENT",
              "LINE_COMMENT",
              "WHITESPACES",
              "DOC_COMMENT",
              "NEWLINE",
              "SPACE",
              "WHITE_SPACE",
              "SingleLineComment",
              "MultiLineComment");
      List<Node> foundSkipRules = new ArrayList<>();
      for (Rule r : this.grammar.getRules()) {
        if (possibleSkipRules.contains(r.name())) {
          foundSkipRules.add(grammar.mkIdent(r.name()));
        }
      }
      if (foundSkipRules.isEmpty()) return;

      Node rhs =
          grammar.mkTerm(grammar.mkOrderedChoice(foundSkipRules), Optional.of(Operator.PLUS));

      Rule r = grammar.mkParsingRule("SKIP_", rhs);

      grammar.getOptions().skipRules =
          Optional.of(
              foundSkipRules.stream().map(skipR -> (Ident) skipR).map(i -> i.name()).toList());

      grammar.addRule(r);

      return;
    }
    ;

    List<Node> skipRules =
        grammar.getOptions().skipRules.get().stream()
            .map(grammar::mkIdent)
            .map(i -> (Node) i)
            .collect(Collectors.toList());
    Node rhs = grammar.mkTerm(grammar.mkOrderedChoice(skipRules), Optional.of(Operator.PLUS));

    Rule r = grammar.mkParsingRule("SKIP_", rhs);

    grammar.addRule(r);
  }
}
