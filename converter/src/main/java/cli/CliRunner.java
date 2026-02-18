package cli;

class CliRunner {
  private String[] argv;

  private final String HELP_STRING =
      """
    antlr2peg -i input.g4 -o output.lua
		-i to indicate input file
		-o to indicate output file
		--print-tree To create a graphviz file represeting the tree
		""";

  public CliRunner(String[] args) {
    this.argv = args;
  }

  public void run() {
    var parser = new CliParser(this.argv);
    CliOptions options = parser.parse();

    if (options.printHelp) {
      this.printHelp();
      return;
    }
  }

  private void printHelp() {
    System.out.println(HELP_STRING);
  }
}
