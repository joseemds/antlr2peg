package transformation;

import java.util.Comparator;
import java.util.List;
import peg.KeywordCollector;
import peg.PegGrammar;
import peg.node.Node;
import peg.node.Rule;

public class PrependKeywords implements RuleTransformation {

  private final List<String> POSSIBLE_RULE_NAME = List.of("ID", "ID_", "IDENTIFIER", "IDENT");
  private final PegGrammar grammar;
  private final List<String> keywords;

  public PrependKeywords(PegGrammar grammar) {
    KeywordCollector keywordsCollector = new KeywordCollector(grammar);
    this.grammar = grammar;
    this.keywords = keywordsCollector.collectKeywords();
    System.out.println("Here with keywords" + keywords);
    if (!this.keywords.isEmpty()) {
      this.addKeywordRule();
    }
  }

  @Override
  public Rule apply(Rule rule) {
    if (POSSIBLE_RULE_NAME.contains(rule.name().toUpperCase()) && grammar.isLexicalRule(rule)) {

      if (keywords.isEmpty()) {
        return rule;
      }

      Node keywordsRef = this.grammar.mkIdent("_keywords");
      Node notKeywords = this.grammar.mkNot(keywordsRef, false);
      Node newRhs = this.grammar.mkSequence(notKeywords, rule.rhs());
      return new Rule(rule.name(), newRhs, rule.kind());
    }

    return rule;
  }

  private void addKeywordRule() {
    List<Node> keywordsNode =
        keywords.stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .map(grammar::mkLiteral)
            .map(l -> (Node) l)
            .toList();

    Node keywordChoice = this.grammar.mkOrderedChoice(keywordsNode);
    Rule keywordsRule = grammar.mkParsingRule("_keywords", keywordChoice);

    this.grammar.addRule(keywordsRule);
  }
}
