package cli;

import converter.Converter;
import exception.LeftRecursionException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import peg.GraphvizPrinter;
import peg.LeftRecursionChecker;
import peg.PegGrammar;
import peg.grammar.AmbiguousChoiceDetector;
import utils.StatsTracker;

public class CliRunner {
  private String[] argv;

  private final String HELP_STRING =
      """
    antlr2peg -i input.g4 -o output.lua
		-i to indicate input file
		-o to indicate output file
		--print-tree To create a graphviz file represeting the tree
		""";

  public CliRunner() {}

  public CliRunner(String[] args) {
    this.argv = args;
  }

  public void run() {
    CliParser cliParser = new CliParser(this.argv);
    CliOptions options = cliParser.parse();
    run(options);
  }
  ;

  public StatsTracker runWithStats(CliOptions options) {
    StatsTracker statsTracker = new StatsTracker();
    run(options, statsTracker);
    return statsTracker;
  }

  public void run(CliOptions options) {
    run(options, new StatsTracker());
  }

  public void run(CliOptions options, StatsTracker statsTracker) {
    if (options.printHelp) {
      this.printHelp();
    }

    PegGrammar pegGrammar = Converter.convertToPegGrammar(options.input, statsTracker);
    AmbiguousChoiceDetector hasAmbiguousChoice =
        new AmbiguousChoiceDetector(pegGrammar, statsTracker);
    LeftRecursionChecker isLeftRecursive = new LeftRecursionChecker(pegGrammar);
    hasAmbiguousChoice.checkAmbiguity();
    if (isLeftRecursive.check()) {
      throw new LeftRecursionException("Left recursion is not supported");
    }

    Path outputFile = Path.of(options.output);
    try {
      Files.writeString(outputFile, Converter.convertToLpeg(pegGrammar));
    } catch (IOException e) {
      System.out.println("Failed when creating ouput file");
      e.printStackTrace();
    }
    Converter.convertToLpeg(pegGrammar);

    if (options.dumpTree) {
      GraphvizPrinter graphvizPrinter = new GraphvizPrinter();
      try {
        Files.writeString(Path.of("ast.dot"), graphvizPrinter.print(pegGrammar.getRules()));
      } catch (IOException e) {
        System.out.println("Failed when creating graphviz file");
        e.printStackTrace();
      }
    }
  }

  private void printHelp() {
    System.out.println(HELP_STRING);
  }
}
