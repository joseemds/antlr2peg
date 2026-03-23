package transformation;

import java.util.List;
import peg.KeywordCollector;
import peg.PegGrammar;
import peg.node.Node;
import peg.node.Rule;

public class AppendKeywords implements RuleTransformation {

  private final List<String> POSSIBLE_RULE_NAME = List.of("ID", "ID_", "IDENTIFIER", "IDENT");
  private final KeywordCollector keywordsCollector;
  private final PegGrammar grammar;

  public AppendKeywords(PegGrammar grammar) {
    this.keywordsCollector = new KeywordCollector(grammar);
    this.grammar = grammar;
  }

  @Override
  public Rule apply(Rule rule) {
    if (POSSIBLE_RULE_NAME.contains(rule.name().toUpperCase()) && grammar.isLexicalRule(rule)) {

      List<String> keywords = keywordsCollector.collectKeywords();
      if (keywords.isEmpty()) {
        return rule;
      }
      List<Node> possibleKeywords =
          keywords.stream().map(grammar::mkLiteral).map(l -> (Node) l).toList();

      Node keywordChoices = this.grammar.mkOrderedChoice(possibleKeywords);
      Node notKeywords = this.grammar.mkNot(keywordChoices, false);
      Node newRhs = this.grammar.mkSequence(notKeywords, rule.rhs());
      return new Rule(rule.name(), newRhs, rule.kind());
    }

    return rule;
  }
}
