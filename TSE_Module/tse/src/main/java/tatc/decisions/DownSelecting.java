package tatc.decisions;

import tatc.tradespaceiterator.ProblemProperties;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

import java.util.*;

/**
 * DownSelecting pattern decision:
 * 
 * In the down-selecting pattern, each alternative is a subset of a set of entities E.
 * We represent an architecture fragment by a binary array of length m, 
 * where m = |E| and Mi = 1 means entity i is selected, 0 means not selected.
 */
public class DownSelecting extends Decision {

    /**
     * The set of all entities we can choose from.
     */
    private List<Object> E;
    private int[] lastEncoding;
    private Random rand = new Random();

    public DownSelecting(ProblemProperties properties, String decisionName) {
        super(properties, decisionName);
        this.E = new ArrayList<>();
        this.parentDecisions = new ArrayList<Decision>();

    }

    /**
     * Sets the set E of entities from which we will down-select.
     */
    public void setEntities(List<Object> entities) {
        this.E = new ArrayList<>(entities);
    }

    @Override
    public void initializeDecisionVariables() {
        // E should be set before this is called.
        // Just ensure E is not empty.
        if (E.isEmpty()) {
            throw new IllegalStateException("DownSelecting decision " + decisionName + " has no entities (E) defined.");
        }
    }

    @Override
    public Iterable<Map<String, Object>> enumerateArchitectures() {
        // Enumerate all subsets of E - might be huge if E is large.
        // Usually not desired, but we can do it if needed.
        // If E is large, this is impractical. 
        // For simplicity, return a small example or empty.
        
        // To keep consistent, let's just return an empty list 
        // or a minimal set. Typically, full enumeration isn't used for down-selecting 
        // due to combinational explosion.
        
        return Collections.emptyList();
    }

    @Override
    public Object encodeArchitecture(Map<String, Object> architecture) {
        // Encoding a given architecture:
        // If architecture doesn't specify which are selected, 
        // we must guess or rely on some field. 
        // For simplicity, suppose architecture has a key = decisionName that 
        // is a list of selected entities from E.
        
        @SuppressWarnings("unchecked")
        List<Object> selected = (List<Object>) architecture.get(decisionName);
        if (selected == null) {
            // If no selection info, just encode all 0
            int[] encoding = new int[E.size()];
            Arrays.fill(encoding, 0);
            return encoding;
        }

        int[] encoding = new int[E.size()];
        Arrays.fill(encoding, 0);
        for (Object sel : selected) {
            int idx = E.indexOf(sel);
            if (idx != -1) {
                encoding[idx] = 1;
            }
        }
        this.lastEncoding = encoding;

        return encoding;
    }

    @Override
    public List<Map<String, Object>> decodeArchitecture(Object encoded, Solution sol) {
        List<Map<String,Object>> currentArchitectures = new ArrayList<Map<String,Object>>();
        int[] chrom = (int[]) encoded;
        if (chrom.length != E.size()) {
            throw new IllegalArgumentException("Encoded length does not match |E|.");
        }

        // Determine selected entities
        List<Object> selectedEntities = new ArrayList<>();
        for (int i = 0; i < chrom.length; i++) {
            if (chrom[i] == 1) {
                selectedEntities.add(E.get(i));
            }
        }

        // Add the selected subset to each architecture map
        // For each architecture in currentArchitectures, add (decisionName -> selectedEntities)
        for (Map<String, Object> arch : currentArchitectures) {
            arch.put(decisionName, selectedEntities);
        }

        return currentArchitectures;
    }

    // @Override
    // public void mutate(Object encoded) {
    //     int[] chrom = (int[]) encoded;
    //     double mutationProbability = 0.05; // Example value

    //     for (int i = 0; i < chrom.length; i++) {
    //         if (rand.nextDouble() < mutationProbability) {
    //             // Flip bit
    //             chrom[i] = (chrom[i] == 0) ? 1 : 0;
    //         }
    //     }
    // }

    // @Override
    // public Object crossover(Object parent1, Object parent2) {
    //     int[] p1 = (int[]) parent1;
    //     int[] p2 = (int[]) parent2;
    //     if (p1.length != p2.length) {
    //         throw new IllegalArgumentException("Parents differ in length. Cannot crossover.");
    //     }

    //     int[] child = new int[p1.length];
    //     for (int i = 0; i < p1.length; i++) {
    //         // Uniform crossover: 50% chance from p1 or p2
    //         child[i] = rand.nextBoolean() ? p1[i] : p2[i];
    //     }
    //     return child;
    // }

    @Override
public Object crossover(Object parent1, Object parent2) {
    int[] p1 = (int[]) parent1;
    int[] p2 = (int[]) parent2;
    int[] child = new int[p1.length];

    Random rand = new Random();
    for (int i = 0; i < p1.length; i++) {
        // Uniform crossover
        child[i] = rand.nextBoolean() ? p1[i] : p2[i];
    }
    return child;
}
public Object repairWithDependency(Object partitionEnc, Object downselectEnc){
    return partitionEnc;
}

@Override
public void mutate(Object encoded) {
    int[] encoding = (int[]) encoded;

    // Example: Flip mutation
    Random rand = new Random();
    int idx = rand.nextInt(encoding.length);
    encoding[idx] = 1 - encoding[idx]; // Flip 0 to 1 or 1 to 0
}


    @Override
    public int getNumberOfVariables() {
        return E.size();
    }

    @Override
    public Object randomEncoding() {
        int[] encoding = new int[E.size()];
        // Randomly assign 0 or 1
        for (int i = 0; i < encoding.length; i++) {
            encoding[i] = rand.nextBoolean() ? 1 : 0;
        }
        // If needed, ensure at least one selected:
        // If no selected, force one randomly:
        if (Arrays.stream(encoding).sum() == 0 && encoding.length > 0) {
            encoding[rand.nextInt(encoding.length)] = 1;
        }
        this.lastEncoding = encoding;
        return encoding;
    }
    @Override
    public int[] getLastEncoding() {
        return lastEncoding;
    }
    @Override
    public int getMaxOptionForVariable(int i) {
        // Binary: each variable can be 0 or 1, so maxOption=2
        return 2;
    }
    public void addParentDecision(Decision parent) {
        parentDecisions.add(parent);
    }
    

    @Override
    public Object extractEncodingFromSolution(Solution solution, int offset) {
        int length = getNumberOfVariables();
        int[] encoding = new int[length];

        for (int i = 0; i < length; i++) {
            double val = ((RealVariable)solution.getVariable(offset + i)).getValue();
            int bit = (int)Math.round(val);
            if (bit < 0 || bit > 1) {
                throw new IllegalArgumentException("DownSelecting decision variable out of binary range: " + bit);
            }
            encoding[i] = bit;
        }

        return encoding;
    }
}
