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

/**
 * Main entry point for the Tradespace Search Executive (TSE) application.
 * This class handles the execution of tradespace search operations with support
 * for parallel processing and thread-local topic management.
 * 
 * @author TSE Development Team
 */
public class TSE {

    /** ThreadLocal for storing subscription topic per thread */
    private static final ThreadLocal<String> threadLocalTopic = new ThreadLocal<>();

    /**
     * Main method that initializes and executes the TSE application.
     * 
     * @param args Command line arguments (currently not used)
     */
    public static void main(String[] args) {
        // Setup logger with finest level for detailed debugging
        Logger logger = Logger.getGlobal();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            logger.log(Level.SEVERE, "Uncaught exception in thread " + thread.getName(), throwable);
        });
        Level level = Level.FINEST;
        logger.setLevel(level);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        logger.addHandler(handler);

        // Configure parallel execution - currently set to single thread
        int numThreads = 1;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // Generate timestamp for unique output directories
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        // Submit tasks to executor
        for (int i = 0; i < numThreads; i++) {
            int threadId = i + 1;
            String topic = "TSE" + threadId;

            // Configuration: Set the request file path
            String requestFile = "TSERequests/assigning.json";
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
                    logger.finest(String.format(
                        "[%s] Took %.4f sec",
                        threadLocalTopic.get(),
                        (endTime - startTime) / Math.pow(10, 9)
                    ));
                } catch (IOException e) {
                    logger.log(
                        Level.SEVERE,
                        "Error creating directories: " + e.getMessage(),
                        e
                    );
                } catch (Exception e) {
                    logger.log(
                        Level.SEVERE,
                        "Error executing TSE: " + e.getMessage(),
                        e
                    );
                }
            });
        }

        // Shutdown the executor and wait for completion
        executor.shutdown();
        while (!executor.isTerminated()) {
            // Wait for all threads to complete
        }

        logger.info("All tasks completed.");
    }

    /**
     * Retrieves the thread-specific subscription topic.
     * 
     * @return The subscription topic for the current thread
     */
    public static String getSubscriptionTopic() {
        return threadLocalTopic.get();
    }
}
