package tatc.decisions;

import tatc.tradespaceiterator.ProblemProperties;

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

    public Partitioning(ProblemProperties properties, String decisionName) {
        super(properties, decisionName);
        this.E = new ArrayList<>();
    }

    /**
     * Sets the set E of entities to be partitioned.
     */
    public void setEntities(List<Object> entities) {
        this.E = new ArrayList<>(entities);
    }

    @Override
    public void initializeDecisionVariables() {
        if (E.isEmpty()) {
            throw new IllegalStateException("Partitioning decision " + decisionName + " has no entities defined.");
        }
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
        return encoding;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> decodeArchitecture(Object encoded, List<Map<String, Object>> currentArchitectures) {
        int[] chrom = (int[]) encoded;
        if (chrom.length != E.size()) {
            throw new IllegalArgumentException("Encoded length does not match |E|.");
        }

        // Identify how many subsets we have:
        int maxLabel = 0;
        for (int val : chrom) {
            if (val > maxLabel) {
                maxLabel = val;
            }
        }

        // Create subsets
        List<List<Object>> subsets = new ArrayList<>();
        for (int s = 1; s <= maxLabel; s++) {
            subsets.add(new ArrayList<>());
        }

        for (int i = 0; i < chrom.length; i++) {
            int label = chrom[i];
            subsets.get(label - 1).add(E.get(i));
        }

        // Add these subsets to each architecture map
        for (Map<String, Object> arch : currentArchitectures) {
            arch.put(decisionName, subsets);
        }

        return currentArchitectures;
    }

    @Override
    public void mutate(Object encoded) {
        int[] chrom = (int[]) encoded;
        double mutationProbability = 0.05; // example value

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
        if (p1.length != p2.length) {
            throw new IllegalArgumentException("Parents differ in length. Cannot crossover.");
        }

        int[] child = new int[p1.length];
        // Simple uniform crossover
        for (int i = 0; i < p1.length; i++) {
            child[i] = rand.nextBoolean() ? p1[i] : p2[i];
        }

        repair(child);
        return child;
    }

    @Override
    public int getNumberOfVariables() {
        return E.size();
    }

    @Override
    public Object randomEncoding() {
        int n = E.size();
        int[] encoding = new int[n];
        encoding[0] = 1;
        // For each subsequent a_k: in [1, maxSoFar+1]
        for (int k = 1; k < n; k++) {
            int maxSoFar = maxLabelSoFar(encoding, k);
            int newLabel = 1 + rand.nextInt(maxSoFar + 1);
            encoding[k] = newLabel;
        }

        repair(encoding);
        return encoding;
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
        int length = getNumberOfVariables();
        int[] encoding = new int[length];

        for (int i = 0; i < length; i++) {
            double val = ((RealVariable) solution.getVariable(offset + i)).getValue();
            int label = (int)Math.round(val);
            if (label < 1) {
                label = 1; // ensure at least 1
            }
            // no upper bound check here, because we'll repair anyway
            encoding[i] = label;
        }

        repair(encoding);
        return encoding;
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
