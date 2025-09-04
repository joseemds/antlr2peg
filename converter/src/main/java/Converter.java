import backend.LpegBackend;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import peg.GraphvizPrinter;
import peg.node.*;

public class Converter {
  private static void printHelp() {
    System.out.println("""
    antlr2peg -i input.g4 -o output.lua
   """);
  }

  public static void main(String[] args) throws IOException {
    if (args.length == 1) {
      printHelp();
    }

    Path inputFile = Path.of(args[1]);
    Path outputFile = Path.of(args[3]);
    String input = Files.readString(inputFile);
    CharStream inputStream = CharStreams.fromString(input);
    ANTLRv4Lexer lexer = new ANTLRv4Lexer(inputStream);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    ANTLRv4Parser parser = new ANTLRv4Parser(tokenStream);
    ParseTreeWalker walker = new ParseTreeWalker();
    ParseTree ast = parser.grammarSpec(); // grammarSpec = start rule
    AntlrToPegListener pegListener = new AntlrToPegListener();
    walker.walk(pegListener, ast);
    List<Node> rules = pegListener.getAst().getAst();
		GraphvizPrinter graphPrinter = new GraphvizPrinter();
    LpegBackend lpegBackend = new LpegBackend();
    Files.writeString(outputFile, lpegBackend.convert(rules));
		Files.writeString(Path.of("ast.dot"), graphPrinter.print(rules));
  }
}
