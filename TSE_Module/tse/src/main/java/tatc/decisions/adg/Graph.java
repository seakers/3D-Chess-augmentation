package tatc.decisions.adg;

import org.json.JSONArray;
import org.json.JSONObject;
import org.moeaframework.core.variable.RealVariable;

import jmetal.core.Solution;
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

    // private void buildGraphFromTSE() {
    //     // Extract the top-level TSE request object and the 'designSpace' / 'decisionVariables' subobjects
    //     JSONObject tseRequest = properties.getTsrObject();
    //     JSONObject designSpace = tseRequest.getJSONObject("designSpace");
    //     JSONObject decisionVariablesObject = designSpace.getJSONObject("decisionVariables");
    
    //     // We'll store adjacency info, in-degrees, plus JSON definitions
    //     Map<String, List<String>> adjacency = new HashMap<>();
    //     Map<String, Integer> inDegree = new HashMap<>();
    //     Map<String, JSONObject> decisionJSONMap = new HashMap<>();
    
    //     // Collect all decisions and their JSON definitions
    //     for (String decisionName : decisionVariablesObject.keySet()) {
    //         JSONObject decObj = decisionVariablesObject.getJSONObject(decisionName);
    //         decisionJSONMap.put(decisionName, decObj);
    //         inDegree.put(decisionName, 0); // initialize in-degree
    //     }
    
    //     // Build graph edges based on "parents" array or field
    //     for (String decisionName : decisionJSONMap.keySet()) {
    //         JSONObject decObj = decisionJSONMap.get(decisionName);
    
    //         // "parents" may be empty or array of parent decisions
    //         List<String> parents = new ArrayList<>();
    //         if (decObj.has("parents")) {
    //             Object parentField = decObj.get("parents");
    //             if (parentField instanceof JSONArray) {
    //                 JSONArray parArr = (JSONArray) parentField;
    //                 for (int i = 0; i < parArr.length(); i++) {
    //                     parents.add(parArr.getString(i));
    //                 }
    //             } else if (parentField instanceof String) {
    //                 // If 'parents' is a single string
    //                 parents.add(decObj.getString("parents"));
    //             }
    //         }
    
    //         // For each parent, add edge: parent -> decisionName
    //         for (String p : parents) {
    //             adjacency.computeIfAbsent(p, k -> new ArrayList<>()).add(decisionName);
    //             // Increase in-degree for the child
    //             inDegree.put(decisionName, inDegree.getOrDefault(decisionName, 0) + 1);
    //         }
    //     }
    
    //     // Ensure all nodes appear in adjacency (even if no outgoing edges)
    //     for (String dn : decisionJSONMap.keySet()) {
    //         adjacency.putIfAbsent(dn, new ArrayList<>());
    //     }
    
    //     // Kahn's algorithm for topological sort
    //     Queue<String> queue = new LinkedList<>();
    //     for (Map.Entry<String, Integer> e : inDegree.entrySet()) {
    //         if (e.getValue() == 0) {
    //             queue.add(e.getKey());
    //         }
    //     }
    
    //     List<String> topoOrder = new ArrayList<>();
    //     while (!queue.isEmpty()) {
    //         String node = queue.remove();
    //         topoOrder.add(node);
    //         for (String child : adjacency.get(node)) {
    //             int deg = inDegree.get(child) - 1;
    //             inDegree.put(child, deg);
    //             if (deg == 0) {
    //                 queue.add(child);
    //             }
    //         }
    //     }
    
    //     if (topoOrder.size() != decisionJSONMap.size()) {
    //         throw new IllegalArgumentException("Cycle detected in decision graph or missing nodes. Cannot topologically sort.");
    //     }
    
    //     // Now instantiate each Decision object according to its type and parameters
    //     for (String decisionName : topoOrder) {
    //         JSONObject decObj = decisionJSONMap.get(decisionName);
    //         String type = decObj.getString("type");
    
    //         Decision d = null;
    
    //         // 1) COMBINING
    //         if (type.equalsIgnoreCase("Combining")) {
    //             Combining comb = new Combining(properties, decisionName);
    
    //             // subDecisions from "combiningDecisions" field
    //             JSONArray subDecs = decObj.getJSONArray("combiningDecisions");
    //             List<String> subDecisionNames = new ArrayList<>();
    //             for (int i = 0; i < subDecs.length(); i++) {
    //                 subDecisionNames.add(subDecs.getString(i));
    //             }
    //             comb.setSubDecisions(subDecisionNames);
    
    //             // fetch distinct values for each sub-variable
    //             List<List<Object>> alternatives = new ArrayList<>();
    //             for (String subVar : subDecisionNames) {
    //                 List<Object> subVarValues = properties.getDistinctValuesForVariable(subVar);
    //                 if (subVarValues == null || subVarValues.isEmpty()) {
    //                     throw new IllegalArgumentException(
    //                         "No values found for sub-variable " + subVar
    //                         + " in combining decision " + decisionName
    //                     );
    //                 }
    //                 alternatives.add(subVarValues);
    //             }
    //             comb.setAlternatives(alternatives);
    
    //             // Optional: get parents if "parents" is defined, store them for reference
    //             if (decObj.has("parents")) {
    //                 // parse array or string, store it if needed
    //                 // comb.setParentNames(...);
    //             }
    
    //             comb.initializeDecisionVariables();
    //             d = comb;
    //         }
    
    //         // 2) ASSIGNING
    //         else if (type.equalsIgnoreCase("Assigning")) {
    //             JSONArray parentsArray = decObj.optJSONArray("parents");
    //             List<String> parentNames = new ArrayList<>();
    //             if (parentsArray != null) {
    //                 for (int i = 0; i < parentsArray.length(); i++) {
    //                     parentNames.add(parentsArray.getString(i));
    //                 }
    //             }
    
    //             // For simplicity assume single parent
    //             Decision parentDecision = null;
    //             for (String pname : parentNames) {
    //                 if (decisions.containsKey(pname)) {
    //                     parentDecision = decisions.get(pname);
    //                     break;
    //                 }
    //             }
    //             if (parentDecision == null && !parentNames.isEmpty()) {
    //                 throw new IllegalArgumentException(
    //                     "Parent decision(s) " + parentNames
    //                     + " not found for assigning decision " + decisionName
    //                 );
    //             }
    
    //             // We'll create the Lset from parent's enumerations
    //             List<Map<String, Object>> parentArchs = new ArrayList<>();
    //             if (parentDecision != null) {
    //                 for (Object archObj : parentDecision.enumerateArchitectures()) {
    //                     parentArchs.add((Map<String, Object>) archObj);
    //                 }
    //             } else {
    //                 // No parent
    //                 throw new IllegalArgumentException("Assigning decision " + decisionName
    //                     + " requires a parent to form Lset.");
    //             }
    
    //             List<Object> Lset = new ArrayList<>(parentArchs);
    
    //             // Rset from distinct values
    //             List<Object> Rset = properties.getDistinctValuesForVariable(decisionName);
    //             if (Rset == null || Rset.isEmpty()) {
    //                 throw new IllegalArgumentException("No distinct values found for Rset in assigning decision " + decisionName);
    //             }
    
    //             Assigning assign = new Assigning(properties, decisionName);
    //             assign.setLset(Lset);
    //             assign.setRset(Rset);
    //             // Store the parent's reference if needed, e.g.:
    //             assign.addParentDecision(parentDecision);
    
    //             assign.initializeDecisionVariables();
    //             d = assign;
    //         }
    
    //         // 3) DOWNSELECTING
    //         else if (type.equalsIgnoreCase("DownSelecting")) {
    //             // Typically, downselecting has no direct parent or zero parents, 
    //             // but it's possible it has a parent we want to reference
    //             JSONArray parentsArray = decObj.optJSONArray("parents");
    //             DownSelecting downSelect = new DownSelecting(properties, decisionName);
    
    //             if (parentsArray != null && parentsArray.length() > 0) {
    //                 // If it has a parent, we can find that parent decision's enumerations
    //                 // then unify them into a single set E to possibly downselect from
    //                 List<String> parentNames = new ArrayList<>();
    //                 for (int i = 0; i < parentsArray.length(); i++) {
    //                     parentNames.add(parentsArray.getString(i));
    //                 }
    
    //                 Decision parent = null;
    //                 for (String pname : parentNames) {
    //                     if (decisions.containsKey(pname)) {
    //                         parent = decisions.get(pname);
    //                         break;
    //                     }
    //                 }
    //                 if (parent == null && !parentNames.isEmpty()) {
    //                     throw new IllegalArgumentException("DownSelecting decision " + decisionName
    //                         + " references unknown parent(s): " + parentNames);
    //                 }
    
    //                 List<Object> combinedEntityList = new ArrayList<>();
    //                 if (parent != null) {
    //                     for (Object archObj : parent.enumerateArchitectures()) {
    //                         // e.g. each archObj is a Map<String,Object>
    //                         combinedEntityList.add(archObj);
    //                     }
    //                 }
    
    //                 if (combinedEntityList.isEmpty()) {
    //                     throw new IllegalArgumentException("Parent for downselecting " + decisionName
    //                         + " yielded no enumerations.");
    //                 }
    
    //                 downSelect.setEntities(combinedEntityList);
    //             } else {
    //                 // If there's no parent, we get the set from distinct values on the decisionName
    //                 List<Object> entitySet = properties.getDistinctValuesForVariable(decisionName);
    //                 if (entitySet == null || entitySet.isEmpty()) {
    //                     throw new IllegalArgumentException("No distinct values found for DownSelecting decision " + decisionName);
    //                 }
    //                 downSelect.setEntities(entitySet);
    //             }
    
    //             downSelect.initializeDecisionVariables();
    //             d = downSelect;
    //         }
    
    //         // 4) PARTITIONING
    //         else if (type.equalsIgnoreCase("Partitioning")) {
    //             Partitioning partition = new Partitioning(properties, decisionName);
            
    //             // Extract parents for this decision
    //             JSONArray parentsArrayPA = decObj.optJSONArray("parents");
    //             List<String> parentNames = new ArrayList<>();
    //             if (parentsArrayPA != null) {
    //                 for (int i = 0; i < parentsArrayPA.length(); i++) {
    //                     parentNames.add(parentsArrayPA.getString(i));
    //                 }
    //             }
            
    //             // Assume single parent for simplicity
    //             Decision parentDecisionPA = null;
    //             for (String pname : parentNames) {
    //                 if (decisions.containsKey(pname)) {
    //                     parentDecisionPA = decisions.get(pname);
    //                     break;
    //                 }
    //             }
            
    //             if (parentDecisionPA == null && !parentNames.isEmpty()) {
    //                 throw new IllegalArgumentException("Parent decision(s) " + parentNames + " not found for partitioning decision " + decisionName);
    //             }
            
    //             // Set the parent decision
    //             partition.addParentDecision(parentDecisionPA);
            
    //             // Initialize entities (E) from the TSERequest's "entities" field
    //             List<Map<String, Object>> entityMaps = new ArrayList<>();
    //             if (decObj.has("entities")) {
    //                 JSONArray entitiesArray = decObj.getJSONArray("entities");
    //                 for (int i = 0; i < entitiesArray.length(); i++) {
    //                     String entityKey = entitiesArray.getString(i);
            
    //                     // Get distinct values for the entity
    //                     List<Object> entityValues = properties.getDistinctValuesForVariable(entityKey);
    //                     if (entityValues != null && !entityValues.isEmpty()) {
    //                         // Convert each entity value to a Map<String, Object>
    //                         for (Object value : entityValues) {
    //                             Map<String, Object> entityMap = new HashMap<>();
    //                             entityMap.put("entity", value); // You can adapt the key here based on your requirements
    //                             entityMaps.add(entityMap);
    //                         }
    //                     } else {
    //                         throw new IllegalArgumentException("No values found for entity " + entityKey + " in Partitioning decision " + decisionName);
    //                     }
    //                 }
    //             }
            
    //             partition.setEntities(entityMaps); // Set the entities as List<Map<String, Object>>
    //             partition.initializeDecisionVariables();
    //             d = partition;
    //         }
    //         // 5) OTHERS or UNIMPLEMENTED
    //         else {
    //             throw new UnsupportedOperationException("Decision type " + type + " not implemented yet.");
    //         }
    
    //         // // keep track of references to parents if needed inside each decision
    //         // if (decObj.has("parents")) {
    //         //     JSONArray pars = decObj.optJSONArray("parents");
    //         //     if (pars != null) {
    //         //         for (int i = 0; i < pars.length(); i++) {
    //         //             String pName = pars.getString(i);
    //         //             if (decisions.containsKey(pName)) {
    //         //                 d.addParentDecision(decisions.get(pName));  // e.g., new method in your abstract Decision
    //         //             }
    //         //         }
    //         //     }
    //         // }
    
    //         // Put the newly created decision into the map
    //         this.decisions.put(decisionName, d);
    //     }
    
    //     // Store them in topological order for easy iteration
    //     for (String dn : topoOrder) {
    //         topoOrderedDecisions.add(decisions.get(dn));
    //     }
    // }

    private void buildGraphFromTSE() {
        // Extract the top-level TSE request object and the 'designSpace' / 'decisionVariables' subobjects
        JSONObject tseRequest = properties.getTsrObject();
        JSONObject designSpace = tseRequest.getJSONObject("designSpace");
        JSONObject decisionVariablesObject = designSpace.getJSONObject("decisionVariables");
    
        // We'll store adjacency info, in-degrees, plus JSON definitions
        Map<String, List<String>> adjacency = new HashMap<>();
        Map<String, Integer> inDegree = new HashMap<>();
        Map<String, JSONObject> decisionJSONMap = new HashMap<>();
    
        // Collect all decisions and their JSON definitions
        for (String decisionName : decisionVariablesObject.keySet()) {
            JSONObject decObj = decisionVariablesObject.getJSONObject(decisionName);
            decisionJSONMap.put(decisionName, decObj);
            inDegree.put(decisionName, 0); // initialize in-degree
        }
    
        // Build graph edges based on "parents" array or field
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
                    // If 'parents' is a single string
                    parents.add(decObj.getString("parents"));
                }
            }
    
            // For each parent, add edge: parent -> decisionName
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
    
        // Kahn's algorithm for topological sort
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
            String resultType = decObj.getString("resultType");

            // 1) COMBINING
            if (type.equalsIgnoreCase("Combining")) {
                Combining comb = new Combining(properties, decisionName);
    
                // subDecisions from "combiningDecisions" field
                JSONArray subDecs = decObj.getJSONArray("combiningDecisions");
                List<String> subDecisionNames = new ArrayList<>();
                for (int i = 0; i < subDecs.length(); i++) {
                    subDecisionNames.add(subDecs.getString(i));
                }
                comb.setSubDecisions(subDecisionNames);
    
                // fetch distinct values for each sub-variable
                List<List<Object>> alternatives = new ArrayList<>();
                for (String subVar : subDecisionNames) {
                    List<Object> subVarValues = properties.getDistinctValuesForVariable(subVar);
                    if (subVarValues == null || subVarValues.isEmpty()) {
                        throw new IllegalArgumentException(
                            "No values found for sub-variable " + subVar
                            + " in combining decision " + decisionName
                        );
                    }
                    alternatives.add(subVarValues);
                }
                comb.setAlternatives(alternatives);
    
                // Optional: get parents if "parents" is defined, store them for reference
                if (decObj.has("parents")) {
                    // parse array or string, store it if needed
                    // comb.setParentNames(...);
                }
    
                comb.initializeDecisionVariables();
                d = comb;
            }
    
            // 2) ASSIGNING
            else if (type.equalsIgnoreCase("Assigning")) {
                JSONArray parentsArray = decObj.optJSONArray("parents");
                List<String> parentNames = new ArrayList<>();
                String lSource = decObj.getString("L");
                String rSource = decObj.getString("R");

                if (parentsArray != null) {
                    for (int i = 0; i < parentsArray.length(); i++) {
                        parentNames.add(parentsArray.getString(i));
                    }
                }
    
                // For simplicity assume single parent
                Decision parentDecision = null;
                for (String pname : parentNames) {
                    if (decisions.containsKey(pname)) {
                        parentDecision = decisions.get(pname);
                        break;
                    }
                }
                if (parentDecision == null && !parentNames.isEmpty()) {
                    throw new IllegalArgumentException(
                        "Parent decision(s) " + parentNames
                        + " not found for assigning decision " + decisionName
                    );
                }
    
                // We'll create the Lset from parent's enumerations
                List<Map<String, Object>> parentArchs = new ArrayList<>();
                if (parentDecision != null) {
                    for (Object archObj : parentDecision.enumerateArchitectures()) {
                        parentArchs.add((Map<String, Object>) archObj);
                    }
                } else {
                    // No parent
                    throw new IllegalArgumentException("Assigning decision " + decisionName
                        + " requires a parent to form Lset.");
                }
                List<Object> Lset = new ArrayList<>();;
                List<Object> Rset = new ArrayList<>();;
                
                //List<Object> Lset = new ArrayList<>(parentArchs);
                if(!this.decisions.keySet().contains(lSource)){
                    Lset = properties.getDistinctValuesForVariable(lSource);

                }
                if(!this.decisions.keySet().contains(rSource)){
                    Rset = properties.getDistinctValuesForVariable(rSource);

                }
                
                Assigning assign = new Assigning(properties, decisionName);
                assign.setLSource(lSource);
                assign.setRSource(rSource);
                assign.setLset(Lset);
                assign.setRset(Rset);
                // Store the parent's reference if needed, e.g.:
                assign.addParentDecision(parentDecision);
    
                assign.initializeDecisionVariables();
                d = assign;
            }
    
            // 3) DOWNSELECTING
            else if (type.equalsIgnoreCase("DownSelecting")) {
                // Typically, downselecting has no direct parent or zero parents, 
                // but it's possible it has a parent we want to reference
                JSONArray parentsArray = decObj.optJSONArray("parents");
                String entitiesSource = decObj.getString("E");
                DownSelecting downSelect = new DownSelecting(properties, decisionName);
                downSelect.setEntitiesSource(entitiesSource);
    
                if (parentsArray != null && parentsArray.length() > 0) {
                    // If it has a parent, we can find that parent decision's enumerations
                    // then unify them into a single set E to possibly downselect from
                    List<String> parentNames = new ArrayList<>();
                    for (int i = 0; i < parentsArray.length(); i++) {
                        parentNames.add(parentsArray.getString(i));
                    }
    
                    Decision parent = null;
                    for (String pname : parentNames) {
                        if (decisions.containsKey(pname)) {
                            parent = decisions.get(pname);
                            break;
                        }
                    }
                    if (parent == null && !parentNames.isEmpty()) {
                        throw new IllegalArgumentException("DownSelecting decision " + decisionName
                            + " references unknown parent(s): " + parentNames);
                    }
    
                    List<Object> combinedEntityList = new ArrayList<>();
                    if (parent != null) {
                        for (Object archObj : parent.enumerateArchitectures()) {
                            // e.g. each archObj is a Map<String,Object>
                            combinedEntityList.add(archObj);
                        }
                    }
    
                    if (combinedEntityList.isEmpty()) {
                        throw new IllegalArgumentException("Parent for downselecting " + decisionName
                            + " yielded no enumerations.");
                    }
    
                    downSelect.setEntities(combinedEntityList);
                } else {
                    // If there's no parent, we get the set from distinct values on the decisionName
                    List<Object> entitySet = properties.getDistinctValuesForVariable(entitiesSource);
                    if (entitySet == null || entitySet.isEmpty()) {
                        throw new IllegalArgumentException("No distinct values found for DownSelecting decision " + decisionName);
                    }
                    downSelect.setEntities(entitySet);
                }
    
                downSelect.initializeDecisionVariables();
                d = downSelect;
            }
            
    
            // 4) PARTITIONING
            else if (type.equalsIgnoreCase("Partitioning")) {
                Partitioning partition = new Partitioning(properties, decisionName);
            
                // Extract parents for this decision
                JSONArray parentsArrayPA = decObj.optJSONArray("parents");
                List<String> parentNames = new ArrayList<>();
                if (parentsArrayPA != null) {
                    for (int i = 0; i < parentsArrayPA.length(); i++) {
                        parentNames.add(parentsArrayPA.getString(i));
                    }
                }
            
                // Assume single parent for simplicity
                Decision parentDecisionPA = null;
                for (String pname : parentNames) {
                    if (decisions.containsKey(pname)) {
                        parentDecisionPA = decisions.get(pname);
                        break;
                    }
                }
            
                if (parentDecisionPA == null && !parentNames.isEmpty()) {
                    throw new IllegalArgumentException("Parent decision(s) " + parentNames + " not found for partitioning decision " + decisionName);
                }
            
                // Set the parent decision
                partition.addParentDecision(parentDecisionPA);
                List<Object> entityValues = new ArrayList<>();
                // Initialize entities (E) from the TSERequest's "entities" field
                List<Map<String, Object>> entityMaps = new ArrayList<>();
                if (decObj.has("E")) {
                    String entityKey = decObj.getString("E");
                    //JSONArray entitiesArray = decObj.getJSONArray("E");
                    if(this.decisions.keySet().contains(entityKey)){
                        partition.setEntitiesSource(entityKey);
                    }
                    else{
                        // Get distinct values for the entity
                        entityValues = properties.getDistinctValuesForVariable(entityKey);
                        if (entityValues != null && !entityValues.isEmpty()) {
                            // Convert each entity value to a Map<String, Object>
                            for (Object value : entityValues) {
                                Map<String, Object> entityMap = new HashMap<>();
                                entityMap.put("entity", value); // You can adapt the key here based on your requirements
                                entityMaps.add(entityMap);
                            }
                        } else {
                            throw new IllegalArgumentException("No values found for entity " + entityKey + " in Partitioning decision " + decisionName);
                        }

                    }
            
 
                    
                }
            
                partition.setEntities(entityValues); // Set the entities as List<Map<String, Object>>
                partition.initializeDecisionVariables();
                d = partition;
            }
            // 5) OTHERS or UNIMPLEMENTED
            else {
                throw new UnsupportedOperationException("Decision type " + type + " not implemented yet.");
            }
    
            // // keep track of references to parents if needed inside each decision
            // if (decObj.has("parents")) {
            //     JSONArray pars = decObj.optJSONArray("parents");
            //     if (pars != null) {
            //         for (int i = 0; i < pars.length(); i++) {
            //             String pName = pars.getString(i);
            //             if (decisions.containsKey(pName)) {
            //                 d.addParentDecision(decisions.get(pName));  // e.g., new method in your abstract Decision
            //             }
            //         }
            //     }
            // }
    
            // Put the newly created decision into the map
            d.setResultType(resultType);
            this.decisions.put(decisionName, d);
        }
        
        // Store them in topological order for easy iteration
        for (String dn : topoOrder) {
            topoOrderedDecisions.add(decisions.get(dn));
        }
    }

            /**
         * Returns a set of leaf decisions (decisions that are not parents of any other decision).
         */
        public Set<String> getLeafDecisions() {
            Set<String> allDecisions = new HashSet<>(decisions.keySet());
            Set<String> parentDecisions = new HashSet<>();

            // Identify all decisions that are parents
            for (Decision d : decisions.values()) {
                List<Decision> parents = d.getParentDecisions();


                if (parents != null) {
                    for(Decision de : parents){
                        parentDecisions.add(de.getDecisionName());
                    }
                }
            }

            // Leaf decisions = all decisions - parent decisions
            allDecisions.removeAll(parentDecisions);

            return allDecisions;
        }


    public void setInputs(Decision d) {
        // Example: we either fetch from a parent decision's 'getResult()' 
        // or from a direct call to problemProperties.
    
        if (d instanceof DownSelecting) {
            DownSelecting downD = (DownSelecting) d;
            String eSource = downD.getEntitiesSource();
    
            // If eSource refers to another decision's name, fetch that decision's results:
            if (this.decisions.containsKey(eSource)) {
                Decision sourceDecision = this.decisions.get(eSource);
                List<Object> results = sourceDecision.getResult();
                downD.setEntities(results);
            } else {
                // Otherwise, retrieve from TSERequest
                List<Object> directVals = properties.getDistinctValuesForVariable(eSource);
                downD.setEntities(directVals);
            }
    
        } else if (d instanceof Assigning) {
            Assigning assignD = (Assigning) d;
            String lSource = assignD.getLSource();
            String rSource = assignD.getRSource();
    
            // Resolve L set
            if (this.decisions.containsKey(lSource)) {
                List<Object> resultsL = this.decisions.get(lSource).getResult();
                assignD.setLset(resultsL);
            } else {
                List<Object> directValsL = properties.getDistinctValuesForVariable(lSource);
                assignD.setLset(directValsL);
            }
    
            // Resolve R set
            if (this.decisions.containsKey(rSource)) {
                List<Object> resultsR = this.decisions.get(rSource).getResult();
                assignD.setRset(resultsR);
            } else {
                List<Object> directValsR = properties.getDistinctValuesForVariable(rSource);
                assignD.setRset(directValsR);
            }
    
        } else if (d instanceof Partitioning) {
            Partitioning partD = (Partitioning) d;
            String eSource = partD.getEntitiesSource();
    
            if (this.decisions.containsKey(eSource)) {
                // eSource is output from a parent decision
                Decision sourceDec = this.decisions.get(eSource);
                List<Object> parentResult = sourceDec.getResult();
                partD.setEntities(parentResult);

            } else {
                // eSource is a direct variable from TSERequest
                List<Object> directVals = properties.getDistinctValuesForVariable(eSource);
                partD.setEntities(directVals);
            }
    
        } else if (d instanceof Combining) {
            // If you need to update Combining's sub-decisions from other decisions, do similarly:
            Combining comb = (Combining) d;
            // Possibly fetch something like comb.getSubDecisions() and see if they reference other decisions, etc.
            // If it’s purely direct from TSERequest, you might not need to do anything here.
        }
        // else if (d instanceof Permuting) { ... }
        // else if (d instanceof Connecting) { ... }
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
