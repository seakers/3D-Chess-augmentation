package tatc.tradespaceiterator;

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
}

