package benchmark;

import cli.CliOptions;
import cli.CliRunner;
import cli.RunResult;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Tasks {
  private final CliRunner runner = new CliRunner();

  public void run(String grammarFile) {
    CliOptions options = new CliOptions();
    options.setInputFile(grammarFile);
    runner.run(options);
  }

  public RunResult compilePeg(Path antlrGrammar, Path antlrLexer, String startRule) {
    CliOptions options = new CliOptions();
    try {
      Path outputFile = Files.createTempFile(antlrGrammar.getFileName().toString(), ".gen.lua");
      options.setInputFile(antlrGrammar.toString());
      options.setLexerFile(antlrLexer.toString());
      options.setOutputFile(outputFile.toString());
      options.setStartRule(startRule);
      return runner.runWithStats(options);

    } catch (IOException e) {
      throw new Error("Error when creating output file");
    }
  }

  public RunResult compilePeg(Path antlrGrammar, String startRule) {
    CliOptions options = new CliOptions();
    try {
      Path outputFile = Files.createTempFile(antlrGrammar.getFileName().toString(), ".gen.lua");
      options.setInputFile(antlrGrammar.toString());
      options.setOutputFile(outputFile.toString());
      options.setStartRule(startRule);
      return runner.runWithStats(options);

    } catch (IOException e) {
      throw new Error("Error when creating output file");
    }
  }

  // public boolean runPEG(String antlrGrammar, String inputFile) {
  //   try {
  //     Path outputFile = compilePeg(Path.of(antlrGrammar));
  //     Process p = new ProcessBuilder("lua", outputFile.toAbsolutePath().toString()).start();
  //     BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
  //     writer.write(Files.readString(Path.of(inputFile)));
  //     writer.flush();
  //
  //     int result = p.waitFor();
  //
  //     return result == 0;
  //   } catch (IOException | InterruptedException e) {
  //     return false;
  //   }
  // }

  public boolean runANTLR(String antlrGrammar, String startRule, String inputFile) {
    ProcessBuilder pb = new ProcessBuilder("antlr4-parse", antlrGrammar, startRule, inputFile);

    try {
      Process p = pb.start();
      int result = p.waitFor();
      if (result != 0) {
        System.err.println("antlr4-parse should suceeded, but it didn't");
      }
      return result == 0;
    } catch (IOException | InterruptedException e) {
      throw new IllegalStateException("Failed to run antlr4-parse");
    }
  }
  ;
}
