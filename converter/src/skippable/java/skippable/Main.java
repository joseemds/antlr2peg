package skippable;


import benchmark.GrammarEntry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import converter.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

public class Main {

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Usage: skippable <grammars.json> [base-dir]");
      System.exit(1);
    }

    Path jsonPath = Path.of(args[0]);
    Path baseDir = args.length >= 2 ? Path.of(args[1]) : Path.of(".");

    List<GrammarEntry> defs = loadGrammarEntries(jsonPath);

    ObjectMapper mapper = new ObjectMapper();
    ObjectNode output = mapper.createObjectNode();

    for (GrammarEntry def : defs) {
      System.out.println("Processing grammar: " + def.name());

      Set<String> skippable = collectSkippable(def, baseDir);

      ArrayNode arr = mapper.createArrayNode();
      skippable.forEach(arr::add);
      output.set(def.name(), arr);
    }

    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(output);
    Path outputPath = Path.of("skippable.json");
    Files.writeString(outputPath, json);
    System.out.println("Results written to " + outputPath.toAbsolutePath());
  }

  private static Set<String> collectSkippable(GrammarEntry def, Path baseDir) {
    SkippableRuleListener listener = new SkippableRuleListener();

    try {
      walkGrammar(baseDir.resolve(def.parser()), listener);

      boolean hasSeparateLexer = def.lexer() != null && !def.lexer().isBlank();
      if (hasSeparateLexer) {
        walkGrammar(baseDir.resolve(def.lexer()), listener);
      }
    } catch (Exception e) {
      System.err.println("Failed to process grammar: " + def.name() + " -> " + e.getMessage());
    }

    return listener.getSkippableRules();
  }

  private static void walkGrammar(Path grammarPath, SkippableRuleListener listener)
      throws Exception {
    var input = CharStreams.fromPath(grammarPath);
    var lexer = new ANTLRv4Lexer(input);
    var tokens = new CommonTokenStream(lexer);
    var parser = new ANTLRv4Parser(tokens);
    var tree = parser.grammarSpec();
    new ParseTreeWalker().walk(listener, tree);
  }

  private static List<GrammarEntry> loadGrammarEntries(Path jsonPath) throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    JsonNode root = mapper.readTree(jsonPath.toFile());
    JsonNode array = root.isArray() ? root : root.elements().next();

    List<GrammarEntry> defs = new ArrayList<>();
    for (JsonNode node : array) {
      String name = node.path("name").asText();
      String lexer = node.path("lexer").asText("");
      String parser = node.path("parser").asText("");
      String start = node.path("start").asText("");

      if (parser.isBlank()) {
        System.err.println("Skipping entry \"" + name + "\": missing required \"parser\" field");
        continue;
      }

      List<String> examples = new ArrayList<>();
      for (JsonNode ex : node.path("example")) {
        examples.add(ex.asText());
      }
      defs.add(new GrammarEntry(name, parser, lexer, start, examples));
    }
    return defs;
  }
}
