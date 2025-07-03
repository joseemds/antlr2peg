// Generated from antlr/abnf/abnf.g4 by ANTLR 4.13.2
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link abnfParser}.
 */
public interface abnfListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link abnfParser#rulelist}.
	 * @param ctx the parse tree
	 */
	void enterRulelist(abnfParser.RulelistContext ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#rulelist}.
	 * @param ctx the parse tree
	 */
	void exitRulelist(abnfParser.RulelistContext ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#rule_}.
	 * @param ctx the parse tree
	 */
	void enterRule_(abnfParser.Rule_Context ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#rule_}.
	 * @param ctx the parse tree
	 */
	void exitRule_(abnfParser.Rule_Context ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#elements}.
	 * @param ctx the parse tree
	 */
	void enterElements(abnfParser.ElementsContext ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#elements}.
	 * @param ctx the parse tree
	 */
	void exitElements(abnfParser.ElementsContext ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#alternation}.
	 * @param ctx the parse tree
	 */
	void enterAlternation(abnfParser.AlternationContext ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#alternation}.
	 * @param ctx the parse tree
	 */
	void exitAlternation(abnfParser.AlternationContext ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#concatenation}.
	 * @param ctx the parse tree
	 */
	void enterConcatenation(abnfParser.ConcatenationContext ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#concatenation}.
	 * @param ctx the parse tree
	 */
	void exitConcatenation(abnfParser.ConcatenationContext ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#repetition}.
	 * @param ctx the parse tree
	 */
	void enterRepetition(abnfParser.RepetitionContext ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#repetition}.
	 * @param ctx the parse tree
	 */
	void exitRepetition(abnfParser.RepetitionContext ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#repeat_}.
	 * @param ctx the parse tree
	 */
	void enterRepeat_(abnfParser.Repeat_Context ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#repeat_}.
	 * @param ctx the parse tree
	 */
	void exitRepeat_(abnfParser.Repeat_Context ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#element}.
	 * @param ctx the parse tree
	 */
	void enterElement(abnfParser.ElementContext ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#element}.
	 * @param ctx the parse tree
	 */
	void exitElement(abnfParser.ElementContext ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#group}.
	 * @param ctx the parse tree
	 */
	void enterGroup(abnfParser.GroupContext ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#group}.
	 * @param ctx the parse tree
	 */
	void exitGroup(abnfParser.GroupContext ctx);
	/**
	 * Enter a parse tree produced by {@link abnfParser#option}.
	 * @param ctx the parse tree
	 */
	void enterOption(abnfParser.OptionContext ctx);
	/**
	 * Exit a parse tree produced by {@link abnfParser#option}.
	 * @param ctx the parse tree
	 */
	void exitOption(abnfParser.OptionContext ctx);
}