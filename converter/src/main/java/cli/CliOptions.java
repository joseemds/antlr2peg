package cli;

import java.util.List;

public class CliOptions {
  public boolean printHelp = false;
  public boolean dumpTree = false;
  public boolean stats = true;
  public String lexerFile;
  public String input;
  public String output;
  public String identifierRule;
  public List<String> skipRules;

  public CliOptions() {}

  public void setDumpTree(boolean dump) {
    this.dumpTree = dump;
  }

  public void setInputFile(String input) {
    this.input = input;
  }

  public void setOutputFile(String output) {
    this.output = output;
  }

  public void setPrintHelp(boolean printHelp) {
    this.printHelp = printHelp;
  }

  public void setLexerFile(String lexerFile) {
    this.lexerFile = lexerFile;
  }

  public void setSkipRules(List<String> skipRules) {
    this.skipRules = skipRules;
  }

  public void setIdentifier(String identifierRule) {
    this.identifierRule = identifierRule;
  }
}
