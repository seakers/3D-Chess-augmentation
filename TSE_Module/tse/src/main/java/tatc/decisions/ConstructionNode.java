package tatc.decisions;

import tatc.decisions.adg.Graph;
import tatc.tradespaceiterator.ProblemProperties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.moeaframework.core.Solution;

import java.util.*;

/**
 * ConstructionNode is a special "decision" that does not encode an actual
 * design variable. Instead, it assembles outputs from other decisions into
 * one final architecture object. This helps keep decode logic clean by
 * centralizing how partial decisions (e.g., orbits, payload selections) are
 * merged.
 */
public class ConstructionNode extends Decision {

    /**
     * A structure (parsed from the JSON TSERequest) describing how to
     * assemble parent decisions' results into the final architecture.
     * For example:
     * 
     *  "constructionNode": {
     *      "orbits": {
     *          "fullObject": "orbitSelection"
     *      },
     *      "satellites": {
     *          "payload": "instrumentPartitioning"
     *      }
     *  }
     * 
     * The keys (e.g., "orbits", "satellites") are top-level fields to populate
     * in the final architecture, and the values indicate which parent decision
     * or field within that decision to retrieve.
     */
    private Map<String, Object> constructionDefinition;

    /**
     * Constructor
     * 
     * @param properties    The problem properties (parsed TSERequest).
     * @param decisionName  The name/key for this construction node (e.g., "constructionNode").
     */
    public ConstructionNode(ProblemProperties properties, String decisionName) {
        super(properties, decisionName);
    }
    @Override
    public void initializeDecisionVariables() {
        // Get the root JSON object from ProblemProperties
        JSONObject tsrObject = properties.getTsrObject();

        if (tsrObject != null && tsrObject.has("designSpace")) {
            JSONObject designSpace = tsrObject.getJSONObject("designSpace");

            if (designSpace.has("decisionVariables")) {
                JSONObject decisionVars = designSpace.getJSONObject("decisionVariables");

                // Extract this specific decision's node definition
                if (decisionVars.has(this.decisionName)) {
                    JSONObject nodeDef = decisionVars.getJSONObject(this.decisionName);
                    this.constructionDefinition = jsonToMap(nodeDef);
                } else {
                    this.constructionDefinition = new HashMap<>();
                }
            } else {
                this.constructionDefinition = new HashMap<>();
            }
        } else {
            this.constructionDefinition = new HashMap<>();
        }
    }
    
    /**
     * This node does not enumerate anything by itself.
     */
    @Override
    public Iterable<Map<String, Object>> enumerateArchitectures() {
        // Construction node doesn't generate new combinations;
        // it only assembles them. Return an empty list or
        // possibly a single empty map.
        return Collections.emptyList();
    }

    /**
     * Construction node doesn't encode an architecture into variables.
     */
    @Override
    public Object encodeArchitecture(Map<String, Object> architecture) {
        // Not applicable for a node that simply merges parent results.
        return null;
    }

    @Override
    public List<Map<String, Object>> decodeArchitecture(Object encoded, Solution sol, Graph graph) {
        // Final aggregated architectures
        List<Map<String, Object>> finalArchitectures = new ArrayList<>();
    
        // 1) Retrieve the "constellation" definition from constructionDefinition.
        Object constellationObj = this.constructionDefinition.get("constellation");
        if (!(constellationObj instanceof Map)) {
            return finalArchitectures; // nothing to aggregate
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> constellationDef = (Map<String, Object>) constellationObj;
        // For example:
        // { "orbit": { "fullObject": "orbitSelection" },
        //   "satellites": { "payload": "instrumentPartitioning" } }
    
        // 2) Build a map: fieldName -> List<Object> partials (retrieved from parent's getResult() or literal)
        Map<String, List<Object>> fieldToListOfPartials = new HashMap<>();
        for (Map.Entry<String, Object> fieldEntry : constellationDef.entrySet()) {
            String fieldName = fieldEntry.getKey();  // e.g., "orbit" or "satellites"
            Object definition = fieldEntry.getValue();
            List<Object> partials = retrievePartialsFromDefinition(definition, graph);
            fieldToListOfPartials.put(fieldName, partials);
        }
    
        // 3) Determine the number of pairs to produce using element-to-element pairing.
        //    We assume that all fields have the same number of elements;
        //    otherwise, we use the minimum size.
        int pairCount = Integer.MAX_VALUE;
        for (List<Object> list : fieldToListOfPartials.values()) {
            if (list.size() < pairCount) {
                pairCount = list.size();
            }
        }
        if (pairCount == Integer.MAX_VALUE) {
            pairCount = 0;
        }
        if (pairCount == 0) {
            return finalArchitectures; // Nothing to pair
        }
    
        // 4) For each index from 0 to pairCount-1, build one final architecture.
        //    For each field, retrieve the i-th partial result.
        for (int i = 0; i < pairCount; i++) {
            Map<String, Object> finalArch = new HashMap<>();
            for (String fieldName : fieldToListOfPartials.keySet()) {
                Object partialObj = fieldToListOfPartials.get(fieldName).get(i);
                if ("satellites".equalsIgnoreCase(fieldName)) {
                    // For satellites, merge with baseline satellite data from the TSERequest.
                    JSONArray baselineSatellites = getBaselineSatellitesFromTSERequest();
                    // If baselineSatellites is null, use an empty array.
                    if (baselineSatellites == null) {
                        baselineSatellites = new JSONArray();
                    }
                    // Build the final satellites list by cloning each baseline satellite and replacing "payload"
                    // with the i-th partial result (which should be a List or Map, as returned by instrumentPartitioning).
                    List<Map<String, Object>> satList = buildSatellitesWithPayload(baselineSatellites, partialObj);
                    finalArch.put("satellites", satList);
                } else {
                    // For other fields (like "orbit"), extract the main object if needed.
                    finalArch.put(fieldName, extractMainObjectIfNeeded(fieldName, partialObj));
                }
            }
            finalArchitectures.add(finalArch);
        }
    
        return finalArchitectures;
    }
    
    /**
     * Helper: Given a definition such as { "fullObject": "orbitSelection" } or { "payload": "instrumentPartitioning" },
     * retrieve the corresponding List<Object> from the referenced parent's getResult().
     * If the definition is not a Map, treat it as a literal and return a single-element list.
     */
    private List<Object> retrievePartialsFromDefinition(Object definition, Graph graph) {
        if (definition instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> defMap = (Map<String, Object>) definition;
            if (defMap.size() == 1) {
                String parentName = defMap.values().iterator().next().toString();
                Decision parentDec = graph.getDecision(parentName);
                if (parentDec != null && parentDec.getResult() != null) {
                    return parentDec.getResult();
                } else {
                    return new ArrayList<>();
                }
            } else {
                return new ArrayList<>();
            }
        } else {
            return Collections.singletonList(definition);
        }
    }
    
    /**
     * If fieldName is "orbit" (or similar), and partialObj is a Map containing that key,
     * unwrap it; otherwise, return partialObj as is.
     */
    private Object extractMainObjectIfNeeded(String fieldName, Object partialObj) {
        if (partialObj instanceof Map) {
            Map<?, ?> mapObj = (Map<?, ?>) partialObj;
            if (mapObj.containsKey(fieldName)) {
                return mapObj.get(fieldName);
            }
        }
        return partialObj;
    }
    
    /**
     * Returns TSERequest's baseline satellites as a JSONArray.
     */
    private JSONArray getBaselineSatellitesFromTSERequest() {
        JSONObject tseRequest = properties.getTsrObject();
        if (tseRequest == null) return null;
        JSONObject designSpace = tseRequest.optJSONObject("designSpace");
        if (designSpace == null) return null;
        JSONArray spaceSegments = designSpace.optJSONArray("spaceSegment");
        if (spaceSegments == null || spaceSegments.length() == 0) return null;
        JSONObject firstSegment = spaceSegments.optJSONObject(0);
        if (firstSegment == null) return null;
        return firstSegment.optJSONArray("satellites");
    }
    
    /**
     * Build the final satellites list by cloning each baseline satellite and
     * replacing its "payload" field with the provided partial result.
     * Assumes that partialObj is a List (for decision variables) or a Map with a "payload" key.
     */
    private List<Map<String, Object>> buildSatellitesWithPayload(JSONArray baselineSats, Object partialObj) {
        List<Map<String, Object>> resultSats = new ArrayList<>();
        for (int i = 0; i < baselineSats.length(); i++) {
            JSONObject satJson = new JSONObject(baselineSats.getJSONObject(i).toString());
            // If partialObj is a List, assign that list as payload.
            if (partialObj instanceof List) {
                satJson.put("payload", partialObj);
            } else if (partialObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> pMap = (Map<String, Object>) partialObj;
                if (pMap.containsKey("payload")) {
                    satJson.put("payload", pMap.get("payload"));
                }
            }
            resultSats.add(jsonToMap(satJson));
        }
        return resultSats;
    }
    
    /**
     * Convert a JSONObject to a Map<String,Object>.
     */
    private Map<String, Object> jsonToMap(JSONObject obj) {
        Map<String, Object> map = new HashMap<>();
        for (String key : obj.keySet()) {
            Object val = obj.get(key);
            if (val instanceof JSONObject) {
                val = jsonToMap((JSONObject) val);
            } else if (val instanceof JSONArray) {
                val = jsonToList((JSONArray) val);
            }
            map.put(key, val);
        }
        return map;
    }
    
    /**
     * Convert a JSONArray to a List<Object>.
     */
    private List<Object> jsonToList(JSONArray arr) {
        List<Object> list = new ArrayList<>();
        for (int i = 0; i < arr.length(); i++) {
            Object val = arr.get(i);
            if (val instanceof JSONObject) {
                val = jsonToMap((JSONObject) val);
            } else if (val instanceof JSONArray) {
                val = jsonToList((JSONArray) val);
            }
            list.add(val);
        }
        return list;
    }
    

    
    /**
     * Helper method:
     *  - If definition = { "fullObject": "someDecision" } or { "payload": "anotherDecision" },
     *    fetch that parent's getResult() => returns List<Object>.
     *  - Otherwise, returns empty list.
     */
    private List<Object> retrieveParentResult(Map<String, Object> definition, Graph graph) {
        if (definition == null || definition.size() != 1) {
            return Collections.emptyList();
        }
        // e.g. definition = { "fullObject" : "orbitSelection" } => parentName="orbitSelection"
        String parentName = definition.values().iterator().next().toString();
        Decision parentD = graph.getDecision(parentName);
        if (parentD != null && parentD.getResult() != null) {
            return parentD.getResult();
        }
        return Collections.emptyList();
    }
    

    /**
     * Construction node does not have variables to mutate.
     */
    @Override
    public void mutate(Object encoded) {
        // No-op
    }

    /**
     * Construction node does not have variables to crossover.
     */
    @Override
    public Object crossover(Object parent1, Object parent2) {
        // No-op
        return null;
    }

    /**
     * Returns zero because there are no integer variables to encode in a
     * construction node.
     */
    @Override
    public int getNumberOfVariables() {
        return 0;
    }

    /**
     * There is no "random encoding" here, so return null or empty.
     */
    @Override
    public Object randomEncoding() {
        return null;
    }

    /**
     * No encoded variables => return 0.
     */
    @Override
    public int getMaxOptionForVariable(int i) {
        return 0;
    }

    /**
     * Construction node has nothing to extract from the MOEA solution,
     * because it doesn't contribute to the chromosome.
     */
    @Override
    public Object extractEncodingFromSolution(Solution solution, int offset) {
        return null;
    }

    /**
     * Construction node can still have parents in the ADG to gather their results,
     * so this method is standard.
     */
    @Override
    public void addParentDecision(Decision decision) {
        parentDecisions.add(decision);
    }

    /**
     * No encoding to store, so return null.
     */
    @Override
    public int[] getLastEncoding() {
        return null;
    }

    /**
     * Construction node doesn't repair encodings — no child/parent enc mismatch
     * in the usual sense.
     */
    @Override
    public Object repairWithDependency(Object childEnc, Object parentEnc) {
        return childEnc;
    }

    /**
     * Construction node doesn't store an encoding to apply.
     */
    @Override
    public void applyEncoding(int[] encoding) {
        // No-op
    }
}
