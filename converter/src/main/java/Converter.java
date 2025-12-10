import backend.LpegBackend;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import peg.GraphvizPrinter;
import peg.PegGrammar;
import peg.grammar.AmbiguousChoiceDetector;
import peg.grammar.UniqueTokenTracker;
import peg.node.*;
import transformation.FixRepetitions;
import transformation.FlattenGrammar;
import transformation.MoveEmpty;
import transformation.ReorderByUniquePath;

public class Converter {
  private static void printHelp() {
    System.out.println("""
    antlr2peg -i input.g4 -o output.lua
   """);
  }

  public static PegGrammar convertToPegGrammar(CharStream input) {
    ANTLRv4Lexer lexer = new ANTLRv4Lexer(input);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    ANTLRv4Parser parser = new ANTLRv4Parser(tokenStream);
    ParseTreeWalker walker = new ParseTreeWalker();
    ParseTree ast = parser.grammarSpec(); // grammarSpec = start rule
    AntlrToPegListener pegListener = new AntlrToPegListener();
    walker.walk(pegListener, ast);
    var grammarOptions = pegListener.getGrammarOptions();
    var grammar = pegListener.getGrammar();
    grammar.setGrammarOptions(grammarOptions);
    grammar.computeNonTerminals();
    grammar.computeFirst();
    grammar.computeFollowSets();

    // for(var entry : grammar.getFirsts().entrySet()){
    //     Ident nonterm = new Ident(entry.getKey());
    //     if(entry.getValue().contains(nonterm)) {
    //      throw new Error("Grammar is left recursive and is not supported");
    //     }
    //  }

    grammar = grammar.transform(new FlattenGrammar());
    grammar = grammar.transform(new MoveEmpty());
    UniqueTokenTracker uniqueTokenTracker = new UniqueTokenTracker(grammar);
    uniqueTokenTracker.analyzeGrammar();

    uniqueTokenTracker.printUniqueTokens();
    uniqueTokenTracker.printUniquePaths();
    grammar = grammar.transform(new ReorderByUniquePath(grammar));
    grammar = grammar.transform(new FixRepetitions(grammar));

    // for (Entry<String, Set<Node>> e : grammar.getFollows().entrySet()) {
    //   System.out.printf("FOLLOWS(%s) = %s\n", e.getKey(), e.getValue().toString());
    // }
    //
    // for (Entry<String, Set<Node>> e : grammar.getFirsts().entrySet()) {
    //   System.out.printf("FIRST(%s) = %s\n", e.getKey(), e.getValue().toString());
    // }
    //
    // System.out.println("===========");
    // for (Entry<String, Set<Node>> e : grammar.getFirsts().entrySet()) {
    //   var follow = grammar.getFollows().get(e.getKey());
    //   follow.retainAll(e.getValue());
    //   System.out.printf(
    //       "FIRST(%s) ^ FOLLOWS(%s) = %s\n", e.getKey(), e.getKey(), e.getValue(), follow);
    // }

    return grammar;
  }

  public static String convertToLpeg(PegGrammar pegGrammar) {
    LpegBackend lpegBackend = new LpegBackend(pegGrammar);
    return lpegBackend.convert(pegGrammar.getRules());
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 1) {
      printHelp();
    }

    Path inputFile = Path.of(args[1]);
    Path outputFile = Path.of(args[3]);
    String input = Files.readString(inputFile);
    CharStream inputStream = CharStreams.fromString(input);
    PegGrammar grammar = convertToPegGrammar(inputStream);
    //
    // System.out.println(grammar.getRules());

    AmbiguousChoiceDetector ambiguityDetector = new AmbiguousChoiceDetector(grammar);
    ambiguityDetector.checkAmbiguity();
    GraphvizPrinter graphPrinter = new GraphvizPrinter();
    LpegBackend lpegBackend = new LpegBackend(grammar);
    Files.writeString(outputFile, lpegBackend.convert(grammar.getRules()));
    Files.writeString(Path.of("ast.dot"), graphPrinter.print(grammar.getRules()));
  }
}
