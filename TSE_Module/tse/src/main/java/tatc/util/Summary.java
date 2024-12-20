package tatc.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.moeaframework.core.Solution;

import tatc.decisions.Decision;

public class Summary {
    public static void writeSummaryFile(Map<String, Double> objectives, Map<String, Object> archVariables, int archIndex) throws IOException {
        //String csvFile = "summary.csv";
        String csvFile = System.getProperty("tatc.output") + File.separator + "summary.csv";
        File file = new File(csvFile);
        boolean fileExists = file.exists();
    
        // Collect headers from archVariables and objectives
        Set<String> variableNames = new LinkedHashSet<>(archVariables.keySet());
        Set<String> objectiveNames = new LinkedHashSet<>(objectives.keySet());
    
        // Prepare to write to CSV
        try (FileWriter csvWriter = new FileWriter(file, true)) { // 'true' enables appending
            // If the file is new, write the header
            if (!fileExists) {
                List<String> header = new ArrayList<>();
                header.add("archIndex"); // Include archIndex in header
                header.addAll(variableNames);
                header.addAll(objectiveNames);
                csvWriter.append(String.join(",", header));
                csvWriter.append("\n");
            }
    
            // Prepare row values
            List<String> rowValues = new ArrayList<>();
            rowValues.add(Integer.toString(archIndex)); // Add archIndex to row
    
            // Add decision variable values
            for (String varName : variableNames) {
                Object value = archVariables.get(varName);
                String valueStr = (value != null) ? value.toString() : "";
                // Escape quotes and handle special characters
                valueStr = valueStr.replace("\"", "\"\"");
                if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                    valueStr = "\"" + valueStr + "\"";
                }
                rowValues.add(valueStr);
            }
    
            // Add objective values
            for (String objName : objectiveNames) {
                Double value = objectives.get(objName);
                String valueStr = (value != null) ? value.toString() : "";
                // Escape quotes and handle special characters
                valueStr = valueStr.replace("\"", "\"\"");
                if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                    valueStr = "\"" + valueStr + "\"";
                }
                rowValues.add(valueStr);
            }
    
            // Write the row to the CSV file
            csvWriter.append(String.join(",", rowValues));
            csvWriter.append("\n");
        }
    }

public static void writeSummaryFileGA(Map<String, Double> objectives, Solution solution, int archIndex, List<Decision> decisions) throws IOException {
    // Path to the CSV file
    String csvFile = System.getProperty("tatc.output") + File.separator + "summary.csv";
    File file = new File(csvFile);
    boolean fileExists = file.exists();

    // Decode the solution to extract architectural decision variables
    Map<String, Object> archVariables = decodeSolution(solution, decisions);

    // Collect headers from archVariables and objectives
    Set<String> variableNames = new LinkedHashSet<>(archVariables.keySet());
    Set<String> objectiveNames = new LinkedHashSet<>(objectives.keySet());

    // Prepare to write to CSV
    try (FileWriter csvWriter = new FileWriter(file, true)) { // 'true' enables appending
        // If the file is new, write the header
        if (!fileExists) {
            List<String> header = new ArrayList<>();
            header.add("archIndex"); // Include archIndex in header
            header.addAll(variableNames); // Variable names
            header.addAll(objectiveNames); // Objective names
            csvWriter.append(String.join(",", header));
            csvWriter.append("\n");
        }

        // Prepare row values
        List<String> rowValues = new ArrayList<>();
        rowValues.add(Integer.toString(archIndex)); // Add archIndex to row

        // Add decision variable values
        for (String varName : variableNames) {
            Object value = archVariables.get(varName);
            String valueStr = (value != null) ? value.toString() : "";
            // Escape quotes and handle special characters
            valueStr = valueStr.replace("\"", "\"\"");
            if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                valueStr = "\"" + valueStr + "\"";
            }
            rowValues.add(valueStr);
        }

        // Add objective values
        for (String objName : objectiveNames) {
            Double value = objectives.get(objName);
            String valueStr = (value != null) ? value.toString() : "";
            // Escape quotes and handle special characters
            valueStr = valueStr.replace("\"", "\"\"");
            if (valueStr.contains(",") || valueStr.contains("\"") || valueStr.contains("\n")) {
                valueStr = "\"" + valueStr + "\"";
            }
            rowValues.add(valueStr);
        }

        // Write the row to the CSV file
        csvWriter.append(String.join(",", rowValues));
        csvWriter.append("\n");
    }
}
/**
 * Decodes the Solution to extract architecture variables using the list of decisions.
 *
 * @param solution  The solution to be decoded.
 * @param decisions The list of decisions in the problem.
 * @return A map where keys are variable names and values are the corresponding decision values.
 */
private static Map<String, Object> decodeSolution(Solution solution, List<Decision> decisions) {
    Map<String, Object> archVariables = new HashMap<>();
    int offset = 0;

    for (Decision d : decisions) {
        // Extract the encoding for this decision from the solution
        Object encoded = d.extractEncodingFromSolution(solution, offset);
        offset += d.getNumberOfVariables();

        // Decode the encoding into an architectural variable
        List<Map<String, Object>> decodedArchs = d.decodeArchitecture(encoded, Collections.singletonList(new HashMap<>()));
        
        // Extract variable values for this decision
        for (Map<String, Object> arch : decodedArchs) {
            archVariables.putAll(arch);
        }
    }

    return archVariables;
}


}
