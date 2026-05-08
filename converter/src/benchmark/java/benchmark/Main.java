package benchmark;

import cli.RunResult;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import exception.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {
  private static final Tasks tasks = new Tasks();

  public static void main(String[] args) throws Exception {
    if (args.length < 1) {
      System.err.println("Usage: benchmark <grammars.json> [base-dir]");
      System.exit(1);
    }

    Path jsonPath = Path.of(args[0]);
    Path baseDir = args.length >= 2 ? Path.of(args[1]) : cloneRepo();

    List<GrammarEntry> defs = loadGrammarEntries(jsonPath);
    Map<String, TaskResult> results = new LinkedHashMap<>();

    for (GrammarEntry def : defs) {
      System.out.println("Processing grammar: " + def.name());
      processGrammar(def, baseDir, results);
    }

    System.out.println("Completed " + results.size() + " grammars");
    results.forEach(
        (name, result) -> {
          switch (result) {
            case TaskResult.Success s -> System.out.println("OK: " + name);
            case TaskResult.Failure f ->
                System.err.println("FAIL [" + f.kind() + "]: " + name + " -> " + f.message());
          }
        });

    ObjectMapper mapper = new ObjectMapper();
    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(results);
    Path outputPath = Path.of("results.json");
    Files.writeString(outputPath, json);
    System.out.println("Results written to " + outputPath.toAbsolutePath());
  }

  private static Path cloneRepo() {
    Path repoPath = Path.of("/tmp", "grammars");
    if (Files.exists(repoPath)) {
      return repoPath;
    }

    long start = System.nanoTime();
    ProcessBuilder pb =
        new ProcessBuilder(
            "git",
            "clone",
            "--depth",
            "1",
            "https://github.com/antlr/grammars-v4",
            repoPath.toString());

    try {
      Process p = pb.start();
      int exitCode = p.waitFor();
      long elapsed = System.nanoTime() - start;
      System.out.println("Cloned: exitCode=" + exitCode + " elapsed=" + elapsed + "ns");
      return repoPath;
    } catch (Exception e) {
      throw new Error("Failed to clone grammars-v4", e);
    }
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

  private static void processGrammar(
      GrammarEntry def, Path baseDir, Map<String, TaskResult> results) {
    try {
      RunResult result;
      boolean hasSeparateLexer = def.lexer() != null && !def.lexer().isBlank();
      if (hasSeparateLexer) {
        Path parserPath = baseDir.resolve(def.parser());
        Path lexerPath = baseDir.resolve(def.lexer());
        System.out.println(
            "Parser path " + parserPath + " def.parser " + def.parser() + " def: " + def);
        result = tasks.compilePeg(parserPath, lexerPath, def.start());
      } else {
        Path parserPath = baseDir.resolve(def.parser());
        System.out.println(
            "Parser path " + parserPath + " def.parser " + def.parser() + " def: " + def);
        result = tasks.compilePeg(parserPath, def.start());
      }

      results.put(def.name(), new TaskResult.Success(result));

    } catch (LeftRecursionException e) {
      results.put(def.name(), new TaskResult.Failure(ErrorKind.LEFT_RECURSION, e.getMessage()));
    } catch (RuleNotFoundException e) {
      results.put(def.name(), new TaskResult.Failure(ErrorKind.MISSING_RULE, e.getMessage()));
    } catch (WrongStartRuleException e) {
      results.put(def.name(), new TaskResult.Failure(ErrorKind.WRONG_START_RULE, e.getMessage()));
    } catch (SemanticActionNotAllowedException e) {
      results.put(
          def.name(), new TaskResult.Failure(ErrorKind.HAS_SEMANTIC_ACTION, e.getMessage()));
    } catch (Throwable e) {
      results.put(def.name(), new TaskResult.Failure(ErrorKind.UNKNOWN, e.getMessage()));
    }
  }
}
