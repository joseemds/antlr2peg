import java.io.IOException;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class Converter {
	public static void main(String[] args) throws IOException {
		CharStream input = CharStreams.fromString("a = | b | c");
		AbnfLexer lexer = new AbnfLexer(input);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		AbnfParser parser = new AbnfParser(tokenStream);
		ParseTree tree = parser.rulelist();
    System.out.println(tree.toStringTree(parser));

	}
}
