package converter;

import backend.LpegBackend;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import peg.PegGrammar;
import peg.grammar.AmbiguousChoiceDetector;
import peg.grammar.UniqueTokenTracker;
import peg.node.*;
import transformation.FixRepetitions;
import transformation.FlattenGrammar;
import transformation.MoveEmpty;
import transformation.ReorderByUniquePath;
import utils.StatsTracker;

public class Converter {

  public static PegGrammar convertToPegGrammar(String inputFile, StatsTracker statsTracker) {
    Path inputPath = Path.of(inputFile);
    try {
      String input = Files.readString(inputPath);
      CharStream inputStream = CharStreams.fromString(input);
      ANTLRv4Lexer lexer = new ANTLRv4Lexer(inputStream);
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

      for (var entry : grammar.getFirsts().entrySet()) {
        Ident nonterm = new Ident(entry.getKey());
        if (entry.getValue().contains(nonterm)) {
          throw new Error("Grammar is left recursive and is not supported");
        }
      }

      grammar = grammar.transform(new FlattenGrammar());
      grammar = grammar.transform(new MoveEmpty());
      UniqueTokenTracker uniqueTokenTracker = new UniqueTokenTracker(grammar);
      uniqueTokenTracker.analyzeGrammar();

      uniqueTokenTracker.printUniqueTokens();
      uniqueTokenTracker.printUniquePaths();
      grammar = grammar.transform(new ReorderByUniquePath(grammar, statsTracker));
      FixRepetitions fixRepetitions = new FixRepetitions(grammar, statsTracker);
      grammar = grammar.transform(fixRepetitions);
      List<Rule> newRules = fixRepetitions.getNewRules();
      for (Rule rule : newRules) {
        grammar.addRule(rule);
      }

      System.out.println("===========");
      for (Entry<String, Set<Node>> e : grammar.getFirsts().entrySet()) {
        var follow = grammar.getFollows().get(e.getKey());
        var intersection = new HashSet<>(follow);
        intersection.retainAll(e.getValue());
        // System.out.printf(
        //     "FIRST(%s) ^ FOLLOWS(%s) = %s\n", e.getKey(), e.getKey(), e.getValue(),
        // intersection);
        System.out.printf("FIRST(%s) = %s\n", e.getKey(), e.getValue());
        System.out.printf("FOLLOW(%s) = %s\n", e.getKey(), follow);
      }

      AmbiguousChoiceDetector hasAmbiguousChoice =
          new AmbiguousChoiceDetector(grammar, statsTracker);
      hasAmbiguousChoice.checkAmbiguity();

      return grammar;

    } catch (IOException e) {
      throw new IllegalArgumentException("Error when reading inputFile " + inputFile);
    }
  }

  public static String convertToLpeg(PegGrammar pegGrammar) {
    LpegBackend lpegBackend = new LpegBackend(pegGrammar);
    return lpegBackend.convert(pegGrammar.getRules());
  }
}
