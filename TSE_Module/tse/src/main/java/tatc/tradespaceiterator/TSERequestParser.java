package tatc.tradespaceiterator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.json.JSONObject;
import org.json.JSONArray;


public class TSERequestParser {
public Map<String, List<String>> getEvaluatorsForObjective(JSONObject tseRequest, String objectiveKey) {
        Map<String, List<String>> evaluatorsAndMetrics = new HashMap<>();
        JSONObject objectives = tseRequest.getJSONObject("evaluator").getJSONObject("objectives");

        if (objectives.has(objectiveKey)) {
            JSONArray evaluators = objectives.getJSONObject(objectiveKey).getJSONArray("evaluators");

            for (int i = 0; i < evaluators.length(); i++) {
                JSONObject evaluator = evaluators.getJSONObject(i);
                String evaluatorName = evaluator.getString("name");
                List<String> metrics = evaluator.getJSONArray("metrics").toList().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());

                evaluatorsAndMetrics.put(evaluatorName, metrics);
            }
        }

        return evaluatorsAndMetrics;
    }

    // To get evaluators and metrics for the science_score objective
    public Map<String, List<String>> getScienceEvaluators(JSONObject tseRequest) {
        return getEvaluatorsForObjective(tseRequest, "science_score");
    }

    // To get evaluators and metrics for the lifecycle_cost objective
    public Map<String, List<String>> getCostEvaluators(JSONObject tseRequest) {
        return getEvaluatorsForObjective(tseRequest, "lifecycle_cost");
    }
}

