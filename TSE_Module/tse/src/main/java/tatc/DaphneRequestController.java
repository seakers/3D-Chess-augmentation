// src/main/java/tatc/DaphneRequestController.java
package tatc;

import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.io.IOException;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import tatc.model.TSEResponse;
import tatc.tradespaceiterator.TradespaceSearchExecutive;
import tatc.tradespaceiterator.ProblemProperties;
import tatc.tradespaceiterator.TradespaceSearchStrategy;
import tatc.tradespaceiterator.TradespaceSearchStrategyMOEAnew;
import org.moeaframework.core.Solution;
import org.moeaframework.core.NondominatedPopulation;

/**
 * REST controller for handling TSE (Tradespace Search Executive) requests.
 * This controller receives JSON requests, processes them asynchronously,
 * and returns responses with workflow execution results.
 * 
 * @author TSE Development Team
 */
@RestController
@RequestMapping("/tse")
public class DaphneRequestController {
    private static final Logger logger = LoggerFactory.getLogger(DaphneRequestController.class);

    /** Single-threaded executor for processing TSE requests */
    private final ExecutorService executor = Executors.newFixedThreadPool(1);

    /** Timestamp formatter for creating unique output directories */
    private static final DateTimeFormatter TS_FMT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    /**
     * Receives and processes TSE requests asynchronously.
     * 
     * @param rawJson The raw JSON request string
     * @return ResponseEntity containing the TSE response
     */
    @PostMapping(consumes = "application/json", produces = "application/json")
    public ResponseEntity<TSEResponse> receiveTseRequest(@RequestBody String rawJson) {
        // Parse the JSON request
        JSONObject tseRequest = new JSONObject(rawJson);
        
        // Extract and log callback URL if present
        String callbackUrl = tseRequest.optString("callbackUrl", null);
        logger.info("Received TSERequest with callback URL: {}", callbackUrl);

        // Extract workflow ID and create topic
        String wfid = tseRequest.optString("workflowId", "unknown");
        String topic = "TSE" + wfid;
        logger.info("Received TSERequest for workflowId={} → scheduling on topic={}", wfid, topic);

        // Submit the job for asynchronous processing
        Future<TSEResponse> job = executor.submit(() -> {
            try {
                // Create timestamped output directory
                String timestamp = LocalDateTime.now().format(TS_FMT);
                Path outDir = Paths.get("TSE_Module/tse/results", "results_" + timestamp + "_" + wfid);
                Files.createDirectories(outDir);

                // Save the TSERequest to a file in the output directory
                Path requestFile = outDir.resolve("TSERequest.json");
                Files.write(requestFile, rawJson.getBytes());

                // Execute Tradespace Search Executive
                long startTime = System.nanoTime();
                String fullPathArg0 = requestFile.toAbsolutePath().toString();
                String fullPathArg1 = outDir.toAbsolutePath().toString();
                TradespaceSearchExecutive tse = new TradespaceSearchExecutive(fullPathArg0, fullPathArg1);
                tse.run();
                long endTime = System.nanoTime();

                // Log execution time
                logger.info("[{}] Took {} sec", topic, (endTime - startTime) / Math.pow(10, 9));

                // Return success response
                return TSEResponse.success(wfid, outDir.toString());
                
            } catch (Exception e) {
                logger.error("TSE execution failed for workflowId=" + wfid, e);
                return TSEResponse.failure(wfid, e.getMessage());
            }
        });

        try {
            TSEResponse response = job.get();
            return ResponseEntity.ok(response);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("TSE execution failed for workflowId=" + wfid, e);
            return ResponseEntity
                .status(500)
                .body(TSEResponse.failure(wfid, e.getMessage()));
        }
    }
}
