import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.tree.ParseTreeProperty;

/**
 * AntlrListener
 */
public class AntlrListener extends ANTLRv4ParserBaseListener{
	private ParseTreeProperty<String> parseTreeProperty = new ParseTreeProperty<String>();

	StringBuilder out = new StringBuilder();

	public void printBuf(){
		System.out.println(this.out.toString());
	}


	@Override
	public void exitRules(ANTLRv4Parser.RulesContext ctx){
		for(var rule: ctx.ruleSpec()){
			out.append(parseTreeProperty.get(rule));
			out.append("\n");
		}
	}



	public void exitRuleSpec(ANTLRv4Parser.RuleSpecContext ctx){
		if(ctx.parserRuleSpec() != null){
			var childNode = parseTreeProperty.get(ctx.parserRuleSpec());
			parseTreeProperty.put(ctx, childNode);
		} else if (ctx.lexerRuleSpec() != null){
			// var childNode = ctx.lexerRuleSpec();
			parseTreeProperty.put(ctx, "unimplemented");
		}
	}


	public void exitRuleBlock(ANTLRv4Parser.RuleBlockContext ctx){
		parseTreeProperty.put(ctx, parseTreeProperty.get(ctx.ruleAltList()));
	}


	@Override
	public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx){
		StringBuilder buf = new StringBuilder();
		String body = parseTreeProperty.get(ctx.ruleBlock());
		buf.append(ctx.RULE_REF().getText() + " <- ");
		buf.append(body);
		parseTreeProperty.put(ctx, buf.toString());
	}

	@Override
	public void exitRuleAltList(ANTLRv4Parser.RuleAltListContext ctx){
		List<ANTLRv4Parser.LabeledAltContext> alts = ctx.labeledAlt();
		List<String> parts = new ArrayList<>();
		for(ANTLRv4Parser.LabeledAltContext alt : alts){
			parts.add(parseTreeProperty.get(alt));
		}

		parseTreeProperty.put(ctx, String.join(" / ", parts));
	}

	@Override
	public void exitLabeledAlt(ANTLRv4Parser.LabeledAltContext ctx){
		parseTreeProperty.put(ctx, parseTreeProperty.get(ctx.alternative()));
	}

	@Override
	public void exitAlternative(ANTLRv4Parser.AlternativeContext ctx){
		StringBuilder buf = new StringBuilder();
		for(var elem : ctx.element()) {
			buf.append(parseTreeProperty.get(elem));

		}
		parseTreeProperty.put(ctx, buf.toString());
	}


	@Override
	public void exitElement(ANTLRv4Parser.ElementContext ctx){
		String suffix = "";
		if(ctx.ebnfSuffix() != null){
			suffix = ctx.ebnfSuffix().getText();
		}

		if(ctx.labeledElement() != null){
		} else if(ctx.atom() != null){
			ANTLRv4Parser.AtomContext atomCtx = ctx.atom();
			parseTreeProperty.put(ctx, atomCtx.getText() + suffix + " ");
		} else if(ctx.ebnf() != null){
			parseTreeProperty.put(ctx, parseTreeProperty.get(ctx.ebnf()));
		} else if(ctx.actionBlock() != null){}
	}


	@Override
	public void exitEbnf(ANTLRv4Parser.EbnfContext ctx){
		StringBuilder buf = new StringBuilder();
		// String suffix = ctx.blockSuffix().getText();

		buf.append(parseTreeProperty.get(ctx.block()));
		if(ctx.blockSuffix() != null){
			buf.append(ctx.blockSuffix().getText());
		}

		buf.append(" ");
		parseTreeProperty.put(ctx, buf.toString());
	}

	@Override
	public void exitBlock(ANTLRv4Parser.BlockContext ctx){
		StringBuilder buf = new StringBuilder();
		buf.append(ctx.LPAREN().getText());
		// for(var ruleAction : ctx.ruleAction()){
		//
		// }
		//

		buf.append(parseTreeProperty.get(ctx.altList()));
		buf.append(ctx.RPAREN().getText());
		parseTreeProperty.put(ctx, buf.toString());
	}

	@Override
	public void exitAltList(ANTLRv4Parser.AltListContext ctx){
		List<ANTLRv4Parser.AlternativeContext> alts = ctx.alternative();
		List<String> parts = new ArrayList<>();
		for(ANTLRv4Parser.AlternativeContext alt : alts){
			parts.add(parseTreeProperty.get(alt));
		}

		parseTreeProperty.put(ctx, String.join(" / ", parts));
	}

}
