package cli;

class CliParser {
  private String[] argv;

  public CliParser(String[] args) {
    this.argv = args;
  }

  public CliOptions parse() {
    CliOptions options = new CliOptions();

    if (this.argv.length <= 1) {
      options.setPrintHelp(true);
      return options;
    }

    for (int i = 0; i < argv.length; i++) {
      String arg = argv[i];

      switch (arg) {
        case "-i":
        case "--input":
          options.setInputFile(argv[i + 1]);
          break;

        case "-o":
        case "--output":
          options.setOutputFile(argv[i + 1]);
          break;

        case "--dump-tree":
          options.setDumpTree(true);
          break;

        case "-h":
        case "--help":
          options.setPrintHelp(true);
          break;
      }
    }

    validate(options);

    return options;
  }

  private void validate(CliOptions options) {
    if (options.printHelp) {
      return;
    }

    if (options.input == null) {
      throw new IllegalArgumentException("Input file is required.");
    }
  }
}
