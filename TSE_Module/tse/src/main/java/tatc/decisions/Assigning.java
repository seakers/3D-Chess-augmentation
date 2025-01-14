package tatc.decisions;

import tatc.tradespaceiterator.ProblemProperties;
import java.util.*;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.RealVariable;

/**
 * Assigning Pattern - Each entity in L can be assigned to any subset of R.
 * Encoded as a binary matrix M of size n×m (n = |L|, m = |R|), 
 * flattened into an integer array of length n*m.
 * M[i*m + j] = 1 if L[i] assigned to R[j], else 0.
 */
public class Assigning extends Decision {

    private List<Object> Lset; // Entities in L
    private List<Object> Rset; // Entities in R
    private Random rand = new Random();
    private String lSource;
    private String rSource;

    /**
     * Constructing an Assigning decision node.
     * @param properties the problem properties
     * @param decisionName the decision node name
     */
    public Assigning(ProblemProperties properties, String decisionName) {
        super(properties, decisionName);
        this.Lset = new ArrayList<>();
        this.Rset = new ArrayList<>();
    }

    /**
     * Set the L entities.
     * @param Lset a list of entities in L
     */
    public void setLset(List<Object> Lset) {
        this.Lset = new ArrayList<>(Lset);
    }
    public void setLSource(String entitiesSource) {
        this.lSource = entitiesSource;
    }
    
    public String getLSource() {
        return this.lSource;
    }

    public void setRSource(String rSource) {
        this.rSource = rSource;
    }
    
    public String getRSource() {
        return this.rSource;
    }




    /**
     * Set the R entities.
     * @param Rset a list of entities in R
     */
    public void setRset(List<Object> Rset) {
        this.Rset = new ArrayList<>(Rset);
    }

    @Override
    public void initializeDecisionVariables() {
        // Lset and Rset should be set by external methods.
        // No direct initialization needed if sets are already assigned.
    }

    @Override
    public Iterable<Map<String, Object>> enumerateArchitectures() {
        // Full enumeration of all assignments would be 2^(n*m).
        // That's huge and typically not feasible for large sets.
        // If needed, we can return a reduced set or skip full enumeration.
        // For now, return an empty list or consider a small scenario.
        return Collections.emptyList();
    }

    @Override
    public Object encodeArchitecture(Map<String, Object> architecture) {
        // We expect something like:
        // architecture.get(decisionName) => Map<Lentity, List<Rentity>>
        Object obj = architecture.get(decisionName);
        if (!(obj instanceof Map)) {
            throw new IllegalArgumentException("Assigning decision expects a map under decisionName key.");
        }

        @SuppressWarnings("unchecked")
        Map<Object, List<Object>> assignmentMap = (Map<Object, List<Object>>) obj;

        int n = Lset.size();
        int m = Rset.size();
        int[] encoding = new int[n * m];

        // Initialize all to 0
        Arrays.fill(encoding, 0);

        // For each L[i], get assigned R
        for (int i = 0; i < n; i++) {
            Object Li = Lset.get(i);
            List<Object> assignedR = assignmentMap.get(Li);
            if (assignedR != null) {
                for (Object Rj : assignedR) {
                    int j = Rset.indexOf(Rj);
                    if (j < 0) {
                        throw new IllegalArgumentException("R entity " + Rj + " not found in Rset.");
                    }
                    encoding[i * m + j] = 1;
                }
            }
        }

        return encoding;
    }

    @Override
    public List<Map<String, Object>> decodeArchitecture(Object encoded,Solution sol) {
        int[] chrom = (int[]) encoded;
        int n = Lset.size();
        int m = Rset.size();
    
        if (chrom.length != n * m) {
            throw new IllegalArgumentException("Encoded length does not match n*m for assigning decision.");
        }
    
        // List to store the results
        List<Map<String, Object>> resultList = new ArrayList<>();
    
        // Loop over each element in Lset
        for (int i = 0; i < n; i++) {
            // Extract the attributes of the L element
            Object lElement = Lset.get(i);
            Map<String, Object> elementMap = new HashMap<>();
    
            if (lElement instanceof Map) {
                // If Lset elements are maps, copy the key-value pairs (like "altitude": 500, "inclination": 45)
                elementMap.putAll((Map<String, Object>) lElement);
            } else {
                // If Lset is just a list of simple elements, we'll name it generically
                elementMap.put("L_value", lElement);
            }
    
            // Extract the R assignments corresponding to this L element
            Set<Object> assignedR = new HashSet<>();
            for (int j = 0; j < m; j++) {
                if (chrom[i * m + j] == 1) {
                    assignedR.add(Rset.get(j));
                }
            }
    
            // Add the R assignments to the hashmap
            elementMap.put(decisionName, assignedR); // Use decisionName as the key for the R assignments
    
            // Add the element map to the result list
            resultList.add(elementMap);
        }
    
        return resultList;
    }
    
    
    @Override
public Object extractEncodingFromSolution(Solution solution, int offset) {
    int length = getNumberOfVariables(); // should be n*m
    int[] encoding = new int[length];

    for (int i = 0; i < length; i++) {
        double val = ((RealVariable) solution.getVariable(offset + i)).getValue();
        // Convert to binary (0 or 1)
        // If val is close to 0, this means "not assigned"
        // If val is close to 1, this means "assigned"
        int bit = (int)Math.round(val);
        if (bit < 0 || bit > 1) {
            throw new IllegalArgumentException("Assigning decision variable out of binary range: " + bit);
        }
        encoding[i] = bit;
    }

    return encoding;
}

    @Override
    public void mutate(Object encoded) {
        int[] chrom = (int[]) encoded;
        double mutationProbability = 0.05; // example
        for (int k = 0; k < chrom.length; k++) {
            if (rand.nextDouble() < mutationProbability) {
                // Flip bit
                chrom[k] = (chrom[k] == 0) ? 1 : 0;
            }
        }
    }

    @Override
    public Object crossover(Object parent1, Object parent2) {
        int[] p1 = (int[]) parent1;
        int[] p2 = (int[]) parent2;
        if (p1.length != p2.length) {
            throw new IllegalArgumentException("Parents differ in length.");
        }

        int[] child = new int[p1.length];
        for (int i = 0; i < p1.length; i++) {
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
        // Number of variables is n*m where n=|L|, m=|R|
        return Lset.size() * Rset.size();
    }
    @Override
    public int[] getLastEncoding() {
        return lastEncoding;
    }

    @Override
    public Object randomEncoding() {
        int n = Lset.size();
        int m = Rset.size();
        int[] encoding = new int[n * m];
        for (int i = 0; i < encoding.length; i++) {
            encoding[i] = rand.nextBoolean() ? 1 : 0;
        }
        return encoding;
    }

    @Override
    public int getMaxOptionForVariable(int i) {
        // Each variable is binary, so max option count is 2
        // (0 or 1)
        return 2;
    }
    public void addParentDecision(Decision parent) {
        parentDecisions.add(parent);
    }

    @Override
    public Object repairWithDependency(Object childEnc, Object parentEnc) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'repairWithDependency'");
    }
    
    
}
