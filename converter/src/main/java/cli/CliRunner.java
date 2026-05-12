package cli;

import converter.Converter;
import exception.LeftRecursionException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import peg.GraphvizPrinter;
import peg.LeftRecursionChecker;
import peg.PegGrammar;
import utils.StatsTracker;

public class CliRunner {
  private String[] argv;

  private final String HELP_STRING =
      """
    antlr2peg -i input.g4 -o output.lua
		-i to indicate input file
	  -l to indicate (separated) lexer file (optional)
		-o to indicate output file
	  -s to indicate start rule
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

  public RunResult runWithStats(CliOptions options) {
    StatsTracker statsTracker = new StatsTracker();
    Path output = run(options, statsTracker);
    return new RunResult(output, statsTracker);
  }

  public Path run(CliOptions options) {
    return run(options, new StatsTracker());
  }

  public Path run(CliOptions options, StatsTracker statsTracker) {
    if (options.printHelp) {
      this.printHelp();
    }
    PegGrammar pegGrammar = null;

    if (options.lexerFile != null && !options.lexerFile.isEmpty()) {
      pegGrammar =
          Converter.convertToPegGrammar(options.input, options.lexerFile, statsTracker, options);
    } else {
      pegGrammar = Converter.convertToPegGrammar(options.input, statsTracker, options);
    }

    LeftRecursionChecker isLeftRecursive = new LeftRecursionChecker(pegGrammar);
    if (isLeftRecursive.check()) {
      throw new LeftRecursionException("Left recursion is not supported");
    }

    Path outputFile = Path.of(options.output);
    try {
      Files.writeString(outputFile, Converter.convertToLpeg(pegGrammar));
    } catch (IOException e) {
      throw new UncheckedIOException("Failed when creating output file", e);
    }

    if (options.dumpTree) {
      GraphvizPrinter graphvizPrinter = new GraphvizPrinter();
      try {
        Files.writeString(Path.of("ast.dot"), graphvizPrinter.print(pegGrammar.getRules()));
      } catch (IOException e) {
        System.out.println("Failed when creating graphviz file");
        e.printStackTrace();
      }
    }
    return outputFile;
  }

  private void printHelp() {
    System.out.println(HELP_STRING);
  }
}
