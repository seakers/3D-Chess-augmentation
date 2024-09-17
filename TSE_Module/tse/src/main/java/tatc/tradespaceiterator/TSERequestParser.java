package tatc.tradespaceiterator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.json.JSONArray;


public class TSERequestParser {
    public List<String> getScienceEvaluators(JSONObject tseRequest) {
        JSONArray evaluators = tseRequest.getJSONObject("evaluator").getJSONArray("science_score");
        return evaluators.toList().stream().map(Object::toString).collect(Collectors.toList());
    }

    public List<String> getCostEvaluators(JSONObject tseRequest) {
        JSONArray evaluators = tseRequest.getJSONObject("evaluator").getJSONArray("lifecycle_cost");
        return evaluators.toList().stream().map(Object::toString).collect(Collectors.toList());
    }
    public static void main(String[] args) {
        String jsonFilePath = "TSERequestExample.json";
        try {
            // Read the JSON file into a String
            String content = new String(Files.readAllBytes(Paths.get(jsonFilePath)));

            // Parse the String content into a JSONObject
            JSONObject tseRequest = new JSONObject(content);

            // Create an instance of TSERequestParser
            TSERequestParser parser = new TSERequestParser();

            // Get science evaluators
            List<String> scienceEvaluators = parser.getScienceEvaluators(tseRequest);
            System.out.println("Science Evaluators: " + scienceEvaluators);

            // Get cost evaluators
            List<String> costEvaluators = parser.getCostEvaluators(tseRequest);
            System.out.println("Cost Evaluators: " + costEvaluators);

        } catch (IOException e) {
            System.out.println("Error reading the JSON file: " + e.getMessage());
            e.printStackTrace();
        }
    }
}

