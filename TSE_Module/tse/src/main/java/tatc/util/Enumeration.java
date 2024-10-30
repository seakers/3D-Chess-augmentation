package tatc.util;


import org.hipparchus.util.FastMath;
import tatc.architecture.constellations.*;
import tatc.architecture.specifications.PassiveOpticalScanner;
import tatc.architecture.specifications.Satellite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class containing useful static methods involving enumeration of constellations
 */
public class Enumeration {

    /**
     * Maximum number of architectures allowed in the FF evaluation. Program stops when attempting to run a TSR
     * containing a larger number of architectures with FF mode and not run into memory issues.
     */
    public static final int maxNumberOfArchitecturesFF = 100000;

    /**
     * Constructor of an enumeration class
     */
    public Enumeration() {
    }

/**
 * Enumerates all the possible homogeneous Walker constellation parameters given the possible values of altitudes,
 * inclinations, number of satellites, number of planes, relative spacing parameters, payload decision variables,
 * and satellite objects.
 *
 * @param alts                    the possible values for altitude
 * @param incs                    the possible values for inclination
 * @param ts                      the possible values for number of satellites
 * @param ps                      the possible values for number of planes
 * @param fs                      the possible values for relative spacing
 * @param payloadFocalLengths     the possible values for payload focal length
 * @param payloadBitsPerPixel     the possible values for payload bits per pixel
 * @param payloadNumDetectorsRows the possible values for number of detectors rows along track
 * @param payloadApertureDias     the possible values for payload aperture diameter
 * @param eccentricity            the eccentricity of the orbits
 * @return an array list of constellation parameters containing all possible homogeneous Walker constellations
 * @throws IllegalArgumentException
 */
public static ArrayList<ConstellationParameters> fullFactHomogeneousWalker(
List<Double> alts, 
List<Object> incs, 
List<Integer> ts, 
List<Integer> ps, 
List<Integer> fs,
List<Satellite> sats,
List<Double> payloadFocalLengths,
List<Integer> payloadBitsPerPixel,
List<Integer> payloadNumDetectorsRows,
List<Double> payloadApertureDias,
double eccentricity
) throws IllegalArgumentException {
    ArrayList<ConstellationParameters> constels = new ArrayList<>();

    ts.sort(Integer::compareTo);
    if (ts.contains(0)) {
        constels.add(new HomogeneousWalkerParameters(0, 0, 0, 0, 0, null,0,0,0,0.0));
        ts.remove(0);
    }
    if (ps == null || ps.isEmpty()) {
        ArrayList<Integer> allowedPlanes = new ArrayList<>();
        for (int i = 1; i <= Collections.max(ts); i++) {
            allowedPlanes.add(i);
        }
        ps = allowedPlanes;
    }

    if (fs == null || fs.isEmpty()) {
        ArrayList<Integer> allowedRelativeSpacing = new ArrayList<>();
        for (int f = 0; f <= Collections.max(ps) - 1; f++) {
            allowedRelativeSpacing.add(f);
        }
        fs = allowedRelativeSpacing;
    }

    // Start the actual enumeration
    int archCounter = 0;
    for (Double altitude : alts) {
        for (Object inclination : incs) {
            double inclinationModified;
            if ("SSO".equals(inclination)) {
                inclinationModified = FastMath.toDegrees(Utilities.incSSO(altitude * 1000, eccentricity));
            } else if (inclination instanceof Double) {
                inclinationModified = (Double) inclination;
            } else {
                throw new IllegalArgumentException("Inclination not identified");
            }
            for (Integer t : ts) {
                ArrayList<Integer> planes = allowedPlanes(t, ps);
                for (Integer plane : planes) {
                    for (int i_f = 0; i_f <= plane - 1; ++i_f) {
                        for (Satellite sat : sats){
                            if (fs.contains(i_f)) {
                                // Iterate over payload decision variables
                                for (Double payloadFocalLength : payloadFocalLengths) {
                                    for (Integer payloadBits : payloadBitsPerPixel) {
                                        for (Integer payloadNumDetRows : payloadNumDetectorsRows) {
                                            for (Double payloadApertureDia : payloadApertureDias) {
                                                // Create instrument with specific parameters
                                                PassiveOpticalScanner instrument = new PassiveOpticalScanner(null, null, null, altitude, altitude, altitude, null, null, altitude, t, null, t, altitude, altitude, altitude, t, t);
                                                instrument.setFocalLength(payloadFocalLength);
                                                instrument.setBitsPerPixel(payloadBits);
                                                instrument.setNumberOfDetectorsRowsAlongTrack(payloadNumDetRows);
                                                instrument.setApertureDia(payloadApertureDia);
                                                // Set other instrument parameters as needed
                                                // Set other satellite parameters as needed

                                                if (archCounter < maxNumberOfArchitecturesFF) {
                                                    constels.add(new HomogeneousWalkerParameters(altitude, inclinationModified, t, plane, i_f, sat,payloadFocalLength,payloadBits,payloadNumDetRows,payloadApertureDia));
                                                    archCounter++;
                                                } else {
                                                    System.out.println("Aborting TAT-C... This design space contains too many architectures" +
                                                            " to run a full factorial enumeration. Consider using MOEA or KDO search strategies.");
                                                    System.exit(1);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    return constels;
}

    /**
     * Enumerates all the possible homogeneous Walker constellation parameters given the possible values of altitudes,
     * inclinations, number of satellites, number of planes, relative spacing parameters and satellite objects.
     * @param alts the possible values for altitude
     * @param incs the possible values for inclination
     * @param ts the possible values for number of satellites
     * @param ps the possible values for number of planes
     * @param fs the possible values for relative spacing
     * @param eccentricity the eccentricity of the orbits
     * @return an array list of constellation parameters containing all possible homogeneous Walker constellations
     * @throws IllegalArgumentException
     */
    public static ArrayList<ConstellationParameters> fullFactHomogeneousWalker(List<Double> alts, List<Object> incs, List<Integer> ts, List<Integer> ps, List<Integer> fs, double eccentricity) throws IllegalArgumentException{
        ArrayList<ConstellationParameters> constels = new ArrayList<>();

        ts.sort(Integer::compareTo);
        if (ts.contains(0)) {
            constels.add(new HomogeneousWalkerParameters(0, 0, 0, 0, 0, null,0,0,0,0.0));
            ts.remove(0);
        }

        if (ps == null){
            ArrayList<Integer> allowedPlanes = new ArrayList<>();
            for (int i=1; i<= Collections.max(ts); i++){
                allowedPlanes.add(i);
            }
            ps=allowedPlanes;
        }

        if (fs == null){
            ArrayList<Integer> allowedRelativeSpacing = new ArrayList<>();
            for (int f=0; f<= Collections.max(ps)-1; f++){
                allowedRelativeSpacing.add(f);
            }
            fs=allowedRelativeSpacing;
        }

        // Start the actual enumeration
        int archCounter = 0;
        for(Double altitude : alts) {
            for(Object inclination : incs) {
                double inclinationModified = -1;
                if (inclination.equals("SSO")){
                    inclinationModified = FastMath.toDegrees(Utilities.incSSO(altitude*1000,eccentricity));
                } else if (inclination instanceof Double){
                    inclinationModified = (Double) inclination;
                } else {
                    throw new IllegalArgumentException("Inclination not identified");
                }
                for(Integer t : ts) {

                    ArrayList<Integer> planes = allowedPlanes(t,ps);

                    for(Integer plane : planes) {
                        for(int i_f = 0; i_f <= plane - 1; ++i_f) {
                            if (fs.contains(i_f)) {
                                if (archCounter <= maxNumberOfArchitecturesFF){
                                    constels.add(new HomogeneousWalkerParameters(0, 0, 0, 0, 0, null,0,0,0,0.0));
                                    archCounter++;
                                }else{
                                    System.out.println("Aborting TAT-C... This design space contains too many architectures" +
                                            " to run a full factorial enumeration. Consider using MOEA or KDO search strategies.");
                                    System.exit(1);
                                }
                            }
                        }
                    }
                }
            }
        }
        return constels;
    }

    /**
     * Enumerates all the possible heterogeneous Walker constellation parameters given the possible values of altitudes,
     * inclinations, number of satellites, number of planes, relative spacing parameters and satellite objects.
     * @param alts the possible values for altitude
     * @param incs the possible values for inclination
     * @param ts the possible values for number of satellites
     * @param ps the possible values for number of planes
     * @param fs the possible values for relative spacing
     * @param sats the possible satellites
     * @param eccentricity the eccentricity of the orbits
     * @return an array list of constellation parameters containing all possible heterogeneous Walker constellations
     * @throws IllegalArgumentException
     */
    public static ArrayList<ConstellationParameters> fullFactHeterogeneousWalker(List<Double> alts, List<Object> incs, List<Integer> ts, List<Integer> ps, List<Integer> fs, List<Satellite> sats, double eccentricity){
        ArrayList<ConstellationParameters> constels= new ArrayList<>();
        int archCounter = 0;
        for (Integer t : ts){
            if (t == 0){
                constels.add(new HeterogeneousWalkerParameters(0,0,0,null,null));
            }else {
                ArrayList<Integer> planes = allowedPlanes(t,ps);
                for (Integer p : planes){
                    //Generate all possible Walker planes with t/p satellites in each plane and store them in walkerPlanesTotal
                    ArrayList<WalkerPlane> walkerPlanesTotal = new ArrayList<>();
                    for (Integer tt : ts){
                        if (tt!=0){
                            ArrayList<Integer> pplanes = allowedPlanes(tt,ps);
                            for (Integer pp : pplanes){
                                if (tt/pp == t/p){
                                    ArrayList<Integer> tArrayList=new ArrayList<>();
                                    ArrayList<Integer> pArrayList=new ArrayList<>();
                                    tArrayList.add(tt);
                                    pArrayList.add(pp);
                                    ArrayList<WalkerPlane> walkerPlanes = fullFactPlanesWalker(alts,incs,tArrayList,pArrayList,fs,eccentricity);
                                    for (WalkerPlane wp : walkerPlanes){
                                        if (!walkerPlanesTotal.contains(wp)){
                                            walkerPlanesTotal.add(wp);
                                        }
                                    }
                                }
                            }
                        }

                    }
                    archCounter = archCounter + Combinatorics.binomial(walkerPlanesTotal.size(),p).intValue();
                    if (archCounter > maxNumberOfArchitecturesFF){
                        System.out.println("Aborting TAT-C... This design space contains too many architectures" +
                                " to run a full factorial enumeration. Consider using MOEA or KDO search strategies.");
                        System.exit(1);
                    }
                    //For the values of t and p, compute all the possible Heterogeneous constellations with p planes from walkerPlanesTotal.
                    for (List<WalkerPlane> planeCombination : Combinatorics.combination(walkerPlanesTotal,p)){
                        for (Satellite sat : sats){
                            constels.add(new HeterogeneousWalkerParameters(t,p,-1,planeCombination,sat));
                        }
                    }

                }
            }

        }
        return constels;
    }

    /**
     * Enumerates all the possible homogeneous Walker planes given the possible values of altitudes,
     * inclinations, number of satellites, number of planes, relative spacing parameters and satellites.
     * @param alts the possible values for altitude
     * @param incs the possible values for inclination
     * @param ts the possible values for number of satellites
     * @param ps the possible values for number of planes
     * @param fs the possible values for relative spacing
     * @param eccentricity the eccentricity of the orbits
     * @return an array list containing all possible walker planes
     * @throws IllegalArgumentException
     */
    public static ArrayList<WalkerPlane> fullFactPlanesWalker(List<Double> alts, List<Object> incs, List<Integer> ts, List<Integer> ps, List<Integer> fs, double eccentricity){
        ArrayList<WalkerPlane> walkerPlanesTotal= new ArrayList<>();
        ArrayList<ConstellationParameters> walkerParameters = fullFactHomogeneousWalker(alts,incs,ts,ps,fs,eccentricity);
        for (ConstellationParameters w : walkerParameters){
            ArrayList<WalkerPlane> walkerPlanes = createWalkerPlanes(((HomogeneousWalkerParameters)w).getA(),((HomogeneousWalkerParameters)w).getI(),
                    ((HomogeneousWalkerParameters)w).getT(),((HomogeneousWalkerParameters)w).getP(),((HomogeneousWalkerParameters)w).getF());
            for (WalkerPlane p : walkerPlanes){
                if (!walkerPlanesTotal.contains(p)){
                    walkerPlanesTotal.add(p);
                }
            }
        }
        return walkerPlanesTotal;
    }

    /**
     * Enumerates all planes of a given Walker constellation defined by its altitude, inclination, number of satellites,
     * number of planes and relative spacing parameter
     * @param a the altitude of the Walker constellation
     * @param i the inclination of the Walker constellation
     * @param t the number of satellites in the Walker constellation
     * @param p the number of planes in the Walker constellation
     * @param f the relative spacing parameter of the Walker constellation
     * @return an array list containing all possible walker planes
     * @throws IllegalArgumentException
     */
    @SuppressWarnings("Duplicates")
    public static ArrayList<WalkerPlane> createWalkerPlanes(double a, double i, int t, int p, int f){
        ArrayList<WalkerPlane> walkerPlanes= new ArrayList<>();
        //checks for valid parameters
        if (t < 0 || p < 0) {
            throw new IllegalArgumentException(String.format("Expected t>0, p>0."
                    + " Found f=%d and p=%d", t, p));
        }
        if ((t % p) != 0) {
            throw new IllegalArgumentException(
                    String.format("Incompatible values for total number of "
                            + "satellites <t=%d> and number of planes <p=%d>. "
                            + "t must be divisible by p.", t, p));
        }
        if (f < 0 && f > p - 1) {
            throw new IllegalArgumentException(
                    String.format("Expected 0 <= f <= p-1. "
                            + "Found f = %d and p = %d.", f, p));
        }

        //Uses ArchitectureCreator delta pa
        final int s = t / p; //number of satellites per plane
        final double pu = 2 * FastMath.PI / t; //pattern unit
        final double delAnom = pu * p; //in plane spacing between satellites
        final double delRaan = pu * s; //node spacing
        final double phasing = pu * f;
        final double refAnom = 0;
        final double refRaan = 0;

        for (int planeNum = 0; planeNum < p; planeNum++) {
            double raan = FastMath.toDegrees(refRaan + planeNum * delRaan);
            List<Double> tas = new ArrayList<>();
            for (int satNum = 0; satNum < s; satNum++) {
                        tas.add(FastMath.toDegrees((refAnom + satNum * delAnom + phasing * planeNum) % (2. * FastMath.PI)));
            }
            walkerPlanes.add(new WalkerPlane(a,i,raan,tas));
        }
        return walkerPlanes;
    }

    /**
     * Enumerates all the possible train constellation parameters given the possible values of altitudes, number of satellites,
     * LTANs of the first (reference satellite), duration of time between satellites and satellites.
     * @param alts the possible values for altitude
     * @param ts the possible values for number of satellites
     * @param LTANs the possible values for Longitude Time of the Ascending Node of the first(reference) satellite
     *              in "hh:mm:ss" format
     * @param satIntervals the possible values for duration of time between satellites in the train constellation in
     *                     ISO 8601 duration format
     * @param sats the possible satellites
     * @return an array list of constellation parameters containing all possible train constellations
     * @throws IllegalArgumentException
     */
    public static ArrayList<ConstellationParameters> fullFactTrain(List<Double> alts, List<Integer> ts, List<String> LTANs, List<String> satIntervals, List<Satellite> sats) {
        ArrayList<ConstellationParameters> constels = new ArrayList<>();

        ts.sort(Integer::compareTo);
        if (ts.contains(0)) {
            constels.add(new TrainParameters(0,0,null,null,null));
            ts.remove(0);
        }

        int archCounter = 0;
        for (Double alt : alts){
            for (Integer t : ts){
                for (String LTAN : LTANs){
                    for (String satInterval : satIntervals){
                        for (Satellite sat : sats){
                            if (archCounter <= maxNumberOfArchitecturesFF){
                                constels.add(new TrainParameters(alt,t,LTAN,satInterval,sat));
                                archCounter++;
                            }else{
                                System.out.println("Aborting TAT-C... This design space contains too many architectures" +
                                        " to run a full factorial enumeration. Consider using MOEA or KDO search strategies.");
                                System.exit(1);
                            }
                        }
                    }
                }
            }
        }
        return constels;
    }

    /**
     * Enumerates all the possible Ad-hoc constellation parameters given the possible values of number of satellites
     * and satellite objects.
     * @param ts the possible values for number of satellites
     * @param sats the possible satellites
     * @return an array list of constellation parameters containing all possible Ad-hoc constellations
     * @throws IllegalArgumentException
     */
    public static ArrayList<ConstellationParameters> fullFactAdHoc(List<Integer> ts, List<Satellite> sats) {
        ArrayList<ConstellationParameters> constels = new ArrayList<>();

        ts.sort(Integer::compareTo);
        if (ts.contains(0)) {
            constels.add(new AdHocParameters(0,null));
            ts.remove(0);
        }

        for (Integer t : ts){
            for (Satellite sat : sats) {
                constels.add(new AdHocParameters(t, sat));
            }
        }
        return constels;
    }

    /**
     * Method that filters the possible number of planes for a constellation with equal number of satellites in
     * each plane given the total number of satellites and a list of allowed planes (potentially defined in the TSR).
     * For instance, if t=3 and ps=[1,2,3], this method would return [1,3].
     * @param t the total number of satellites in the constellation
     * @param ps the allowed number of planes in the constellation
     * @return the subset of the given list of allowed number of planes for the given total number of satellites
     */
    public static ArrayList<Integer> allowedPlanes(int t, List<Integer>ps){
        ArrayList<Integer> allowedPlanes = new ArrayList<>();
        if (t != 1 && ps.contains(1)) {
            allowedPlanes.add(1);
        }
        int i_p;
        for(i_p = 2; i_p <= t / 2; ++i_p) {
            if (t % i_p == 0 && ps.contains(i_p)) {
                allowedPlanes.add(i_p);
            }
        }
        if ( ps.contains(t)){
            allowedPlanes.add(t);
        }
        return allowedPlanes;
    }

    /**
     * Method that filters the possible number of satellites for a constellation with equal number of satellites in
     * each plane given the total number of planes and a list of allowed number of satellites (potentially defined
     * in the TSR).
     * For instance, if p=3 and ps=[1,2,3,4,5,6,7,8,9,10,11,12], this method would return [3,6,9,12].
     * @param p the total number of planes in the constellation
     * @param ts the allowed number of satellites in the constellation
     * @return the subset of the given list of allowed number of planes for the given total number of satellites
     */
    public static ArrayList<Integer> allowedNumberOfSatellites(int p, List<Integer>ts){
        ArrayList<Integer> allowedNumberofSatellites = new ArrayList<>();
        if (ts.contains(0)){
            allowedNumberofSatellites.add(0);
        }
        for(int i_t = Collections.max(ts); i_t >= p ; --i_t) {
            if (i_t % p == 0 && ts.contains(i_t)) {
                allowedNumberofSatellites.add(i_t);
            }
        }
        return allowedNumberofSatellites;
    }
}