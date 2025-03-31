package tatc.decisions;

import tatc.decisions.adg.AdgSolution;
import tatc.decisions.adg.Graph;
import tatc.tradespaceiterator.ProblemProperties;

import org.json.JSONArray;
import org.json.JSONObject;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

import java.util.*;

/**
 * Partitioning pattern decision:
 * 
 * For n entities in E, a partition is represented by an integer array A = [a1, a2, ..., an]
 * where a_i = j means entity i is in subset j.
 * 
 * Constraints: 
 * - a1 = 1
 * - a_k ∈ [1, max_label_so_far + 1] for each k
 * This ensures a canonical labeling of subsets.
 */
public class Partitioning extends Decision {

    /**
     * The set of entities to partition
     */
    private List<Object> E;
    private Random rand = new Random();
    private String entitiesSource;

    public Partitioning(ProblemProperties properties, String decisionName) {
        super(properties, decisionName);
        this.E = new ArrayList<>();
        this.parentDecisions = new ArrayList<Decision>();

    }
    public void addParentDecision(Decision parent) {
        parentDecisions.add(parent);
    }
    

    /**
     * Sets the set E of entities to be partitioned.
     */
    public void setEntities(List<Object> entities) {
        this.E = entities;
    }
    public void setEntitiesSource(String entitiesSource) {
        this.entitiesSource = entitiesSource;
    }
    
    public String getEntitiesSource() {
        return this.entitiesSource;
    }



    @Override
    public void initializeDecisionVariables() {
        // if (E.isEmpty()) {
        //     throw new IllegalStateException("Partitioning decision " + decisionName + " has no entities defined.");
        // }
    }

    @Override
    public Iterable<Map<String, Object>> enumerateArchitectures() {
        // Enumerating all partitions of a set is huge (Bell number complexity).
        // Usually not done. Return empty.
        return Collections.emptyList();
    }

    @Override
    public Object encodeArchitecture(Map<String, Object> architecture) {
        // If the architecture provides a partition under decisionName,
        // it might be a list of subsets: a List<List<Object>> representing subsets.
        @SuppressWarnings("unchecked")
        List<List<Object>> subsets = (List<List<Object>>) architecture.get(decisionName);
        if (subsets == null) {
            // If no partition given, default to each entity in its own subset.
            int[] encoding = new int[E.size()];
            for (int i = 0; i < E.size(); i++) {
                encoding[i] = i + 1; // each entity in unique subset
            }
            repair(encoding);
            return encoding;
        }

        int[] encoding = new int[E.size()];
        Arrays.fill(encoding, -1);

        // Assign subsets based on the order subsets appear (to ensure canonical labeling)
        // We'll label subsets in the order they appear: first subset is label 1, next is 2, etc.
        int label = 1;
        for (List<Object> subset : subsets) {
            for (Object entity : subset) {
                int idx = E.indexOf(entity);
                if (idx == -1) {
                    throw new IllegalArgumentException("Entity not found in E: " + entity);
                }
                if (encoding[idx] != -1) {
                    throw new IllegalArgumentException("Entity assigned more than once: " + entity);
                }
                encoding[idx] = label;
            }
            label++;
        }

        // If any entity wasn't assigned, assign it to last subset:
        for (int i = 0; i < encoding.length; i++) {
            if (encoding[i] == -1) {
                encoding[i] = label++; // create a new subset for it
            }
        }

        repair(encoding);
        this.lastEncoding = encoding;
        return encoding;
    }
    /**
 * Repair the child encoding if this partitioning depends on a 
 * DownSelecting node that has selected fewer items.
 */
public Object repairWithDependency(Object partitionEnc, Object downselectEnc) {
    int[] partitionChrom = (int[]) partitionEnc;
    int[] downselChrom = (int[]) downselectEnc;

    // Reconstruct the new partition array
    int selectedCount = 0;
    for (int bit : downselChrom) {
        if (bit == 1) selectedCount++;
    }

    // create a brand new array and fill from the old partitionChrom
    int[] newPartition = new int[selectedCount];
    // we must track reading from partitionChrom[x] in some consistent way

    int readPos = 0;
    int newIndex = 0;
    int maxSoFar = 0;
    for (int i=0; i<selectedCount; i++){
        if (readPos < partitionChrom.length) {
            // We can copy the old partition label
            newPartition[newIndex++] = partitionChrom[readPos++];
        } else {
            // Mismatch: we've run out of old partition entries
            // Insert a random label or fallback label
            // e.g., random from [1..maxLabels]
            maxSoFar = maxLabelSoFar(newPartition, i);
            int randomLabel = 1 + rand.nextInt(maxSoFar);
            newPartition[newIndex++] = randomLabel;
        }
    }

    // now newPartition is your final 
    repair(newPartition);
    return newPartition;
}


/** Re-label group IDs consecutively. e.g. 
 *  If you find used labels {1,2,4} => you map {1->1,2->2,4->3}. 
 */
private void relabel(int[] partition) {
    Set<Integer> used = new TreeSet<>();
    for (int label : partition) {
        used.add(label);
    }
    // e.g. used might be [1,2,4]
    Map<Integer,Integer> labelMap = new HashMap<>();
    int next = 1;
    for (Integer oldL : used) {
        labelMap.put(oldL, next++);
    }
    // apply
    for (int i=0; i< partition.length; i++) {
        partition[i] = labelMap.get(partition[i]);
    }
}

    public List<Decision> getParents(){
        return parentDecisions;
    }

    @Override
    @SuppressWarnings("unchecked")
public List<Map<String, Object>> decodeArchitecture(Object encoded, Solution sol, Graph graph) {
    // Extract the decision-specific variables from TSERequest
    JSONObject designSpace = this.properties.getTsrObject()
            .getJSONObject("designSpace");
    JSONObject spaceSegment = designSpace.getJSONArray("spaceSegment")
            .getJSONObject(0);

    List<Map<String, Object>> architectureParams = new ArrayList<>();
    List<Object> selectedEntities = this.E;

    // Validate encoding length
    int[] chrom = (int[]) encoded;
    if (chrom.length != selectedEntities.size()) {
        throw new IllegalArgumentException("Encoded length does not match the size of selected entities.");
    }

    // Determine the number of subsets (max label in chrom)
    int maxLabel = Arrays.stream(chrom).max().orElse(0);

    // Create subsets based on encoding
    List<List<Object>> subsets = new ArrayList<>(Collections.nCopies(maxLabel, null));
    for (int i = 0; i < maxLabel; i++) {
        subsets.set(i, new ArrayList<>());
    }

    for (int i = 0; i < chrom.length; i++) {
        int label = chrom[i];
        if (label < 1 || label > maxLabel) {
            throw new IllegalArgumentException("Invalid label in encoding: " + label);
        }
        subsets.get(label - 1).add(selectedEntities.get(i)); // Store the entities in subsets
    }

    // Process each subset and construct the architecture
    for (int k = 0; k < subsets.size(); k++) {
        List<Object> subset = subsets.get(k);

        // Initialize a new decision map for this subset
        Map<String, Object> newDecisionMap = new HashMap<>();

        // Fetch the fixed elements from TSERequest
        JSONArray fixedSatellites = spaceSegment.getJSONArray("satellites");

        // Add the subset to the map under its decision name
        // Combine fixed and variable parts
        if ("satellites".equals(resultType)) {
            List<JSONObject> satelliteConfigs = new ArrayList<>();
            for (Object entity : subset) {
                // Clone the fixed satellite config and attach the payload
                JSONObject satellite = new JSONObject(fixedSatellites.getJSONObject(0).toString());
                satellite.put("payload", entity);
                satelliteConfigs.add(satellite);
            }
            newDecisionMap.put("satellites", satelliteConfigs);
        }
        // Add the constructed map to the architecture parameters
        architectureParams.add(newDecisionMap);
    }
    return architectureParams;
}


    @Override
    public int[] getLastEncoding() {
        return lastEncoding;
    }

    @Override
    public void mutate(Object encoded) {
        int[] chrom = (int[]) encoded;
        double mutationProbability = this.properties.getTradespaceSearch().getSettings().getSearchParameters().getpMutation(); // example value

        for (int i = 1; i < chrom.length; i++) {
            if (rand.nextDouble() < mutationProbability) {
                // Mutate a_k: must be in [1, max_so_far+1]
                int maxSoFar = maxLabelSoFar(chrom, i);
                // Choose a new label in [1, maxSoFar+1]
                int newLabel = 1 + rand.nextInt(maxSoFar + 1);
                chrom[i] = newLabel;
            }
        }

        repair(chrom);
    }

    @Override
    public Object crossover(Object parent1, Object parent2) {
        int[] p1 = (int[]) parent1;
        int[] p2 = (int[]) parent2;
        int n1 = p1.length;
        int n2 = p2.length;
        int nShort = Math.min(n1, n2);
        int nLong  = Math.max(n1, n2);
    
        // Child has length = longer of the two parent lengths
        int[] child = new int[nLong];
    
        // ----------------------------
        // 1) Cycle crossover in the overlapping region [0..nShort-1]
        // ----------------------------
        boolean[] visited = new boolean[nShort];
        Arrays.fill(visited, false);
        int cycle = 0;
    
        for (int i = 0; i < nShort; i++) {
            if (!visited[i]) {
                // Start a new cycle at index i
                int index = i;
                do {
                    visited[index] = true;
                    // Even cycles => copy from p1, Odd cycles => copy from p2
                    child[index] = (cycle % 2 == 0) ? p1[index] : p2[index];
    
                    // Find next index in the cycle
                    int nextIndex = -1;
                    for (int j = 0; j < nShort; j++) {
                        if (!visited[j] && p1[j] == p2[index]) {
                            nextIndex = j;
                            break;
                        }
                    }
                    if (nextIndex == -1) {
                        break;
                    }
                    index = nextIndex;
                } while (index != i);
                cycle++;
            }
        }
    
        // ----------------------------
        // 2) Copy leftover region
        //    Indices >= nShort
        // ----------------------------
        // If p1 is longer, copy leftover from p1; 
        // if p2 is longer, copy leftover from p2
        // if both are the same length => no leftover
        if (n1 > nShort) {
            // leftover portion in p1 => [nShort..(n1-1)]
            // copy them to child
            for (int i = nShort; i < n1; i++) {
                child[i] = p1[i];
            }
        }
        if (n2 > nShort) {
            // leftover portion in p2 => [nShort..(n2-1)]
            // If both parents have leftover, you can pick from p1 or p2
            // or do random. Here, we assume p2 overwrites p1 if both have leftover:
            for (int i = nShort; i < n2; i++) {
                child[i] = p2[i];
            }
        }
    
        // ----------------------------
        // 3) For any unvisited indices < nShort, if they were not assigned,
        //    default them to p1[i]. (Should be assigned by cycle, but just in case)
        // ----------------------------
        for (int i = 0; i < nShort; i++) {
            if (!visited[i]) {
                child[i] = p1[i];
            }
        }
    
        // ----------------------------
        // 4) Repair the child's encoding to enforce ascending labeling constraints
        // ----------------------------
        repair(child);
    
        return child;
    }
    
    

    @Override
    public int getNumberOfVariables() {
        if((this.parentDecisions.get(0) instanceof DownSelecting)){

            if (parentDecisions.get(0) == null) {
                throw new IllegalStateException("Parent decision is not set for DownSelecting decision " + decisionName);
            }
        
            // Get the parent's last encoding or selected entities
            int[] parentSelectedEntities = ((DownSelecting)parentDecisions.get(0)).getLastEncoding(); // This method should return the selected entities from the parent's last encoding
            if (parentSelectedEntities == null) {
                return E.size();
            }
            int counter = 0;
            for(int i : parentSelectedEntities){
                counter += i;
            }
            return counter;
        }else{
            return E.size();
        }
        // Return the size of the selected entities
    }
    

    @Override
    public Object randomEncoding() {
        int n = E.size();
        if (n == 0) {
            // Return an empty list of subsets
            return new ArrayList<Object>();
        }

        // 1) Generate integer encoding (labels)
        int[] encoding = new int[n];
        encoding[0] = 1;
        int maxLabel = 1;
        for (int i = 1; i < n; i++) {
            int newLabel = 1 + rand.nextInt(maxLabel + 1);
            encoding[i] = newLabel;
            if (newLabel > maxLabel) {
                maxLabel = newLabel;
            }
        }
        // Optional repair
        repair(encoding);

        // 2) Build subsets from labels
        //   a) create list-of-lists
        List<List<Object>> subsets = new ArrayList<>();
        for (int label = 1; label <= maxLabel; label++) {
            subsets.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            int label = encoding[i];
            subsets.get(label - 1).add(E.get(i));
        }

        // 3) Convert List<List<Object>> to List<Object>
        //    i.e. each subset is an element in a List<Object>.
        List<Object> finalResult = new ArrayList<>();
        for (List<Object> subset : subsets) {
            finalResult.add(subset);
        }

        // Keep track of your integer encoding if needed
        this.lastEncoding = encoding;
        this.result = finalResult;
        // Return as List<Object> so that it matches code expecting `List<Object>`
        return encoding;
    }

    /**
     * Applies an integer array where each element is a subset label (e.g., 1,2,3, ...).
     * The i-th label indicates which subset E.get(i) belongs to.
     * It stores a List<Object> of subsets in this.result,
     * where each subset is itself a List<Object> of items sharing the same label.
     */
    @Override
    public void applyEncoding(int[] encoding) {
        // Handle case where E is null or empty
        if (E == null || E.isEmpty()) {
            this.result = new ArrayList<>();
            return;
        }
    
        // Ensure we don't go out of bounds
        int n = Math.min(encoding.length, E.size());
    
        // Determine the max label in the portion we actually use
        int maxLabel = 0;
        for (int i = 0; i < n; i++) {
            if (encoding[i] > maxLabel) {
                maxLabel = encoding[i];
            }
        }
    
        // Create the subset lists
        List<List<Object>> subsets = new ArrayList<>();
        for (int label = 1; label <= maxLabel; label++) {
            subsets.add(new ArrayList<>());
        }
    
        // Assign each entity E[i] to the subset indicated by encoding[i]
        for (int i = 0; i < n; i++) {
            int label = encoding[i];
            if (label > 0 && label <= maxLabel) {
                subsets.get(label - 1).add(E.get(i));
            }
        }
    
        // Handle cases where encoding.length > E.size()
        // (meaning the encoding array has extra labels beyond E's size)
        if (encoding.length > E.size()) {
            for (int i = E.size(); i < encoding.length; i++) {
                int label = encoding[i];
                if (label > 0 && label <= maxLabel) {
                    // Store "placeholder" items for extra labels
                    subsets.get(label - 1).add("Placeholder_Item_" + i);
                }
            }
        }
    
        // Convert subsets to a List<Object>
        List<Object> finalResult = new ArrayList<>(subsets);
    
        // Store in this.result
        this.result = finalResult;
    }
    




    


    @Override
    public int getMaxOptionForVariable(int i) {
        // In theory, label could go up to i+1 if we always create a new subset.
        // But let's say max subsets = n, so maxOption = i+1 for position i.
        // We'll return i+1 to ensure no out-of-bound; 
        // the repair step will ensure canonical form.
        return i + 2; // since labeling starts at 1, max = i+1, so upperBound = i+1 -> we say i+2 because upper bound is exclusive?
    }

    @Override
    public Object extractEncodingFromSolution(Solution solution, int offset) {
        return this.getEncodingById(((AdgSolution)solution).getId()); // This method should return the selected entities from the parent's last encoding


    }

    /**
     * Repairs the encoding to ensure canonical labeling:
     * a1 = 1
     * For each a_k, a_k ∈ [1, 1 + max_label_so_far].
     * After ensuring these constraints, we also make sure no gaps in labeling.
     */
    private void repair(int[] chrom) {
        if (chrom.length == 0) return;
        
        // Enforce the first gene is always 1.
        chrom[0] = 1;
        int maxLabelSoFar = 1;
        
        // Ensure each subsequent gene is within [1, maxLabelSoFar+1]
        for (int k = 1; k < chrom.length; k++) {
            int val = chrom[k];
            if (val < 1) {
                val = 1;
            }
            if (val > maxLabelSoFar + 1) {
                val = maxLabelSoFar + 1;
            }
            chrom[k] = val;
            if (val > maxLabelSoFar) {
                maxLabelSoFar = val;
            }
        }
        
        // Remove any gaps by reassigning labels to be consecutive.
        // For example, if chrom = [1, 3, 3, 1], the distinct labels are {1, 3},
        // and we remap them to {1, 2}.
        Set<Integer> usedLabels = new TreeSet<>();
        for (int v : chrom) {
            usedLabels.add(v);
        }
        List<Integer> sortedLabels = new ArrayList<>(usedLabels);
        Map<Integer, Integer> labelMap = new HashMap<>();
        for (int i = 0; i < sortedLabels.size(); i++) {
            labelMap.put(sortedLabels.get(i), i + 1);
        }
        for (int i = 0; i < chrom.length; i++) {
            chrom[i] = labelMap.get(chrom[i]);
        }
    }
    

    private int maxLabelSoFar(int[] chrom, int k) {
        int maxLabel = 1;
        for (int i = 0; i < k; i++) {
            if (chrom[i] > maxLabel) {
                maxLabel = chrom[i];
            }
        }
        return maxLabel;
    }

}
