import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTreeProperty;
import peg.PegAst;
import peg.PegPrinter;
import peg.node.*;

public class AntlrToPegListener extends ANTLRv4ParserBaseListener {

  private final PegAst ast = new PegAst();
  private boolean isFragment = false;
  private ParseTreeProperty<Node> properties = new ParseTreeProperty<>();

  private void copyNode(ParserRuleContext parent, ParserRuleContext child) {
    properties.put(parent, properties.get(child));
  }

  private List<Node> mkNodeList(List<? extends ParserRuleContext> ctxs) {
    List<Node> nodes = new ArrayList<>();
    for (var elem : ctxs) {
      nodes.add(properties.get(elem));
    }

    return nodes;
  }

  public void printAst() {
    PegPrinter pegPrinter = new PegPrinter();
    String out = pegPrinter.print(ast);
    System.out.println(out);
  }

  public PegAst getAst() {
    return this.ast;
  }

  @Override
  public void exitRules(ANTLRv4Parser.RulesContext ctx) {
    for (var rule : ctx.ruleSpec()) {
      ast.addNode(properties.get(rule));
    }
  }

  @Override
  public void exitRuleSpec(ANTLRv4Parser.RuleSpecContext ctx) {
    if (ctx.parserRuleSpec() != null) {
      var childNode = properties.get(ctx.parserRuleSpec());
      properties.put(ctx, childNode);
    } else if (ctx.lexerRuleSpec() != null) {
      var childNode = properties.get(ctx.lexerRuleSpec());
      properties.put(ctx, childNode);
    }
  }

  @Override
  public void exitParserRuleSpec(ANTLRv4Parser.ParserRuleSpecContext ctx) {
    var ident = ctx.RULE_REF().getText();
    var rhs = properties.get(ctx.ruleBlock());
    properties.put(ctx, ast.mkRule(ident, rhs));
  }

  @Override
  public void exitRuleBlock(ANTLRv4Parser.RuleBlockContext ctx) {
    copyNode(ctx, ctx.ruleAltList());
  }

  @Override
  public void exitLexerRuleSpec(ANTLRv4Parser.LexerRuleSpecContext ctx) {
    var ident = ctx.TOKEN_REF().getText();
    var rhs = properties.get(ctx.lexerRuleBlock());
    if (ctx.FRAGMENT() != null) {
      this.isFragment = true;
    }
    properties.put(ctx, ast.mkRule(ident, rhs));
    this.isFragment = false;
  }

  @Override
  public void exitLexerRuleBlock(ANTLRv4Parser.LexerRuleBlockContext ctx) {
    copyNode(ctx, ctx.lexerAltList());
  }

  @Override
  public void exitLexerAltList(ANTLRv4Parser.LexerAltListContext ctx) {
    var nodes = mkNodeList(ctx.lexerAlt());
    var node = ast.mkOrderedChoice(nodes);
    properties.put(ctx, node);
  }

  @Override
  public void exitLexerAlt(ANTLRv4Parser.LexerAltContext ctx) {
    copyNode(ctx, ctx.lexerElements());
  }

  @Override
  public void exitLexerElements(ANTLRv4Parser.LexerElementsContext ctx) {
    var nodes = mkNodeList(ctx.lexerElement());
    var node = ast.mkSequence(nodes);
    properties.put(ctx, node);
  }

  @Override
  public void exitLexerElement(ANTLRv4Parser.LexerElementContext ctx) {
    Optional<Operator> suffix = Optional.empty();
    if (ctx.ebnfSuffix() != null) {
      // TODO: operadores com > 1
      suffix = Optional.of(ast.operatorOfString(ctx.ebnfSuffix().getText().substring(0, 1)));
    }
    if (ctx.lexerAtom() != null) {
      var node = ast.mkTerm(properties.get(ctx.lexerAtom()), suffix);
      properties.put(ctx, node);

    } else if (ctx.lexerBlock() != null) {
      var blockCtx = ctx.lexerBlock();
      var nodes = mkNodeList(blockCtx.lexerAltList().lexerAlt());
      var choice = ast.mkOrderedChoice(nodes);
      var node = ast.mkTerm(choice, suffix);
      properties.put(ctx, node);
    } else if (ctx.actionBlock() != null) {
    }
  }

  @Override
  public void exitLexerAtom(ANTLRv4Parser.LexerAtomContext ctx) {
    if (ctx.characterRange() != null) {
      copyNode(ctx, ctx.characterRange());
    } else if (ctx.LEXER_CHAR_SET() != null) {
      var ident = ast.mkCharset("[" + ctx.LEXER_CHAR_SET().getText() + "]");
      properties.put(ctx, ident);
    } else if (ctx.terminalDef() != null) {
      copyNode(ctx, ctx.terminalDef());
    } else if (ctx.notSet() != null) {
      copyNode(ctx, ctx.notSet());
    } else if (ctx.wildcard() != null) {
      var wildcard = ast.mkWildcard();
      properties.put(ctx, wildcard);
    }
  }

  @Override
  public void exitNotSet(ANTLRv4Parser.NotSetContext ctx) {
    Node node;
    if (ctx.setElement() != null) {
      node = properties.get(ctx.setElement());
    } else {
      node = properties.get(ctx.blockSet());
    }
    var notNote = ast.mkNot(node);
    properties.put(ctx, notNote);
  }

  @Override
  public void exitBlockSet(ANTLRv4Parser.BlockSetContext ctx) {
    var nodes = mkNodeList(ctx.setElement());
    var node = ast.mkSequence(nodes);
    properties.put(ctx, node);
  }

  @Override
  public void exitSetElement(ANTLRv4Parser.SetElementContext ctx) {
    if (ctx.TOKEN_REF() != null) {
      var ident = ast.mkIdent(ctx.TOKEN_REF().getText());
      properties.put(ctx, ident);

    } else if (ctx.STRING_LITERAL() != null) {
      var ident = ast.mkLiteral(ctx.STRING_LITERAL().getText());
      properties.put(ctx, ident);
    } else if (ctx.characterRange() != null) {
      copyNode(ctx, ctx.characterRange());
    } else if (ctx.LEXER_CHAR_SET() != null) {
      var ident = ast.mkCharset("[" + ctx.LEXER_CHAR_SET().getText() + "]");
      properties.put(ctx, ident);
    }
  }

  @Override
  public void exitCharacterRange(ANTLRv4Parser.CharacterRangeContext ctx) {
    StringBuilder buf = new StringBuilder();
    buf.append(ctx.STRING_LITERAL(0).getText());
    buf.append(ctx.RANGE().getText());
    buf.append(ctx.STRING_LITERAL(1).getText());
    var ident = ast.mkIdent(buf.toString());
    properties.put(ctx, ident);
  }

  @Override
  public void exitRuleAltList(ANTLRv4Parser.RuleAltListContext ctx) {
    List<ANTLRv4Parser.LabeledAltContext> alts = ctx.labeledAlt();
    List<Node> nodes = mkNodeList(alts);
    var node = ast.mkOrderedChoice(nodes);
    properties.put(ctx, node);
  }

  @Override
  public void exitLabeledAlt(ANTLRv4Parser.LabeledAltContext ctx) {
    copyNode(ctx, ctx.alternative());
  }

  @Override
  public void exitAlternative(ANTLRv4Parser.AlternativeContext ctx) {
    List<Node> nodes = mkNodeList(ctx.element());
    var node = ast.mkSequence(nodes);
    properties.put(ctx, node);
  }

  @Override
  public void exitElement(ANTLRv4Parser.ElementContext ctx) {
    Optional<Operator> suffix = Optional.empty();
    if (ctx.ebnfSuffix() != null) {
      suffix = Optional.of(ast.operatorOfString(ctx.ebnfSuffix().getText()));
    }

    if (ctx.labeledElement() != null) {
    } else if (ctx.atom() != null) {
      var node = properties.get(ctx.atom());
      var term = ast.mkTerm(node, suffix);
      properties.put(ctx, term);
    } else if (ctx.ebnf() != null) {
      copyNode(ctx, ctx.ebnf());
    } else if (ctx.actionBlock() != null) {
    }
  }

  @Override
  public void exitAtom(ANTLRv4Parser.AtomContext ctx) {
    if (ctx.terminalDef() != null) {
      copyNode(ctx, ctx.terminalDef());
    } else if (ctx.ruleref() != null) {
      var ruleRef = ast.mkIdent(ctx.getText());
      properties.put(ctx, ruleRef);
    } else if (ctx.notSet() != null) {
      copyNode(ctx, ctx.notSet());
    } else if (ctx.wildcard() != null) {
      // FIXME
      var ident = ast.mkIdent(ctx.wildcard().getText());
      properties.put(ctx, ident);
    }
  }

  @Override
  public void exitTerminalDef(ANTLRv4Parser.TerminalDefContext ctx) {
    if (ctx.STRING_LITERAL() != null) {
      var node = ast.mkLiteral(ctx.getText());
      properties.put(ctx, node);
    } else if (ctx.TOKEN_REF() != null) {
      var node = ast.mkIdent(ctx.getText());
      properties.put(ctx, node);
    }
  }

  @Override
  public void exitEbnf(ANTLRv4Parser.EbnfContext ctx) {
    Optional<Operator> suffix = Optional.empty();
    var choices = properties.get(ctx.block());

    if (ctx.blockSuffix() != null) {
      suffix = Optional.of(ast.operatorOfString(ctx.blockSuffix().getText()));
    }

    var node = ast.mkTerm(choices, suffix);
    properties.put(ctx, node);
  }

  @Override
  public void exitBlock(ANTLRv4Parser.BlockContext ctx) {
    copyNode(ctx, ctx.altList());
  }

  @Override
  public void exitAltList(ANTLRv4Parser.AltListContext ctx) {
    var nodes = mkNodeList(ctx.alternative());
    var node = ast.mkOrderedChoice(nodes);
    properties.put(ctx, node);
  }
}
