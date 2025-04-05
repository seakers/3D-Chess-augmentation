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
    
        // Resolve the sources for Lset and Rset.
        List<Object> resolvedLset = resolveSetFromSource(this.lSource, graph);
        List<Object> resolvedRset = resolveSetFromSource(this.rSource, graph);
    
        // Determine keys to use for L and R in the final map.
        String lKey = graph.getDecisionsMap().containsKey(this.lSource)
                ? graph.getDecisionsMap().get(this.lSource).getResultType()
                : this.lSource;
        String rKey = graph.getDecisionsMap().containsKey(this.rSource)
                ? graph.getDecisionsMap().get(this.rSource).getResultType()
                : this.rSource;
    
        // Group L elements by their corresponding R index.
        // Map: rIndex -> List of L elements.
        Map<Integer, List<Object>> rIndexToLList = new HashMap<>();
        int lSize = resolvedLset.size();
        for (int chromIndex = 0; chromIndex < chrom.length; chromIndex++) {
            if (chrom[chromIndex] == 1) {
                int rIndex = chromIndex / lSize;
                int lIndex = chromIndex % lSize;
                Object lElement = resolvedLset.get(lIndex);
                // If lElement is not a Map, wrap it using our helper.
                // if (!(lElement instanceof Map)) {
                //     lElement = createJSONObjectForResultType(lKey, lElement);
                // }
                rIndexToLList.computeIfAbsent(rIndex, k -> new ArrayList<>()).add(lElement);
            }
        }
    
        // Build the final result: one map per R element.
        List<Map<String, Object>> resultList = new ArrayList<>();
        for (Map.Entry<Integer, List<Object>> entry : rIndexToLList.entrySet()) {
            int rIndex = entry.getKey();
            List<Object> lElements = entry.getValue();
            Object rElement = resolvedRset.get(rIndex);
            if (!(rElement instanceof Map)) {
                rElement = createJSONObjectForResultType(rKey, rElement);
            }
            Map<String, Object> map = new HashMap<>();
            map.put(rKey, rElement);
            // Here we store the list of L elements under lKey.
            map.put(lKey, lElements);
            resultList.add(map);
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
        // Cast the inputs to integer arrays.
        int[] childChrom = (int[]) childEnc;
        
        // Expected dimensions: n * m, where n = Lset.size() and m = Rset.size()
        int n = Lset.size();
        int m = Rset.size();
        int expectedLength = n * m;
        
        // 1. Resize the encoding if necessary.
        if (childChrom.length != expectedLength) {
            int[] repaired = new int[expectedLength];
            // Copy as many elements as possible from childChrom.
            for (int i = 0; i < Math.min(childChrom.length, expectedLength); i++) {
                repaired[i] = childChrom[i];
            }
            // Fill remaining positions with 0.
            for (int i = childChrom.length; i < expectedLength; i++) {
                repaired[i] = 0;
            }
            childChrom = repaired;
        }
        
        // 2. Enforce that each L element (each column) is assigned at most once.
        // The encoding mapping: for index i (0 <= i < n*m):
        //    lIndex = i % n, rIndex = i / n.
        // For each L element (column index i), ensure that at most one row has a value 1.
        for (int i = 0; i < n; i++) {
            int onesCount = 0;
            for (int r = 0; r < m; r++) {
                int index = r * n + i;
                if (childChrom[index] == 1) {
                    onesCount++;
                }
            }
            // If more than one assignment exists for this L element, keep the first one and clear the rest.
            if (onesCount > 1) {
                boolean kept = false;
                for (int r = 0; r < m; r++) {
                    int index = r * n + i;
                    if (childChrom[index] == 1) {
                        if (!kept) {
                            kept = true; // keep the first encountered 1
                        } else {
                            childChrom[index] = 0; // clear any additional 1's
                        }
                    }
                }
            }
        }
        
        // (Optional) You might also want to ensure that each L element has at least one assignment.
        // For now, we leave columns with no assignment unchanged.
        
        return childChrom;
    }
    
    
    
}
