package tatc.decisions;

import tatc.decisions.adg.Graph;
import tatc.tradespaceiterator.ProblemProperties;
import java.util.*;

import org.json.JSONObject;
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
public List<Map<String, Object>> decodeArchitecture(Object encoded, Solution sol, Graph graph) {
    int[] chrom = (int[]) encoded;
    int n = Lset.size();
    int m = Rset.size();

    if (chrom.length != n * m) {
        throw new IllegalArgumentException("Encoded length does not match n*m for assigning decision.");
    }

    // List to store the results
    List<Map<String, Object>> resultList = new ArrayList<>();

    // Resolve the sources for Lset and Rset
    List<Object> resolvedLset = resolveSetFromSource(this.lSource, graph);
    List<Object> resolvedRset = resolveSetFromSource(this.rSource, graph);

    // Determine the keys for Lset and Rset in the result map
    String lKey = graph.getDecisionsMap().containsKey(this.lSource) 
                ? graph.getDecisionsMap().get(this.lSource).getResultType() 
                : this.lSource;

    String rKey = graph.getDecisionsMap().containsKey(this.rSource) 
                ? graph.getDecisionsMap().get(this.rSource).getResultType() 
                : this.rSource;

    // Initialize the result list to store mappings of (L, R) pairs
    // Define sizes
    int lSize = resolvedLset.size();
    int rSize = resolvedRset.size();

    // Iterate over the chromosome
    for (int chromIndex = 0; chromIndex < chrom.length; chromIndex++) {
        if (chrom[chromIndex] == 1) {
            // Determine the indices for L and R based on the chromosome structure
            int rIndex = chromIndex / lSize; // Which R element this corresponds to
            int lIndex = chromIndex % lSize; // Which L element this corresponds to

            // Get the corresponding L and R elements
            Object lElement = resolvedLset.get(lIndex);
            Object rElement = resolvedRset.get(rIndex);

            // Create a map to store this (L, R) pair
            Map<String, Object> pairMap = new HashMap<>();

            // Add L element to the map
            if (lElement instanceof Map) {
                pairMap.put(lKey, lElement);
            } else {
                pairMap.put(lKey, createJSONObjectForResultType(lKey, lElement));
            }

            // Add R element to the map
            if (rElement instanceof Map) {
                pairMap.put(rKey, rElement);
            } else {
                pairMap.put(rKey, createJSONObjectForResultType(rKey, rElement));
            }

            // Add the pair map to the result list
            resultList.add(pairMap);
        }
    }

    return resultList;

    }

/**
 * Resolves a set based on the source name.
 */
private List<Object> resolveSetFromSource(String source, Graph graph) {
    if (graph.getDecisionsMap().containsKey(source)) {
        Decision sourceDecision = graph.getDecisionsMap().get(source);
        return sourceDecision.getResult();
    } else {
        return this.properties.getDistinctValuesForVariable(source);
    }
}

/**
 * Creates a JSON object for the given result type and source element.
 */
private JSONObject createJSONObjectForResultType(String resultType, Object sourceElement) {
    JSONObject jsonObject = new JSONObject();

    if ("satellites".equals(resultType)) {
        jsonObject.put("satellite", sourceElement);
    } else if ("orbit".equals(resultType)) {
        jsonObject.put("orbit", sourceElement);
    } else if ("payload".equals(resultType)) {
        jsonObject.put("payload", sourceElement);
    }

    return jsonObject;
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
    public void applyEncoding(int[] encoding) {
        int n = Lset.size();
        int m = Rset.size();
        if (encoding.length != n * m) {
            throw new IllegalArgumentException("Assigning mismatch: " + encoding.length
                + " vs " + n + "*" + m);
        }
    
        // Build a list of assignment records
        List<Object> assignmentList = new ArrayList<>();
    
        for (int i = 0; i < n; i++) {
            List<Object> assignedR = new ArrayList<>();
            for (int j = 0; j < m; j++) {
                if (encoding[i * m + j] == 1) {
                    assignedR.add(Rset.get(j));
                }
            }
            // For clarity, store each L element and its assigned set of R items in a Map
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("L", Lset.get(i));
            itemMap.put("AssignedR", assignedR);
            assignmentList.add(itemMap);
        }
    
        this.result = assignmentList;
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
