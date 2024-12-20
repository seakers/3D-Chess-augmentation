package tatc.decisions.adg;

import org.json.JSONArray;
import org.json.JSONObject;
import tatc.decisions.*;
import tatc.tradespaceiterator.ProblemProperties;

import java.util.*;

/**
 * The Graph class represents the Architecture Decision Graph (ADG) constructed from the TSERequest.
 * It:
 * - Extracts decision variables and their dependencies (parents) from TSERequest.
 * - Builds a directed graph of decision nodes.
 * - Performs a topological sort to determine processing order.
 * - Instantiates Decision objects (e.g., Combining, Assigning) with the appropriate parameters.
 */
public class Graph {

    private ProblemProperties properties;
    private Map<String, Decision> decisions;
    private List<Decision> topoOrderedDecisions;

    public Graph(ProblemProperties properties) {
        this.properties = properties;
        this.decisions = new LinkedHashMap<>();
        this.topoOrderedDecisions = new ArrayList<>();
        buildGraphFromTSE();
    }

    private void buildGraphFromTSE() {
        // Extract decision variables object
        JSONObject tseRequest = properties.getTsrObject();
        JSONObject designSpace = tseRequest.getJSONObject("designSpace");
        JSONObject decisionVariablesObject = designSpace.getJSONObject("decisionVariables");

        // Parse each decision node definition
        // We'll first store the graph structure in adjacency lists form
        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, JSONObject> decisionJSONMap = new HashMap<>();

        // Collect all decisions and their JSON definitions
        for (String decisionName : decisionVariablesObject.keySet()) {
            JSONObject decObj = decisionVariablesObject.getJSONObject(decisionName);
            decisionJSONMap.put(decisionName, decObj);
            inDegree.put(decisionName, 0); // init in-degree
        }

        // Build graph edges based on "parents"
        for (String decisionName : decisionJSONMap.keySet()) {
            JSONObject decObj = decisionJSONMap.get(decisionName);

            // "parents" may be empty or array of parent decisions
            List<String> parents = new ArrayList<>();
            if (decObj.has("parents")) {
                Object parentField = decObj.get("parents");
                if (parentField instanceof JSONArray) {
                    JSONArray parArr = (JSONArray) parentField;
                    for (int i = 0; i < parArr.length(); i++) {
                        parents.add(parArr.getString(i));
                    }
                } else if (parentField instanceof String) {
                    // If parents is a single string (some older versions?), adapt if needed.
                    parents.add(decObj.getString("parents"));
                }
            }

            // For each parent, add edge parent -> decisionName
            // If no parents, it means this is a root decision
            for (String p : parents) {
                adjacency.computeIfAbsent(p, k -> new ArrayList<>()).add(decisionName);
                // Increase in-degree for the child
                inDegree.put(decisionName, inDegree.getOrDefault(decisionName, 0) + 1);
            }
        }

        // Ensure all nodes appear in adjacency (even if no outgoing edges)
        for (String dn : decisionJSONMap.keySet()) {
            adjacency.putIfAbsent(dn, new ArrayList<>());
        }

        // Topological sort using Kahn's algorithm
        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
            if (e.getValue() == 0) {
                queue.add(e.getKey());
            }
        }

        List<String> topoOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.remove();
            topoOrder.add(node);
            for (String child : adjacency.get(node)) {
                int deg = inDegree.get(child) - 1;
                inDegree.put(child, deg);
                if (deg == 0) {
                    queue.add(child);
                }
            }
        }

        if (topoOrder.size() != decisionJSONMap.size()) {
            throw new IllegalArgumentException("Cycle detected in decision graph or missing nodes. Cannot topologically sort.");
        }

        // Now instantiate each Decision object according to its type and parameters
        for (String decisionName : topoOrder) {
            JSONObject decObj = decisionJSONMap.get(decisionName);
            String type = decObj.getString("type");

            Decision d = null;
            if (type.equalsIgnoreCase("Combining")) {
                // Create a Combining decision
                Combining comb = new Combining(properties, decisionName);
                // Extract subDecisions
                JSONArray subDecs = decObj.getJSONArray("combiningDecisions");
                List<String> subDecisionNames = new ArrayList<>();
                for (int i = 0; i < subDecs.length(); i++) {
                    subDecisionNames.add(subDecs.getString(i));
                }
                comb.setSubDecisions(subDecisionNames);

                // Fetch distinct values for each subVar from the problemProperties
                List<List<Object>> alternatives = new ArrayList<>();
                for (String subVar : subDecisionNames) {
                    List<Object> subVarValues = properties.getDistinctValuesForVariable(subVar);
                    if (subVarValues == null || subVarValues.isEmpty()) {
                        throw new IllegalArgumentException("No values found for sub-variable " + subVar + " in combining decision " + decisionName);
                    }
                    alternatives.add(subVarValues);
                }
                comb.setAlternatives(alternatives);
                comb.initializeDecisionVariables();
                d = comb;
            } else if (type.equalsIgnoreCase("Assigning")) {
                JSONObject varObj = decObj;
                JSONArray parentsArray = varObj.optJSONArray("parents");
            
                List<String> parentNames = new ArrayList<>();
                if (parentsArray != null) {
                    for (int i = 0; i < parentsArray.length(); i++) {
                        parentNames.add(parentsArray.getString(i));
                    }
                }
            
                // Assume one parent for simplicity; if multiple, handle accordingly
                Decision parentDecision = null;
                for (String pname : parentNames) {
                    if (decisions.containsKey(pname)) {
                        parentDecision = decisions.get(pname);
                        break;
                    }
                }
            
                if (parentDecision == null && !parentNames.isEmpty()) {
                    throw new IllegalArgumentException("Parent decision(s) " + parentNames + " not found for assigning decision " + decisionName);
                }
            
                // Enumerate parent's architectures to form Lset
                // Parent's enumerateArchitectures() returns an iterable of Maps
                List<Map<String,Object>> parentArchs = new ArrayList<>();
                if (parentDecision != null) {
                    for (Object archObj : parentDecision.enumerateArchitectures()) {
                        parentArchs.add((Map<String,Object>) archObj);
                    }
                } else {
                    // If no parent, we must define some Lset differently.
                    // For now, assume that Assigning always has a parent. 
                    // If needed, handle the no-parent scenario.
                    throw new IllegalArgumentException("Assigning decision " + decisionName + " has no parent. Cannot determine Lset.");
                }
            
                List<Object> Lset = new ArrayList<>(parentArchs);
            
                // For Rset, we fetch distinct values for this decision variable
                // e.g., if decisionName = "payload", we get distinct payload options
                List<Object> Rset = properties.getDistinctValuesForVariable(decisionName);
                if (Rset == null || Rset.isEmpty()) {
                    throw new IllegalArgumentException("No distinct values found for Rset in assigning decision " + decisionName);
                }
            
                tatc.decisions.Assigning assign = new tatc.decisions.Assigning(properties, decisionName);
                assign.setLset(Lset);
                assign.setRset(Rset);
                assign.initializeDecisionVariables();
                d = assign;
            } else {
                // Other patterns (Partitioning, DownSelecting, etc.) can be similarly implemented
                throw new UnsupportedOperationException("Decision type " + type + " not implemented yet.");
            }

            this.decisions.put(decisionName, d);
        }

        // Store decisions in topological order
        for (String dn : topoOrder) {
            topoOrderedDecisions.add(decisions.get(dn));
        }
    }

    /**
     * Returns the decisions in topological order.
     */
    public List<Decision> getTopoOrderedDecisions() {
        return topoOrderedDecisions;
    }

    /**
     * Gets the decision object by name.
     */
    public Decision getDecision(String name) {
        return decisions.get(name);
    }

    /**
     * Returns all decisions as a map.
     */
    public Map<String, Decision> getDecisionsMap() {
        return decisions;
    }
}
