import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;

import peg.Node;
import peg.PegAst;


public class AntlrToPegListener extends ANTLRv4ParserBaseListener{

	private StringBuilder out = new StringBuilder();
	private final PegAst ast = new PegAst();
	private ParseTreeProperty<Node> properties = new ParseTreeProperty<>();

	private void copyNode(ParserRuleContext parent, ParserRuleContext child){
		properties.put(parent, properties.get(child));
	}

	private List<Node> mkNodeList(List<? extends ParserRuleContext> ctxs){
		List<Node> nodes = new ArrayList<>();
		for (var elem : ctxs) {
			nodes.add(properties.get(elem));
		}

		return nodes;
	}


	public void printBuf(){
		System.out.println(this.out.toString());
	}


	@Override
	public void exitRules(ANTLRv4Parser.RulesContext ctx){
	}



	public void exitRuleSpec(ANTLRv4Parser.RuleSpecContext ctx){
		
	}


	public void exitRuleBlock(ANTLRv4Parser.RuleBlockContext ctx){
		copyNode(ctx, ctx.ruleAltList());
	}


	@Override
	public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx){
		StringBuilder buf = new StringBuilder();
		var ident = ctx.RULE_REF().getText();
		var rhs = properties.get(ctx.ruleBlock());
		properties.put(ctx, ast.mkRule(ident, rhs));
	}

	@Override
	public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx){
		// TODO
	}

	@Override
	public void exitLexerRuleBlock(ANTLRv4Parser.LexerRuleBlockContext ctx){
		// StringBuilder buf = new StringBuilder();
		// buf.append(parseTreeProperty.get(ctx.lexerAltList()));
		// parseTreeProperty.put(ctx, buf.toString());
	}


	@Override
	public void exitLexerAltList(ANTLRv4Parser.LexerAltListContext ctx) {
			// StringBuilder buf = new StringBuilder();
			// List<String> altList = new ArrayList<>();
			// for(var lexerAlt : ctx.lexerAlt()){
			// 	altList.add(parseTreeProperty.get(lexerAlt));
			// }
			//
			// String alts = String.join(" / ", altList);
			// buf.append(alts);
			// parseTreeProperty.put(ctx, buf.toString());
	}
	 

	@Override
	public void exitLexerAlt(ANTLRv4Parser.LexerAltContext ctx) {
		/* parseTreeProperty.put(ctx, parseTreeProperty.get(ctx.lexerElements())); */
	}

	@Override
	public void exitLexerElements(ANTLRv4Parser.LexerElementsContext ctx) {
			// StringBuilder buf = new StringBuilder();
			// var lexerElements = ctx.lexerElement();
			// List<String> altList = new ArrayList<>();
			// for(var lexerEl : lexerElements){
			// 	altList.add(parseTreeProperty.get(lexerEl));
			// }
			//
			// String alts = String.join(" ", altList);
			// buf.append(alts);
			// parseTreeProperty.put(ctx, buf.toString());
	}

	@Override
	public void exitLexerElement(ANTLRv4Parser.LexerElementContext ctx) {
		// StringBuilder buf = new StringBuilder();
		// String suffix = "";
		// if (ctx.ebnfSuffix() != null){
		// 	suffix = ctx.ebnfSuffix().getText();
		// }
		// if(ctx.lexerAtom() != null) {
		// 	parseTreeProperty.put(ctx, parseTreeProperty.get(ctx.lexerAtom()) + suffix);
		//
		//
		// } else if(ctx.lexerBlock() != null) {
		// 	var blockCtx = ctx.lexerBlock();
		// 	List<String> altList = new ArrayList<>();
		// 	for(var lexerAlt : blockCtx.lexerAltList().lexerAlt()){
		// 		altList.add(parseTreeProperty.get(lexerAlt));
		// 	}
		//
		// 	String alts = String.join(" / ", altList);
		// 	buf.append("(");
		// 	buf.append(alts);
		// 	buf.append(")");
		// 	buf.append(suffix);
		// 	parseTreeProperty.put(ctx, buf.toString());
		// } else if(ctx.actionBlock() != null){}
	}


	@Override
	public void exitLexerAtom(ANTLRv4Parser.LexerAtomContext ctx){
		// StringBuilder buf = new StringBuilder();
		// if(ctx.characterRange() != null ){
		// 	parseTreeProperty.put(ctx, parseTreeProperty.get(ctx.characterRange()));
		// } else if(ctx.LEXER_CHAR_SET() != null){
		// 	parseTreeProperty.put(ctx, ctx.LEXER_CHAR_SET().getText());
		// } else if(ctx.terminalDef() != null){
		// 	parseTreeProperty.put(ctx, ctx.terminalDef().getText());
		// } else if(ctx.notSet() != null){
		// 	buf.append(parseTreeProperty.get(ctx.notSet()));
		//
		// 	parseTreeProperty.put(ctx, buf.toString());
		// } else if(ctx.wildcard() != null) {
		// 	parseTreeProperty.put(ctx, ctx.wildcard().getText());
		// }
	}


	@Override
	public void exitNotSet(ANTLRv4Parser.NotSetContext ctx){
		// if(ctx.setElement() != null){
		// 	parseTreeProperty.put(ctx, ctx.NOT().getText() + parseTreeProperty.get(ctx.setElement()));
		// } else if (ctx.blockSet() != null) {
		// 	parseTreeProperty.put(ctx, ctx.NOT().getText() + parseTreeProperty.get(ctx.blockSet()));
		// }
	}

	@Override
	public void exitBlockSet(ANTLRv4Parser.BlockSetContext ctx){
		// StringBuilder buf = new StringBuilder();
		// buf.append("(");
		// List<String> setElements = new ArrayList<>();
		// for(var setEl : ctx.setElement()){
		// 	setElements.add(parseTreeProperty.get(setEl));
		// }
		//
		// buf.append(String.join(" ", setElements));
		// buf.append(")");
		//
		// parseTreeProperty.put(ctx, buf.toString());

	}


	@Override
	public void exitSetElement(ANTLRv4Parser.SetElementContext ctx){
		// if(ctx.TOKEN_REF() != null){
		// 	parseTreeProperty.put(ctx, ctx.TOKEN_REF().getText());
		// 	
		// } else if (ctx.STRING_LITERAL() != null){
		// 	parseTreeProperty.put(ctx, ctx.STRING_LITERAL().getText());
		//
		// } else if (ctx.characterRange() != null){
		// 	parseTreeProperty.put(ctx, parseTreeProperty.get(ctx.characterRange()));
		//
		// } else if (ctx.LEXER_CHAR_SET() != null){
		// 	parseTreeProperty.put(ctx, ctx.LEXER_CHAR_SET().getText());
		// }
	}


	@Override
	public void exitCharacterRange(ANTLRv4Parser.CharacterRangeContext ctx){
		// StringBuilder buf = new StringBuilder();
		// buf.append(ctx.STRING_LITERAL(0).getText());
		// buf.append(ctx.RANGE().getText());
		// buf.append(ctx.STRING_LITERAL(1).getText());
		// parseTreeProperty.put(ctx, buf.toString());
	}


	@Override
	public void exitRuleAltList(ANTLRv4Parser.RuleAltListContext ctx){
		List<ANTLRv4Parser.LabeledAltContext> alts = ctx.labeledAlt();
		List<Node> nodes = mkNodeList(alts);
		var node = ast.mkOrderedChoice(nodes);
		properties.put(ctx, node);
	}

	@Override
	public void exitLabeledAlt(ANTLRv4Parser.LabeledAltContext ctx){
		copyNode(ctx, ctx.alternative());
	}

	@Override
	public void exitAlternative(ANTLRv4Parser.AlternativeContext ctx){
		List<Node> nodes = mkNodeList(ctx.element());
	}


	@Override
	public void exitElement(ANTLRv4Parser.ElementContext ctx){
		// String suffix = "";
		// if(ctx.ebnfSuffix() != null){
		// 	suffix = ctx.ebnfSuffix().getText();
		// }
		//
		// if(ctx.labeledElement() != null){
		// } else if(ctx.atom() != null){
		// 	ANTLRv4Parser.AtomContext atomCtx = ctx.atom();
		// 	parseTreeProperty.put(ctx, atomCtx.getText() + suffix + " ");
		// } else if(ctx.ebnf() != null){
		// 	parseTreeProperty.put(ctx, parseTreeProperty.get(ctx.ebnf()));
		// } else if(ctx.actionBlock() != null){}
	}


	@Override
	public void exitEbnf(ANTLRv4Parser.EbnfContext ctx){
		// StringBuilder buf = new StringBuilder();
		// // String suffix = ctx.blockSuffix().getText();
		//
		// buf.append(parseTreeProperty.get(ctx.block()));
		// if(ctx.blockSuffix() != null){
		// 	buf.append(ctx.blockSuffix().getText());
		// }
		//
		// buf.append(" ");
		// parseTreeProperty.put(ctx, buf.toString());
	}

	@Override
	public void exitBlock(ANTLRv4Parser.BlockContext ctx){
		// StringBuilder buf = new StringBuilder();
		// buf.append(ctx.LPAREN().getText());
		// buf.append(parseTreeProperty.get(ctx.altList()));
		// buf.append(ctx.RPAREN().getText());
		// parseTreeProperty.put(ctx, buf.toString());
	}

	@Override
	public void exitAltList(ANTLRv4Parser.AltListContext ctx){
		// List<ANTLRv4Parser.AlternativeContext> alts = ctx.alternative();
		// List<String> parts = new ArrayList<>();
		// for(ANTLRv4Parser.AlternativeContext alt : alts){
		// 	parts.add(parseTreeProperty.get(alt));
		// }
		//
		// parseTreeProperty.put(ctx, String.join(" / ", parts));
	}

}
