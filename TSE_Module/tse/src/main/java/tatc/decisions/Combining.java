package tatc.decisions;

import tatc.tradespaceiterator.ProblemProperties;
import java.util.*;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

/**
 * A Combining decision pattern class. 
 * 
 * In a combining pattern, we have multiple sub-decisions (e.g., "altitude", "inclination"), 
 * each having its own set of discrete alternatives. One architecture choice is selecting 
 * exactly one alternative from each sub-decision, resulting in a tuple 
 * A = [x1, x2, ..., x_n], where x_i is the chosen alternative index for the i-th sub-decision.
 */
public class Combining extends Decision {

    /**
     * The names of the sub-decisions that make up this combining decision.
     * For example: ["altitude", "inclination"].
     */
    private List<String> subDecisions;

    /**
     * For each sub-decision, a list of possible discrete alternatives.
     * For example, if subDecisions = ["altitude", "inclination"], 
     * alternatives.get(0) might be [400, 600, 800] for altitude,
     * alternatives.get(1) might be [30, 60, 90] for inclination.
     */
    private List<List<Object>> alternatives;
    private Random rand = new Random();

    /**
     * Constructs a Combining decision node.
     * 
     * @param properties the problem properties object (not used here for loading, just stored)
     * @param decisionName the name or identifier of this decision node
     */
    public Combining(ProblemProperties properties, String decisionName) {
        super(properties, decisionName);
        this.subDecisions = new ArrayList<>();
        this.alternatives = new ArrayList<>();
        this.parentDecisions = new ArrayList<Decision>();
    }

    @Override
    public void initializeDecisionVariables() {
        // In this simplified approach, we do nothing here since 
        // sub-decisions and alternatives will be provided externally.
    }

    /**
     * Sets the sub-decisions that form this combining decision.
     * @param subDecisions a list of sub-decision names
     */
    public void setSubDecisions(List<String> subDecisions) {
        this.subDecisions = new ArrayList<>(subDecisions);
    }

    /**
     * Sets the alternatives for each sub-decision.
     * @param alternatives a list (for each sub-decision) of the sub-decision's possible values
     */
    public void setAlternatives(List<List<Object>> alternatives) {
        if (alternatives.size() != subDecisions.size()) {
            throw new IllegalArgumentException("Number of alternatives lists must match number of sub-decisions.");
        }
        this.alternatives = new ArrayList<>();
        for (List<Object> alt : alternatives) {
            this.alternatives.add(new ArrayList<>(alt));
        }
    }

    @Override
    public Iterable<Map<String, Object>> enumerateArchitectures() {
        // Enumerate all combinations (the Cartesian product).
        List<Map<String, Object>> allArchitectures = new ArrayList<>();
        cartesianEnumerate(0, new ArrayList<Integer>(), allArchitectures);
        return allArchitectures;
    }

    private void cartesianEnumerate(int index, List<Integer> current, List<Map<String,Object>> result) {
        if (index == alternatives.size()) {
            // Build an architecture from 'current'
            Map<String,Object> arch = new LinkedHashMap<>();
            for (int i=0; i<subDecisions.size(); i++) {
                Object chosenVal = alternatives.get(i).get(current.get(i));
                arch.put(subDecisions.get(i), chosenVal);
            }
            result.add(arch);
            return;
        }

        List<Object> opts = alternatives.get(index);
        for (int i=0; i<opts.size(); i++) {
            current.add(i);
            cartesianEnumerate(index+1, current, result);
            current.remove(current.size()-1);
        }
    }
    public void addParentDecision(Decision parent) {
        parentDecisions.add(parent);
    }
    

    @Override
    public Object encodeArchitecture(Map<String, Object> architecture) {
        // Encode as an integer array representing chosen indices
        int n = subDecisions.size();
        int[] encoding = new int[n];
        for (int i=0; i<n; i++) {
            String var = subDecisions.get(i);
            Object chosenVal = architecture.get(var);
            List<Object> vals = alternatives.get(i);
            int idx = vals.indexOf(chosenVal);
            if (idx < 0) {
                throw new IllegalArgumentException("Value " + chosenVal + " not found among alternatives for variable " + var);
            }
            encoding[i] = idx;
        }
        return encoding;
    }

    @Override
    public List<Map<String, Object>> decodeArchitecture(Object encoded, Solution sol) {
        List<Map<String, Object>> architectures = new ArrayList<>();
        int[] chrom = (int[]) encoded;
        if (chrom.length != subDecisions.size()) {
            throw new IllegalArgumentException("Encoded chromosome length does not match number of sub-decisions.");
        }

        Map<String, Object> arch = new LinkedHashMap<>();
        for (int i=0; i<chrom.length; i++) {
            Object chosenVal = alternatives.get(i).get(chrom[i]);
            arch.put(subDecisions.get(i), chosenVal);
        }
        architectures.add(arch);
        return architectures;
    }
    @Override
    public Object extractEncodingFromSolution(Solution solution, int offset) {
        int length = getNumberOfVariables(); // equals subDecisions.size()
        int[] encoding = new int[length];
        for (int i = 0; i < length; i++) {
            double val = ((RealVariable)solution.getVariable(offset + i)).getValue();
            encoding[i] = (int)Math.round(val);
        }
        return encoding;
    }

    @Override
    public void mutate(Object encoded) {
        // Uniform mutation: With some probability, pick one gene and set it to another random alternative
        int[] chrom = (int[]) encoded;
        double mutationProbability = this.properties.getTradespaceSearch().getSettings().getSearchParameters().getpMutation(); // example value
        for (int i=0; i<chrom.length; i++) {
            if (rand.nextDouble() < mutationProbability) {
                int dim = i;
                int newVal = rand.nextInt(alternatives.get(dim).size());
                chrom[i] = newVal;
            }
        }
    }

    @Override
    public Object crossover(Object parent1, Object parent2) {
        // Uniform crossover
        int[] p1 = (int[]) parent1;
        int[] p2 = (int[]) parent2;
        if (p1.length != p2.length) {
            throw new IllegalArgumentException("Parents differ in length. Cannot crossover.");
        }

        int[] child = new int[p1.length];
        for (int i=0; i<p1.length; i++) {
            // 50% chance from p1 or p2
            if (rand.nextBoolean()) {
                child[i] = p1[i];
            } else {
                child[i] = p2[i];
            }
        }

        return child;
    }

    @Override
    public int getNumberOfVariables() {
        // In the combining pattern, each sub-decision is represented by one variable (an index).
        // Thus, the number of variables equals the number of sub-decisions.
        return subDecisions.size();
    }
    @Override
    public Object randomEncoding() {
        int[] encoding = new int[subDecisions.size()];
        for (int i = 0; i < subDecisions.size(); i++) {
            // For each sub-decision, pick a random index from its set of alternatives
            int numAlternatives = alternatives.get(i).size();
            encoding[i] = rand.nextInt(numAlternatives-1);
        }
        return encoding;
    }
    @Override
    public int[] getLastEncoding() {
        return lastEncoding;
    }
    @Override
    public int getMaxOptionForVariable(int i) {
        if (i < 0 || i >= alternatives.size()) {
            throw new IndexOutOfBoundsException("Index out of range for sub-decision alternatives.");
        }   
        return alternatives.get(i).size()-1;
    }

    @Override
    public Object repairWithDependency(Object childEnc, Object parentEnc) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'repairWithDependency'");
    }
    
    



}
