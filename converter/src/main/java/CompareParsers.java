import java.io.*;
import java.nio.file.*;
import java.util.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

public class CompareParsers {

  public static void main(String[] args) throws Exception {
    List<String> failedFiles = new ArrayList<>();
    List<String> succeedFiles = new ArrayList<>();
    if (args.length != 1) {
      System.err.println("Usage: compareParser <GrammarName>");
      System.exit(1);
    }

    String grammar = args[0];
    Path inputDir = Paths.get("inputs", grammar.toLowerCase());
    if (!Files.isDirectory(inputDir)) {
      System.err.println("No input directory for " + grammar);
      System.exit(1);
    }

    System.out.println("=== Comparing parsers for " + grammar + " ===");

    try (var files = Files.list(inputDir)) {
      List<Path> matched = files.filter(p -> Files.isRegularFile(p)).toList();
      for (Path file : (Iterable<Path>) matched::iterator) {
        System.out.println("\n→ Testing " + file.getFileName());

        String text = Files.readString(file);

        boolean antlrOK = parseWithAntlr(grammar, text);
        boolean generatedOK = parseWithGenerated(grammar, file);

        if (antlrOK == generatedOK) {
          System.out.println("Match: both " + (antlrOK ? "accepted" : "rejected"));
          succeedFiles.add(file.getFileName().toString());
        } else {
          System.out.println("Mismatch: ANTLR=" + antlrOK + ", peg=" + generatedOK);
          failedFiles.add(file.getFileName().toString());
        }
      }

      int totalTested = matched.size();
      System.out.println("\n\n===== Summary =====");
      System.out.println("Number of files tested " + totalTested);
      System.out.printf(
          "Succeed %d (%%%.2f)\n",
          succeedFiles.size(), (float) succeedFiles.size() / totalTested * 100);
      System.out.printf(
          "Failed %d (%%%.2f)\n",
          failedFiles.size(), (float) failedFiles.size() / totalTested * 100);

      System.out.println("Failed files: " + failedFiles);
    }
  }

  static boolean parseWithAntlr(String grammar, String text) {
    try {
      String lexerName = grammar + "Lexer";
      String parserName = grammar + "Parser";

      Class<?> lexerClass = Class.forName(lexerName);
      Class<?> parserClass = Class.forName(parserName);

      CharStream input = CharStreams.fromString(text);
      Lexer lexer = (Lexer) lexerClass.getConstructor(CharStream.class).newInstance(input);
      CommonTokenStream tokens = new CommonTokenStream(lexer);
      Parser parser = (Parser) parserClass.getConstructor(TokenStream.class).newInstance(tokens);

      parser.removeErrorListeners();
      parser.addErrorListener(
          new BaseErrorListener() {
            @Override
            public void syntaxError(
                Recognizer<?, ?> recognizer,
                Object offendingSymbol,
                int line,
                int pos,
                String msg,
                RecognitionException e) {
              throw new RuntimeException("line " + line + ":" + pos + " " + msg);
            }
          });

      // TODO: make it generic for the grammars being tested
      var method = parserClass.getMethod("graph");
      method.invoke(parser);
      return parser.getNumberOfSyntaxErrors() == 0;
    } catch (Exception e) {
      System.out.println("ANTLR parse failed: " + e.getMessage());
      return false;
    }
  }

  static boolean parseWithGenerated(String grammarName, Path inputFile) {
    try {
      Path luaParser = compileToLua(grammarName);
      Process p = new ProcessBuilder("lua", luaParser.toFile().getAbsolutePath()).start();
      try (OutputStream os = p.getOutputStream()) {
        Files.copy(inputFile, os);
      }
      int status = p.waitFor();
      return status == 0;
    } catch (Exception e) {
      throw new Error(e);
    }
  }

  static Path compileToLua(String grammarName) {
    try {
      Path grammarPath = Paths.get("src/main/antlr", grammarName + ".g4");
      if (!Files.exists(grammarPath)) {
        System.err.println("Missing grammar file: " + grammarPath);
        return null;
      }

      System.out.println("[converter] Converting " + grammarPath);

      var grammarContent = Files.readString(grammarPath);
      var grammarStream = CharStreams.fromString(grammarContent);

      var pegGrammar = Converter.convertToPegGrammar(grammarStream);
      var lpegGrammar = Converter.convertToLpeg(pegGrammar);

      Path luaFile = Files.createTempFile(grammarName.toLowerCase() + "_parser", ".lua");
      luaFile.toFile().deleteOnExit();
      Files.writeString(luaFile, lpegGrammar);

      System.out.println("[converter] Generated Lua parser: " + luaFile);
      return luaFile;
    } catch (Exception e) {
      System.err.println("Failed to compile grammar " + grammarName + ": " + e.getMessage());
      return null;
    }
  }
}
