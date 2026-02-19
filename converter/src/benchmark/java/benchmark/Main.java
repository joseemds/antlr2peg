package benchmark;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.StatsTracker;

public class Main {
    private static final Tasks tasks = new Tasks();

    public static void main(String[] args) throws Exception {
        Path repoPath = cloneRepo();
        Map<String, StatsTracker> results = new LinkedHashMap<>();

        try (Stream<Path> paths = Files.walk(repoPath, 1)) {
            paths.filter(Files::isDirectory).forEach(dir -> {
                try (Stream<Path> files = Files.list(dir)) {
                    List<Path> gs = files
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(".g4"))
                            .toList();

                    if (gs.size() == 0) {
                        System.out.println("Grammar file missing at " + dir);
                        return;
                    }
                    if (gs.size() > 1) {
                        System.out.println("Only one grammar is allowed at " + dir);
                        return;
                    }

                    Path grammarPath = gs.get(0);
                    System.out.println("Processing grammar: " + grammarPath);

                    StatsTracker tracker = tasks.compilePeg(grammarPath);
                    results.put(grammarPath.toString(), tracker);

                } catch (Exception e) {
                    System.err.println("Error processing " + dir + ": " + e.getMessage());
                }
            });
        }

        System.out.println("Completed " + results.size() + " grammars");

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
        ProcessBuilder pb = new ProcessBuilder(
                "git", "clone", "--depth", "1",
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
}
