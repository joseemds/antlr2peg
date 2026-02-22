package benchmark;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;
import com.fasterxml.jackson.databind.ObjectMapper;
import utils.StatsTracker;
import exception.LeftRecursionException;

public class Main {
    private static final Tasks tasks = new Tasks();

    public static void main(String[] args) throws Exception {
        Path repoPath = cloneRepo();
        Map<String, TaskResult> results = new LinkedHashMap<>();

        try (Stream<Path> paths = Files.walk(repoPath, 1)) {
            paths.filter(Files::isDirectory).forEach(dir -> {
                try (Stream<Path> files = Files.list(dir)) {
                    List<Path> gs = files
                            .filter(Files::isRegularFile)
                            .filter(p -> p.getFileName().toString().endsWith(".g4"))
                            .toList();

                    if (gs.size() == 0) {
                        results.put(dir.toString(), new TaskResult.Failure(ErrorKind.MISSING_FILE, "No .g4 file found"));
                        return;
                    }
                    if (gs.size() > 1) {
                        results.put(dir.toString(), new TaskResult.Failure(ErrorKind.MULTIPLE_FILES, "Found " + gs.size() + " grammar files")); return;
                    }

                    Path grammarPath = gs.get(0);
                    System.out.println("Processing grammar: " + grammarPath);

                    try {
                        StatsTracker tracker = tasks.compilePeg(grammarPath);
                        results.put(grammarPath.toString(), new TaskResult.Success(tracker));
                    } catch (LeftRecursionException e) {
                        results.put(grammarPath.toString(), new TaskResult.Failure(ErrorKind.LEFT_RECURSION, e.getMessage()));
                    } catch (Throwable e) {
                        results.put(grammarPath.toString(), new TaskResult.Failure(ErrorKind.UNKNOWN, e.getMessage()));
                    }

                } catch (Exception e) {
                    System.err.println("Error processing " + dir + ": " + e.getMessage());
                }
            });
        }

        System.out.println("Completed " + results.size() + " grammars");

        results.forEach((path, result) -> {
            switch (result) {
                case TaskResult.Success s -> System.out.println("OK: " + path);
                case TaskResult.Failure f -> System.err.println("FAIL [" + f.kind() + "]: " + path + " -> " + f.message());
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
