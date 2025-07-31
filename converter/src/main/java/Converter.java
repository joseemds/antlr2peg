import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Converter {
	public static void main(String[] args) throws IOException {
		CharStream input = CharStreams.fromStream(System.in);
		ANTLRv4Lexer lexer = new ANTLRv4Lexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		ANTLRv4Parser parser = new ANTLRv4Parser(tokenStream);
		ParseTreeWalker walker = new ParseTreeWalker();
		ParseTree ast = parser.grammarSpec(); // grammarSpec = start rule
		AntlrListener listener = new AntlrListener();
		walker.walk(listener, ast);
	}
}
