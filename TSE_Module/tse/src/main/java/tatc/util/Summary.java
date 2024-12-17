package tatc.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
}
