package tatc.architecture.variable;

import java.util.*;

import org.moeaframework.core.PRNG;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.BinaryVariable;
import tatc.architecture.specifications.GroundNetwork;
import tatc.architecture.specifications.GroundStation;

/**
 * A variable containing the information for a ground network
 */
public class GroundNetworkVariable implements Variable {
    /**
     * List of possible ground networks
     */
    private final List<GroundNetwork> groundNetworksAllowed;

    /**
     * List of available ground stations on the tradespace search request JSON file
     */
    private final List<GroundStation> groundStationsAllowed;

    /**
     * Map of ground network specification ID and the number of possible ground
     * stations for that ground network specification
     */
    private final HashMap<Integer,List<Integer>> numberGroundStationsAllowed;

    /**
     * The ground network
     */
    private GroundNetwork gn;

    /**
     * Constructs a ground network variable given the list of possible ground networks to choose from,
     * the list of possible ground stations available that form the ground networks and the map containing the
     * number of possible ground stations for each ground network specification ID.
     * The ground network is initialized to null.
     * @param groundNetworksAllowed the list of possible ground networks
     * @param groundStationsAllowed the list of available ground stations
     * @param numberGroundStationsAllowed the map of ground network specification ID and the number of possible ground stations
     */
    public GroundNetworkVariable(List<GroundNetwork> groundNetworksAllowed, List<GroundStation> groundStationsAllowed, HashMap<Integer,List<Integer>> numberGroundStationsAllowed) {
        this.groundNetworksAllowed = groundNetworksAllowed;
        this.groundStationsAllowed = groundStationsAllowed;
        this.numberGroundStationsAllowed = numberGroundStationsAllowed;
        this.gn = null;
    }


    /**
     * Copies the fields of the given ground network variable and creates a new
     * instance of a ground network variable.
     *
     * @param var the ground network variable to copy
     */
    protected GroundNetworkVariable(GroundNetworkVariable var) {
        this.groundNetworksAllowed = var.groundNetworksAllowed;
        this.groundStationsAllowed = var.groundStationsAllowed;
        this.numberGroundStationsAllowed = var.numberGroundStationsAllowed;
        this.gn = var.getGroundNetwork();
    }

    /**
     * Gets the ground network
     *
     * @return the ground network
     */
    public GroundNetwork getGroundNetwork() {
        return gn;
    }

    /**
     * Sets the ground network
     *
     * @param gn the new ground network
     */
    public void setGroundNetwork(GroundNetwork gn) {
        if (groundNetworksAllowed.contains(gn)) {
            this.gn = gn;
        } else {
            throw new IllegalArgumentException(String.format("ground network not included in TradespaceSearch.json"));
        }
    }

    /**
     * Transforms this gn into a binary variable containing information about which ground stations are selected.
     * This binary variable is used during crossover and mutation of the ground network variable.
     * @return a binary variable containing information about what ground stations form this ground network
     */
    public BinaryVariable groundNetworkToBinaryVariable(){
        BinaryVariable binary = new BinaryVariable(groundStationsAllowed.size());

        for (int i = 0 ; i < groundStationsAllowed.size() ; i++){
            if (gn.getGroundStations().contains(groundStationsAllowed.get(i))){
                binary.set(i,true);
            }else{
                binary.set(i,false);
            }
        }

        return binary;
    }

    /**
     * Set the ground network given a binary variable containing information about which ground stations are selected
     * @param binary the binary variable containing information about what ground stations form this ground network
     * @param agencyType the ground network agency type
     */
    public void setGroundNetworkFromBinaryVariable(BinaryVariable binary, String agencyType){
        List<GroundStation> groundStations = new ArrayList<>();
        this.repairBinaryVariable(binary,agencyType);
        for (int i = 0 ; i < binary.getNumberOfBits(); i++){
            if (binary.get(i) == true){
                groundStations.add(groundStationsAllowed.get(i));
            }
        }
        GroundNetwork newGroundNetwork = new GroundNetwork(this.gn.getName(),this.gn.getAcronym(),this.gn.getAgency(),
                groundStations.size(),groundStations);
        newGroundNetwork.setMutable(true);
        newGroundNetwork.setId(this.gn.getId());
        this.setGroundNetwork(newGroundNetwork);

    }

    /**
     * Repairs the binary variable containing information about which ground stations are selected. This is necessary
     * because after crossover or mutation, infeasible chromosomes can be created, such as ground networks with incorrect
     * number of ground stations or ground networks containing ground stations of different agencies
     * @param binary the binary variable containing information about which ground stations are selected
     * @param agencyType the agency type for this ground network
     */
    @SuppressWarnings("Duplicates")
    private void repairBinaryVariable(BinaryVariable binary, String agencyType){
        for (int i = 0; i<binary.getNumberOfBits(); i++){
            boolean nonNull = (groundStationsAllowed.get(i).getAgency() != null) && (agencyType != null);
            if ( nonNull && (binary.get(i) && !groundStationsAllowed.get(i).getAgency().getAgencyType().equalsIgnoreCase(agencyType))){
                binary.set(i,false);
            }
        }

        List<Integer> numberGroundStationsPossible = numberGroundStationsAllowed.get(gn.getId());
        if (!numberGroundStationsPossible.contains(binary.cardinality())){
            int index = PRNG.nextInt(0,numberGroundStationsPossible.size()-1);
            int numberStations = numberGroundStationsPossible.get(index);
            int onesToFlipNumber = binary.cardinality() - numberStations;
            if (onesToFlipNumber>0){
                List<Integer> ones = new ArrayList<>();
                for (int i=0 ;i<binary.getNumberOfBits();i++){
                    boolean Null = (groundStationsAllowed.get(i).getAgency() == null) || (agencyType == null);
                    if (binary.get(i) && (Null || groundStationsAllowed.get(i).getAgency().getAgencyType().equalsIgnoreCase(agencyType))){
                        ones.add(i);
                    }
                }
                HashSet<Integer> onesToFlip = new HashSet<>();
                while (onesToFlip.size()<onesToFlipNumber){
                    onesToFlip.add(PRNG.nextInt(0,ones.size()-1));
                }
                for (Integer oneToFlip : onesToFlip){
                    binary.set(ones.get(oneToFlip),false);
                }
            }else{
                int zerosToFlipNumber = - onesToFlipNumber;
                List<Integer> zeros = new ArrayList<>();
                for (int i=0 ;i<binary.getNumberOfBits();i++){
                    boolean Null = (groundStationsAllowed.get(i).getAgency() == null) || (agencyType == null);
                    if (!binary.get(i) && (Null || groundStationsAllowed.get(i).getAgency().getAgencyType().equalsIgnoreCase(agencyType))){
                        zeros.add(i);
                    }
                }
                HashSet<Integer> zerosToFlip = new HashSet<>();
                while (zerosToFlip.size()<zerosToFlipNumber){
                    zerosToFlip.add(PRNG.nextInt(0,zeros.size()-1));
                }
                for (Integer zeroToFlip : zerosToFlip) {
                    binary.set(zeros.get(zeroToFlip),true);
                }
            }
        }
    }


    @Override
    public Variable copy() {
        return new GroundNetworkVariable(this);
    }

    @Override
    public void randomize() {
        int index = PRNG.nextInt(0,groundNetworksAllowed.size()-1);
        this.setGroundNetwork(groundNetworksAllowed.get(index));
    }

    /**
     * Gets the list of possible ground networks
     * @return the list of possible ground networks
     */
    public List<GroundNetwork> getGroundNetworksAllowed() {
        return groundNetworksAllowed;
    }

    /**
     * Gets the list of available ground stations
     * @return the list of available ground stations
     */
    public List<GroundStation> getGroundStationsAllowed() {
        return groundStationsAllowed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroundNetworkVariable that = (GroundNetworkVariable) o;
        return Objects.equals(groundNetworksAllowed, that.groundNetworksAllowed) &&
                Objects.equals(groundStationsAllowed, that.groundStationsAllowed) &&
                Objects.equals(gn, that.gn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groundNetworksAllowed, groundStationsAllowed, gn);
    }
}