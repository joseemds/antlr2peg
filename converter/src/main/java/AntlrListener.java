import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.TerminalNode;

/**
 * AntlrListener
 */
public class AntlrListener extends ANTLRv4ParserBaseListener{

	@Override
	public void exitRuleBlock(ANTLRv4Parser.RuleBlockContext ctx) {
		System.out.println(ctx);
	}

}
