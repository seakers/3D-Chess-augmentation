package tatc.tradespaceiterator;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.json.JSONObject;
public class EvaluationFunctionGenerator {

    public String generateEvaluationClass(TSERequestParser parser, JSONObject tseRequest) throws IOException {
        List<String> scienceEvaluators = parser.getScienceEvaluators(tseRequest);
        List<String> costEvaluators = parser.getCostEvaluators(tseRequest);

        StringBuilder classBuilder = new StringBuilder();
        classBuilder.append("public class GeneratedEvaluator {\n");

        // Add method to evaluate architecture
        classBuilder.append("    public EvaluationResult evaluate(Architecture arch) {\n");

        // Variables to hold results
        classBuilder.append("        double totalScienceScore = 0;\n");
        classBuilder.append("        double lifecycleCost = 0;\n");

        // Add science evaluators
        for (String evaluator : scienceEvaluators) {
            String evaluatorCode = loadTemplate(evaluator + "Evaluator.java.template");
            classBuilder.append(evaluatorCode).append("\n");
            classBuilder.append("        totalScienceScore += evaluate").append(evaluator).append("(arch);\n");
        }

        // Add cost evaluators
        for (String evaluator : costEvaluators) {
            String evaluatorCode = loadTemplate(evaluator + "Evaluator.java.template");
            classBuilder.append(evaluatorCode).append("\n");
            classBuilder.append("        lifecycleCost += evaluate").append(evaluator).append("(arch);\n");
        }

        classBuilder.append("        return new EvaluationResult(totalScienceScore, lifecycleCost);\n");
        classBuilder.append("    }\n"); // End of evaluate method
        classBuilder.append("}\n"); // End of class

        return classBuilder.toString();
    }

    private String loadTemplate(String templateName) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/" + templateName);
        if (inputStream == null) {
            throw new IOException("Template not found: " + templateName);
        }

        // Read the InputStream into a String
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append('\n');
            }
        }
        return content.toString();
    }
}
