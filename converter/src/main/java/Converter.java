import backend.LpegBackend;
import java.io.IOException;
import java.util.List;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import peg.PegPrinter;
import peg.node.*;

public class Converter {
  public static void main(String[] args) throws IOException {
    CharStream input = CharStreams.fromStream(System.in);
    ANTLRv4Lexer lexer = new ANTLRv4Lexer(input);
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    ANTLRv4Parser parser = new ANTLRv4Parser(tokenStream);
    ParseTreeWalker walker = new ParseTreeWalker();
    ParseTree ast = parser.grammarSpec(); // grammarSpec = start rule
    AntlrListener listener = new AntlrListener();
    AntlrToPegListener pegListener = new AntlrToPegListener();
    PegPrinter pegPrinter = new PegPrinter();

    // walker.walk(listener, ast);
    walker.walk(pegListener, ast);

    // listener.printBuf();
    // pegListener.printAst();
    List<Node> rules = pegListener.getAst().getAst();
    LpegBackend lpegBackend = new LpegBackend();
    System.out.println(lpegBackend.convert(rules));
  }
}
