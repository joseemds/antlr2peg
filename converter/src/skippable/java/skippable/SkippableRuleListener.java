package skippable;

import java.util.HashSet;
import java.util.Set;
import converter.ANTLRv4ParserBaseListener;
import converter.ANTLRv4Parser;
import converter.ANTLRv4Lexer;

public class SkippableRuleListener extends ANTLRv4ParserBaseListener {

  private final Set<String> skippableRules = new HashSet<>();
  private boolean skipCurrentRule = false;
  private String currentRuleName = null;

  public Set<String> getSkippableRules() {
    return skippableRules;
  }

  @Override
  public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
    currentRuleName = ctx.TOKEN_REF().getText();

    if (skipCurrentRule) {
      skippableRules.add(currentRuleName);
    }

    skipCurrentRule = false;
    currentRuleName = null;
  }

  @Override
  public void exitLexerCommand(ANTLRv4Parser.LexerCommandContext ctx) {
    String cmd = ctx.lexerCommandName().getText().toLowerCase();
    if (cmd.equals("skip")) {
      skipCurrentRule = true;
    } else if (cmd.equals("channel") && ctx.lexerCommandExpr() != null) {
      String arg = ctx.lexerCommandExpr().getText();
      if (arg.equals("HIDDEN") || arg.equals("1")) {
        skipCurrentRule = true;
      }
    }
  }
}
