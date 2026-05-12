package transformation;

import java.util.Optional;
import peg.PegGrammar;
import peg.node.Literal;
import peg.node.Node;
import peg.node.Rule;

public class AppendWordSuffix implements RuleTransformation {

  private final PegGrammar grammar;

  public AppendWordSuffix(PegGrammar grammar) {
    this.grammar = grammar;
  }

  @Override
  public Rule apply(Rule rule) {
    if (grammar.isLexicalRule(rule) && isWordLiteral(rule)) {
      // Node wordChar =
      //     grammar.mkOrderedChoice(
      //         grammar.mkCharset(
      //             grammar.mkRange("a", "z"),
      //             grammar.mkRange("0", "9"),
      //             grammar.mkCharsetLiteral("_")));
      Node idRestRef = grammar.mkIdent("_idRest");
      Node wordBoundary = grammar.mkNot(idRestRef, false);
      Node newRhs = grammar.mkSequence(rule.rhs(), wordBoundary);
      return new Rule(rule.name(), newRhs, rule.kind());
    }
    return rule;
  }

  private Optional<String> extractLiteral(Rule rule) {
    return switch (rule.rhs()) {
      case Literal literal ->
          Optional.of(literal.content().substring(1, literal.content().length() - 1));
      default -> Optional.empty();
    };
  }

  private boolean isWordLiteral(Rule rule) {
    Optional<String> lit = extractLiteral(rule);
    if (lit.isEmpty()) return false;
    String content = lit.get();
    for (char c : content.toCharArray()) {
      if (!Character.isLetterOrDigit(c) && c != '_') return false;
    }
    return Character.isLetter(content.charAt(0));
  }
}
