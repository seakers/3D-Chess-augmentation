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

    public static void writeSummaryFileGA(
        Map<String, Double> objectives,
        Solution solution,
        int archIndex,
        List<Decision> decisions) throws IOException {

    String csvFile = System.getProperty("tatc.output") + File.separator + "summary.csv";
    File file = new File(csvFile);
    boolean fileExists = file.exists();

    // --- 1) Identify the number of variables in this solution ---
    int nVars = solution.getNumberOfVariables();

    // --- 2) Collect objective names ---
    List<String> objectiveNames = new ArrayList<>(objectives.keySet());

    // --- 3) If this is the first time (file doesn't exist), write the header ---
    // archIndex, var0, var1, ..., var(nVars-1), objective1, objective2, ...
    try (FileWriter csvWriter = new FileWriter(file, true)) {

        if (!fileExists) {
            List<String> header = new ArrayList<>();
            header.add("archIndex");
            for (int i = 0; i < nVars; i++) {
                header.add("var" + i);
            }
            header.addAll(objectiveNames);
            csvWriter.append(String.join(",", header)).append("\n");
        }

        // --- 4) Prepare the row for this solution ---
        List<String> rowValues = new ArrayList<>();
        rowValues.add(Integer.toString(archIndex));  // archIndex

        // 4A) Add the solution's variable values
        for (int i = 0; i < nVars; i++) {
            // Attempt to interpret the variable as a double or int, depending on your representation
            double value = 0.0;
            try {
                // If you're using RealVariable or EncodingUtils.newInt(...) => can interpret as double
                value = ((org.moeaframework.core.variable.RealVariable) solution.getVariable(i)).getValue();
            } catch (ClassCastException e) {
                // If using a different variable type, handle accordingly.
                // E.g., if it's a BinaryIntegerVariable or an IntegerVariable
                // ...
                throw new IllegalStateException("Unexpected variable type at index " + i, e);
            }
            // Convert to string safely
            String valueStr = String.valueOf(value);
            // Escape quotes, commas, etc. if needed
            valueStr = safeForCSV(valueStr);
            rowValues.add(valueStr);
        }

        // 4B) Add the objective values
        for (String objName : objectiveNames) {
            Double val = objectives.getOrDefault(objName, Double.NaN);
            String valStr = safeForCSV(val.toString());
            rowValues.add(valStr);
        }

        // --- 5) Write the row to CSV ---
        csvWriter.append(String.join(",", rowValues)).append("\n");
    }
}

/**
 * Utility function to escape any commas, quotes, or newlines in a CSV field.
 * Adjust as needed for your data format.
 */
private static String safeForCSV(String field) {
    if (field == null) {
        return "";
    }
    field = field.replace("\"", "\"\"");
    if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
        field = "\"" + field + "\"";
    }
    return field;
}

}
