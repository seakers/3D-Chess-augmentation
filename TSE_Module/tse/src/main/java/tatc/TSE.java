package tatc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import tatc.tradespaceiterator.TradespaceSearchExecutive;

public class TSE {

    // ThreadLocal for storing subscription topic per thread
    private static final ThreadLocal<String> threadLocalTopic = new ThreadLocal<>();

    public static void main(String[] args) {
        // Setup logger
        Level level = Level.FINEST;
        Logger.getGlobal().setLevel(level);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        Logger.getGlobal().addHandler(handler);

        // Configure parallel execution
        int numThreads = 4; // Number of threads for parallel execution
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Timestamp for unique output directories
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        // Submit tasks to executor
        for (int i = 0; i < numThreads; i++) {
            int threadId = i + 1; // Unique ID for each thread
            String topic = "TSE" + threadId; // Generate topic as TSE + thread ID
            String requestFile = "TSERequests/TSERequestClimateCentricGADSPA.json";
            String outputDir = String.format("TSE_Module/tse/results/results_%s_%d", timestamp, threadId);

            executor.submit(() -> {
                try {
                    // Set the thread-local subscription topic
                    threadLocalTopic.set(topic);

                    // Ensure output directory exists
                    Path path = Paths.get(outputDir);
                    Files.createDirectories(path);

                    // Execute Tradespace Search Executive
                    long startTime = System.nanoTime();
                    String fullPathArg0 = Paths.get(requestFile).toAbsolutePath().toString();
                    String fullPathArg1 = path.toAbsolutePath().toString();
                    TradespaceSearchExecutive tse = new TradespaceSearchExecutive(fullPathArg0, fullPathArg1);
                    tse.run();
                    long endTime = System.nanoTime();

                    // Log execution time
                    Logger.getGlobal().finest(String.format("[%s] Took %.4f sec", threadLocalTopic.get(), (endTime - startTime) / Math.pow(10, 9)));
                } catch (IOException e) {
                    Logger.getGlobal().severe("Error creating directories: " + e.getMessage());
                } catch (Exception e) {
                    Logger.getGlobal().severe("Error executing TSE: " + e.getMessage());
                }
            });
        }

        // Shutdown the executor
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to complete
        }

        Logger.getGlobal().info("All tasks completed.");
    }

    // Accessor method for retrieving the thread-specific topic
    public static String getSubscriptionTopic() {
        return threadLocalTopic.get();
    }
}
