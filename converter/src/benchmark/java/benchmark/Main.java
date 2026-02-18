package benchmark;

import java.util.concurrent.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws Exception {
        int threads = Runtime.getRuntime().availableProcessors();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
				cloneRepo();

        // for (String entry : entries) {
        //     futures.add(pool.submit(() -> runSingle(entry)));
        // }
        //
        // List<Stats> results = new ArrayList<>();
        // for (Future<Stats> f : futures) {
        //     results.add(f.get());
        // }
        //
        pool.shutdown();
    }



		static private void cloneRepo(){
        long start = System.nanoTime();

        ProcessBuilder pb = new ProcessBuilder("git", "clone", "--depth", "1", "https://github.com/antlr/grammars-v4", "/tmp/grammars-v4");
				try {

        Process p = pb.start();
        int exitCode = p.waitFor();

        long elapsed = System.nanoTime() - start;

				System.out.println("Exited: " + exitCode + " Elapsed: " + elapsed);
				} catch(Exception e){
					System.out.println("Failed to clone grammars-v4");
				}
    }

	}

