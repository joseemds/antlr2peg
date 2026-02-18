package cli;

import converter.Converter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import peg.GraphvizPrinter;
import peg.LeftRecursionChecker;
import peg.PegGrammar;

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

  public void run(CliOptions options) {
    if (options.printHelp) {
      this.printHelp();
      return;
    }

    PegGrammar pegGrammar = Converter.convertToPegGrammar(options.input);
    LeftRecursionChecker isLeftRecursive = new LeftRecursionChecker(pegGrammar);
    if (isLeftRecursive.check()) {
      throw new IllegalStateException("Left Recursive grammars are not supported");
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
