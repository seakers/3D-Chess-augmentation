package tatc.decisions;

import tatc.decisions.adg.AdgSolution;
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
public List<Map<String, Object>> decodeArchitecture(Object encoded, Solution sol) {
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
        newDecisionMap.put(decisionName, subset);

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

    // 1) Determine the child's length as the max of parent lengths
    int childLen = Math.max(p1.length, p2.length);
    int[] child = new int[childLen];

    // 2) Uniform crossover in overlapping region
    int minLen = Math.min(p1.length, p2.length);
    for (int i = 0; i < minLen; i++) {
        child[i] = rand.nextBoolean() ? p1[i] : p2[i];
    }

    // 3) Copy remaining tail from whichever parent is longer
    if (p1.length < p2.length) {
        // p2 is longer: copy leftover from p2
        for (int i = minLen; i < childLen; i++) {
            child[i] = p2[i];
        }
    } else if (p2.length < p1.length) {
        // p1 is longer: copy leftover from p1
        for (int i = minLen; i < childLen; i++) {
            child[i] = p1[i];
        }
    }
    // If p1.length == p2.length, no leftover region to copy.

    // 4) Repair child to ensure valid labeling, etc.
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
        if (encoding.length != E.size()) {
            throw new IllegalArgumentException("Encoding length mismatch in Partitioning. "
                + "Expected " + E.size() + " but got " + encoding.length);
        }

        // 1) Determine the maximum label
        int maxLabel = 0;
        for (int label : encoding) {
            if (label > maxLabel) {
                maxLabel = label;
            }
        }
        // If there's no entity or all labels are 0, it's an edge case
        // but let's proceed with "empty subsets" or handle it as needed.

        // 2) Create an array of subsets (label from 1..maxLabel)
        List<List<Object>> subsets = new ArrayList<>();
        for (int label = 1; label <= maxLabel; label++) {
            subsets.add(new ArrayList<>());
        }

        // 3) Assign each entity E[i] to the subset indicated by encoding[i]
        for (int i = 0; i < encoding.length; i++) {
            int label = encoding[i];
            if (label > 0 && label <= maxLabel) {
                subsets.get(label - 1).add(E.get(i));
            }
            // If label=0 or out of range, you could either ignore the item or
            // handle it differently, depending on your convention.
        }

        // 4) Convert List<List<Object>> to a List<Object> (where each element is one subset).
        List<Object> finalResult = new ArrayList<>();
        for (List<Object> subset : subsets) {
            finalResult.add(subset);
        }

        // 5) Store in this.result
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
        chrom[0] = 1;

        int maxLabelSoFar = 1;
        for (int k = 1; k < chrom.length; k++) {
            int val = chrom[k];
            if (val < 1) val = 1;
            if (val > maxLabelSoFar + 1) val = maxLabelSoFar + 1;
            chrom[k] = val;
            if (val > maxLabelSoFar) {
                maxLabelSoFar = val;
            }
        }

        // Remove gaps in labeling (e.g., if we have labels {1,3}, re-label 3 as 2)
        // Collect used labels
        Set<Integer> usedLabels = new TreeSet<>();
        for (int v : chrom) usedLabels.add(v);

        List<Integer> sortedLabels = new ArrayList<>(usedLabels);
        // Create a map from old label to new label (no gaps)
        Map<Integer,Integer> labelMap = new HashMap<>();
        for (int i = 0; i < sortedLabels.size(); i++) {
            labelMap.put(sortedLabels.get(i), i+1);
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
