package tatc.decisions;

import tatc.decisions.adg.AdgSolution;
import tatc.decisions.adg.Graph;
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

    /**
     * For each sub-decision, a list of possible discrete alternatives.
     * For example, if subDecisions = ["altitude", "inclination"], 
     * alternatives.get(0) might be [400, 600, 800] for altitude,
     * alternatives.get(1) might be [30, 60, 90] for inclination.
     */
    private List<List<Object>> alternatives;
    private Random rand = new Random();
    private List<String> subDecisionsSource;
    private List<String> alternativesSource;
    private List<List<Object>> subDecisionsData;


    /**
     * Constructs a Combining decision node.
     * 
     * @param properties the problem properties object (not used here for loading, just stored)
     * @param decisionName the name or identifier of this decision node
     */
    public Combining(ProblemProperties properties, String decisionName) {
        super(properties, decisionName);
        this.alternatives = new ArrayList<>();
        this.parentDecisions = new ArrayList<Decision>();
    }

    @Override
    public void initializeDecisionVariables() {
        // In this simplified approach, we do nothing here since 
        // sub-decisions and alternatives will be provided externally.
    }
    public void setSubDecisionsSource(List<String> subDecisionsSource){
        this.subDecisionsSource = subDecisionsSource;
    }
    public void setSubDecisionsData(List<List<Object>> subdecisionData){
        this.subDecisionsData = subdecisionData;
    }
    public List<List<Object>> getSubDecisionsData(){
        return this.subDecisionsData;
    }

    /**
     * Sets the sub-decisions that form this combining decision.
     */
    // For the alternatives
    public void setAlternativesSource(List<String> altKeys) {
        this.alternativesSource = altKeys;
    }
    public List<String> getAlternativesSource() {
        return this.alternativesSource;
    }

    /**
     * Sets the alternatives for each sub-decision.
     * @param alternatives a list (for each sub-decision) of the sub-decision's possible values
     */
    public void setAlternatives(List<List<Object>> alternatives) {
        // if (alternatives.size() != subDecisionsData.size()) {
        //     throw new IllegalArgumentException("Number of alternatives lists must match number of sub-decisions.");
        // }
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
            for (int i=0; i<subDecisionsSource.size(); i++) {
                Object chosenVal = alternatives.get(i).get(current.get(i));
                arch.put(subDecisionsSource.get(i), chosenVal);
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
    public void applyEncoding(int[] encoding) {
        // 1) Check length matches subDecisionsData (each dimension)
        // if (encoding.length != subDecisionsData.size()) {
        //     throw new IllegalArgumentException(
        //         "Combining applyEncoding length mismatch. Expected " 
        //         + subDecisionsData.size() + " but got " + encoding.length
        //     );
        // }
    
        // 2) Build a list to store the chosen alternatives (one per dimension)
        List<Object> chosenAlternatives = new ArrayList<>();
    
        // 3) For each dimension i, we pick the alternative indicated by encoding[i]
        for (int i = 0; i < encoding.length; i++) {
            int chosenIndex = encoding[i];
            // If alternatives.get(i) is empty or chosenIndex out-of-range, skip or clamp
            if (i >= alternatives.size() || alternatives.get(i).isEmpty()) {
                chosenAlternatives.add(null);
                continue;
            }
            List<Object> altList = alternatives.get(i);
            if (chosenIndex < 0 || chosenIndex >= altList.size()) {
                // Either skip or clamp. Here let's skip:
                chosenAlternatives.add(null);
            } else {
                chosenAlternatives.add(altList.get(chosenIndex));
            }
        }
    
        // 4) Store these chosen alternatives as this decision's partial result
        //    We keep it as a List<Object> for consistency with your framework
        this.result = chosenAlternatives;
        // Optionally store this as lastEncoding if you need
        this.lastEncoding = encoding;
    }
    

    @Override
    public Object encodeArchitecture(Map<String, Object> architecture) {
        // Encode as an integer array representing chosen indices
        int n = subDecisionsSource.size();
        int[] encoding = new int[n];
        for (int i=0; i<n; i++) {
            String var = subDecisionsSource.get(i);
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
    public List<Map<String, Object>> decodeArchitecture(Object encoded, Solution sol, Graph graph) {
        // Cast the encoded object to int[]
        int[] chrom = (int[]) encoded;
        // Prepare a container for the decoded list
        List<Map<String, Object>> decoding = new ArrayList<>();
        Map<String, Object> decodedMap = new HashMap<String, Object>();
        for (int i = 0; i < chrom.length; i++) {
            Object decodedValue = alternatives.get(i).get(chrom[i]);
            String decodedKey = alternativesSource.get(i);
            decodedMap.put(decodedKey, decodedValue);
        }
        decoding.add(decodedMap);
    
        // Retrieve the distinct alternatives for this decision's result type 
        // e.g. if resultType = "orbit", this might be an array of orbit objects
        // List<Object> resultAlternatives = properties.getDistinctValuesForVariable(resultType);
        // if (resultAlternatives == null || resultAlternatives.isEmpty()) {
        //     // If no alternatives found, return an empty list
        //     return decoding;
        // }
    
        // // For each gene in the chromosome
        // for (int i = 0; i < chrom.length; i++) {
        //     int selection = chrom[i];
    
        //     // Safety check: clamp or skip invalid indices
        //     if (selection < 0 || selection >= resultAlternatives.size()) {
        //         // You could throw an exception, skip, or clamp. Here we skip:
        //         continue;
        //     }
    
        //     Object selectedAlternative = resultAlternatives.get(selection);
    
        //     // Create a map with the resultType as key, and the chosen alternative as value
        //     Map<String, Object> mapWithType = new HashMap<>();
        //     mapWithType.put(resultType, selectedAlternative);
    
        //     // Add this map to the decoding list
        //     decoding.add(mapWithType);
        // }
    
        return decoding;
    }
    
    // @Override
    // public List<Map<String, Object>> decodeArchitecture(Object encoded, Solution sol, Graph graph) {
    //     // Convert the encoded solution to an int array (binary representation)
    //     int[] chrom = (int[]) encoded;
    
    //     // Let n = # of sub-decision items
    //     int n = subDecisionsData.size();
    //     // Let m = # of alternative items
    //     int m = alternatives.size();
    
    //     // The expected chromosome length is n*m
    //     if (chrom.length != n ) {
    //         throw new IllegalArgumentException("Combining decision: Encoded length ("
    //             + chrom.length + ") does not match n*m (" + (n*m) + ").");
    //     }
    
    //     // The final result is a list of (subDecision, alternative) pairs
    //     List<Map<String, Object>> resultList = new ArrayList<>();
    
    //     // For each gene in the chromosome
    //     for (int idx = 0; idx < chrom.length; idx++) {
    //         if (chrom[idx] == 1) {
    //             // We interpret idx as a row-major index:
    //             // altIndex = idx / n  => which alternative
    //             // subIndex = idx % n  => which sub-decision
    //             int altIndex = idx / n;
    //             int subIndex = idx % n;
    
    //             // Retrieve the sub-decision item
    //             Object subDecItem = subDecisionsData.get(subIndex);
    //             // Retrieve the alternative item
    //             Object altItem = alternatives.get(altIndex);
    
    //             // Build a map describing the combination
    //             Map<String, Object> pairMap = new LinkedHashMap<>();
    //             pairMap.put("subDecisionItem", subDecItem);
    //             pairMap.put("alternativeItem", altItem);
    
    //             resultList.add(pairMap);
    //         }
    //     }
    
    //     return resultList;
    // }
    
    @Override
    public Object extractEncodingFromSolution(Solution solution, int offset) {
        return this.encodingMap.get(((AdgSolution)solution).getId());
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
        int[] p1 = (int[]) parent1;
        int[] p2 = (int[]) parent2;
        int len1 = p1.length;
        int len2 = p2.length;
        int childLen = Math.max(len1, len2);
        int[] child = new int[childLen];
        
        for (int i = 0; i < childLen; i++) {
            boolean p1Available = (i < len1);
            boolean p2Available = (i < len2);
            
            if (p1Available && p2Available) {
                child[i] = rand.nextBoolean() ? p1[i] : p2[i];
            } else if (p1Available) {
                child[i] = p1[i];
            } else if (p2Available) {
                child[i] = p2[i];
            }
        }
        
        // Repair the child encoding to enforce ascending order constraints.        
        return child;
    }
    
    // @Override
    // public int getNumberOfVariables() {
    //     int sdSize = (subDecisionsData == null) ? 0 : subDecisionsData.size();
    //     int altSize = (alternatives == null) ? 0 : alternatives.size();
    //     // Return the larger dimension count
    //     return Math.max(sdSize, altSize);
    // }

    @Override
    public int getNumberOfVariables() {
        // If subDecisionsData is null, return 0
        if (subDecisionsData == null) {
            return 0;
        }
        // Usually, each sub-decision dimension is one "slot" in the encoding
        return subDecisionsData.size();
    }

    
    @Override
    public Object randomEncoding() {
        // dimensionCount = number of combining dimensions
        int dimensionCount = alternatives.size();
    
        // Create the integer chromosome (one index per dimension)
        int[] encoding = new int[dimensionCount];
        // We'll also build a list of chosen objects as the partial result
        List<Object> chosenAlternatives = new ArrayList<>();
    
        // For each dimension i, randomly pick an alternative
        for (int i = 0; i < dimensionCount; i++) {
            // The i-th alternatives list
            List<Object> altList = alternatives.get(i);
            if (altList == null || altList.isEmpty()) {
                // If no alternatives, default to 0 or skip
                encoding[i] = 0;
                chosenAlternatives.add(null);
                continue;
            }
    
            int numAlts = altList.size();
            int selectedIdx = rand.nextInt(numAlts); // random index in [0, numAlts-1]
            encoding[i] = selectedIdx;
    
            // Retrieve the actual alternative object
            Object chosenAlt = altList.get(selectedIdx);
            chosenAlternatives.add(chosenAlt);
        }
    
        // Store the chosen alternatives as this decision's partial result
        this.result = chosenAlternatives;
        // Keep track of the encoding
        this.lastEncoding = encoding;
    
        // Return the chromosome
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
        // Cast encodings to int arrays.
        int[] childEncoding = (int[]) childEnc;
        int[] parentEncoding = (int[]) parentEnc;
    
        // Compute the number of subsets selected in the parent (max label in parent's encoding)
        int nSubsets = 0;
        for (int val : parentEncoding) {
            if (val > nSubsets) {
                nSubsets = val;
            }
        }
        
        // If the child encoding already has the correct length, return it.
        if (childEncoding.length == nSubsets) {
            return childEncoding;
        }
        
        // Otherwise, create a new encoding of length nSubsets.
        int[] repaired = new int[nSubsets];
        for (int i = 0; i < nSubsets; i++) {
            if (i < childEncoding.length) {
                // Copy existing value for indices available in the original encoding.
                repaired[i] = childEncoding[i];
            } else {
                // For new dimensions, generate a random valid index.
                // getMaxOptionForVariable(i) returns the maximum number of alternatives for dimension i.
                int maxOption = getMaxOptionForVariable(i);
                if (maxOption < 1) {
                    maxOption = 1; // Ensure at least one valid alternative.
                }
                repaired[i] = rand.nextInt(maxOption);
            }
        }
        
        return repaired;
    }
    

    public List<String> getSubDecisionsSource() {
        // TODO Auto-generated method stub
        return subDecisionsSource;
    }
    
    



}
