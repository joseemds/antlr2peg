package cli;

public class CliOptions {
  public boolean printHelp = false;
  public boolean dumpTree = false;
  public boolean stats = true;
  public String input;
  public String output;

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
}
