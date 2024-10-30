/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.architecture;

import org.hipparchus.util.FastMath;

import com.google.gson.internal.LinkedTreeMap;

import seakers.conmop.variable.SatelliteVariable;
import tatc.ResultIO;
import tatc.architecture.constellations.WalkerPlane;
import tatc.architecture.specifications.*;
import tatc.util.AbsoluteDate;
import tatc.util.JSONIO;
import tatc.util.TLESatellite;
import tatc.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Class that contains the constellations and ground network assigned to an architecture and is able to create
 * the arch.json file
 */
public class ArchitectureCreator implements ArchitectureMethods{

    /**
     * List of constellations assigned to an architecture
     */
    private List<Constellation> constellations;

    /**
     * Ground network assigned to an architecture
     */
    private GroundNetwork groundNetwork;

    /**
     * Planet labs data base for ad-hoc constellations
     */
    private static final List<TLESatellite> planetDatabase = ResultIO.getPlanetLabsEphemerisDatabase();

    /**
     * Initializes an architecture creator
     */
    public ArchitectureCreator(){
        this.constellations = new ArrayList<>();
        this.groundNetwork = null;
    }

    /**
    * Adds a walker delta-pattern constellation in the list of constellations inside the architecture.
    * @param semimajoraxis the semi-major axis in km
    * @param inc the inclination in deg
    * @param t the number of satellites
    * @param p the number of planes
    * @param f the relative spacing (values from 0 to p-1)
    * @param satellite the satellite (template)
    * @param payloadFocalLength the payload focal length
    * @param payloadBitsPerPixel the payload bits per pixel
    * @param payloadNumDetectorsRows the payload number of detectors rows along track
    * @param payloadApertureDia the payload aperture diameter
    * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
    * @param epoch the epoch in ISO 8601 format
    * @param eccentricity the eccentricity of all the orbits
    */
    public void addHomogeneousWalker(double semimajoraxis, double inc, int t, int p, int f, Satellite satellite, Boolean secondaryPayload, double payloadApertureDia, int payloadBitsPerPixel, double payloadFocalLength, int payloadNumDetectorsRows,String epoch, double eccentricity) {

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
        //If orbit is elliptic, argument of perigee is going to be equally spaced among planes
        final double refPerigee = 0;
        double delPerigee = 0;
        if (eccentricity != 0.0){
            delPerigee = delRaan;
        }

        List<Orbit> listOrbits = new ArrayList<>();
        for (int planeNum = 0; planeNum < p; planeNum++) {
            for (int satNum = 0; satNum < s; satNum++) {
                Orbit orbit = new Orbit("KEPLERIAN",null,
                        semimajoraxis,inc,eccentricity,FastMath.toDegrees(refPerigee + planeNum * delPerigee),
                        FastMath.toDegrees(refRaan + planeNum * delRaan),
                        FastMath.toDegrees((refAnom + satNum * delAnom + phasing * planeNum) % (2. * FastMath.PI)), epoch, null);
                listOrbits.add(orbit);
            }
        }
        // Modify the instrument in the satellite's payload
    List<? super Instrument> payload = satellite.getPayload();

    if (payload != null && !payload.isEmpty()) {
        // Access the first element of the payload (should be a LinkedTreeMap)
        Object instrumentObj = payload.get(0);

        if (instrumentObj instanceof LinkedTreeMap) {
            LinkedTreeMap<String, Object> instrumentMap = (LinkedTreeMap<String, Object>) instrumentObj;

            // Set the instrument properties
            instrumentMap.put("focalLength", payloadFocalLength);
            instrumentMap.put("apertureDia", payloadApertureDia);
            instrumentMap.put("numberOfDetectorsRowsAlongTrack", payloadNumDetectorsRows);
            instrumentMap.put("bitsPerPixel", payloadBitsPerPixel);
        } else {
            // Handle cases where the instrument is not a LinkedTreeMap
            // You may need to deserialize or cast appropriately
            System.err.println("Instrument is not a LinkedTreeMap. Cannot set properties.");
        }
    } else {
        System.err.println("Payload is empty or null. Cannot modify instrument properties.");
    }

    // Set the modified payload back to the satellite
    satellite.setPayload(payload);

        List<Satellite> satellites = new ArrayList<>();
        for (Orbit orbit : listOrbits){
            satellites.add(new Satellite(satellite.getName(),
                    satellite.getAcronym(),
                    satellite.getAgency(),
                    satellite.getMass(),
                    satellite.getDryMass(),
                    satellite.getVolume(),
                    satellite.getPower(),
                    satellite.getCommBand(),
                    satellite.getPayload(),
                    orbit,
                    satellite.getTechReadinessLevel(),
                    satellite.isGroundCommand(),
                    satellite.isSpare(),
                    satellite.getPropellantType(),
                    satellite.getStabilizationType()));
        }
        Constellation constellation=new Constellation("DELTA_HOMOGENEOUS", t,p,f, null,null,satellites,secondaryPayload);
        constellations.add(constellation);
    }

    /**
     * Adds a heterogeneous walker constellation in the list of constellations inside the architecture.
     * @param t the total number of satellites in the constellation
     * @param planes the list of walker planes forming the constellation
     * @param satellite the satellite in the constellation
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     * @param epoch the epoch in 8601 format
     * @param eccentricity the eccentricity of all the orbits
     */
    public void addHeterogenousWalker(int t, List<WalkerPlane> planes, Satellite satellite, Boolean secondaryPayload, String epoch, double eccentricity){
        List<Orbit> listOrbits = new ArrayList<>();
        for (WalkerPlane p : planes) {
            for (double ta : p.getTas()){
                double argPerigee = 0.0;
                if (eccentricity != 0.0){
                    argPerigee = p.getRaan();
                }
                Orbit orbit = new Orbit("KEPLERIAN",null,
                        p.getA()+Utilities.EARTH_RADIUS_KM,p.getI(),eccentricity,argPerigee,
                        p.getRaan(), ta, epoch, null);
                listOrbits.add(orbit);
            }
        }

        List<Satellite> satellites = new ArrayList<>();
        for (Orbit orbit : listOrbits){
            satellites.add(new Satellite(satellite.getName(),
                    satellite.getAcronym(),
                    satellite.getAgency(),
                    satellite.getMass(),
                    satellite.getDryMass(),
                    satellite.getVolume(),
                    satellite.getPower(),
                    satellite.getCommBand(),
                    satellite.getPayload(),
                    orbit,
                    satellite.getTechReadinessLevel(),
                    satellite.isGroundCommand(),
                    satellite.isSpare(),
                    satellite.getPropellantType(),
                    satellite.getStabilizationType()));
        }
        Constellation constellation=new Constellation("DELTA_HETEROGENEOUS", t, planes.size(),null, null,null,satellites, secondaryPayload);
        constellations.add(constellation);
    }

    /**
     * Adds a train constellation in the list of constellations inside the architecture.
     * @param startDate the simulation start date
     * @param altitude the altitude in km
     * @param nsat the number of satellites in the train constellation
     * @param trueLSTANref_str the Longitude Time of the Ascending Node of the first(reference) satellite
     *                         in "hh:mm:ss" format
     * @param satelliteInterval the duration of time between satellites in a train constellation in
     *                          ISO 8601 duration format
     * @param satellite the satellite in the constellation
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     * @param epoch the epoch in 8601 format (should match with start date)
     * @param eccentricity the eccentricity of all the orbits
     */
    public void addTrain(AbsoluteDate startDate, double altitude, int nsat,  String trueLSTANref_str, String satelliteInterval, Satellite satellite, Boolean secondaryPayload, String epoch, double eccentricity){
        ArrayList<Orbit> listOrbits = new ArrayList<>(nsat);
        double inc = Utilities.incSSO(altitude*1000,eccentricity);
        double trueLSTANref = Utilities.hhmmssToSeconds(trueLSTANref_str);
        double timeInterval_s = Utilities.DurationToSeconds(satelliteInterval);
        double JD_UTC = Utilities.dateToJD( startDate.getYear(),startDate.getMonth(),startDate.getDay() + (startDate.getHour() / 24.0) + (startDate.getMinute() / (24.0*60.0)) + (startDate.getSecond() / (24.0*60*60.0)));
        double r_km = Utilities.EARTH_RADIUS_KM + altitude;
        double Tsat_s = 2*FastMath.PI*FastMath.sqrt(r_km*r_km*r_km*1e9/Utilities.GRAV_CONSTANT);
        double true_lstan_s;
        double ta_deg;
        for (int i=0; i<nsat; i++){
            true_lstan_s = trueLSTANref + i*timeInterval_s;
            double RAAN = Utilities.trueLSTANToRAAN(true_lstan_s, JD_UTC);
            ta_deg = -1*i*timeInterval_s*360.0/Tsat_s;
            double TA =ta_deg%360;
            Orbit orbit = new Orbit("KEPLERIAN",altitude,
                    r_km,FastMath.toDegrees(inc),eccentricity,0.0, RAAN,TA, epoch,null);
            listOrbits.add(orbit);
        }

        List<Satellite> satellites = new ArrayList<>();
        for (Orbit orbit : listOrbits){
            satellites.add(new Satellite(satellite.getName(),
                    satellite.getAcronym(),
                    satellite.getAgency(),
                    satellite.getMass(),
                    satellite.getDryMass(),
                    satellite.getVolume(),
                    satellite.getPower(),
                    satellite.getCommBand(),
                    satellite.getPayload(),
                    orbit,
                    satellite.getTechReadinessLevel(),
                    satellite.isGroundCommand(),
                    satellite.isSpare(),
                    satellite.getPropellantType(),
                    satellite.getStabilizationType()));
        }
        Constellation constellation=new Constellation("TRAIN", nsat,null,null, null,satelliteInterval,satellites, secondaryPayload);
        constellations.add(constellation);
    }

    /**
     * Adds an ad-hoc constellation in the list of constellations inside the architecture.
     * @param nsat the number of satellites
     * @param satellite the satellite in the constellation
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     * @param epoch the epoch in 8601 format. TODO: maybe read epoch from database
     */
    public void addAdHoc(int nsat, Satellite satellite, Boolean secondaryPayload, String epoch) {
        ArrayList<Integer> randomIntegers = Utilities.uniqueRandomIntegers(nsat, planetDatabase.size(), false);
        ArrayList<Orbit> listOrbits = new ArrayList<>(nsat);
        for (Integer i : randomIntegers){
            double altitude = planetDatabase.get(i).getAltitude();
            double r_km = Utilities.EARTH_RADIUS_KM + altitude;
            double inc = planetDatabase.get(i).getI();
            double ECC = planetDatabase.get(i).getE();
            double PA = planetDatabase.get(i).getPerigeeArgument();
            double RAAN = planetDatabase.get(i).getRaan();
            double TA = planetDatabase.get(i).getMeanAnomaly();
            Orbit orbit = new Orbit("KEPLERIAN",altitude,
                    r_km,inc,ECC,PA, RAAN,TA, epoch,null);
            listOrbits.add(orbit);
        }
        List<Satellite> satellites = new ArrayList<>();
        for (Orbit orbit : listOrbits){
            satellites.add(new Satellite(satellite.getName(),
                    satellite.getAcronym(),
                    satellite.getAgency(),
                    satellite.getMass(),
                    satellite.getDryMass(),
                    satellite.getVolume(),
                    satellite.getPower(),
                    satellite.getCommBand(),
                    satellite.getPayload(),
                    orbit,
                    satellite.getTechReadinessLevel(),
                    satellite.isGroundCommand(),
                    satellite.isSpare(),
                    satellite.getPropellantType(),
                    satellite.getStabilizationType()));
        }
        Constellation constellation=new Constellation("AD_HOC", nsat,null,null, null,null,satellites,secondaryPayload);
        constellations.add(constellation);

    }

    /**
     * Adds a string of pearls constellation in the list of constellations inside the architecture.
     * @param semimajoraxis the semi-major axis in km
     * @param inc the inclination in deg
     * @param nsat  the number of satellites
     * @param satelliteInterval the duration of time between satellites in a train constellation in
     *                          ISO 8601 duration format
     * @param satellite the satellite
     * @param secondaryPayload true if the constellation uses secondary payloads and false otherwise (or null if not specified)
     * @param epoch the epoch in ISO 8601 format
     * @param eccentricity the eccentricity of all the orbits
     */
    public void addStringOfPearls(double semimajoraxis, double inc, int nsat, double raan, String satelliteInterval, Satellite satellite, Boolean secondaryPayload, String epoch, double eccentricity) {
        double orbitPeriod = 2*FastMath.PI*FastMath.sqrt(semimajoraxis*semimajoraxis*semimajoraxis*1e9/Utilities.GRAV_CONSTANT);
        double timeInterval = Utilities.DurationToSeconds(satelliteInterval);
        double deltaMeanAnomaly = 360.0*timeInterval/orbitPeriod;
        double refMeanAnomaly = 0.0;

        List<Orbit> listOrbits = new ArrayList<>();
        for (int satNum = 0; satNum < nsat; satNum++) {
            Orbit orbit = new Orbit("KEPLERIAN",null,
                    semimajoraxis,inc,eccentricity,0,
                    raan, refMeanAnomaly + satNum * deltaMeanAnomaly, epoch, null);
            listOrbits.add(orbit);
        }

        List<Satellite> satellites = new ArrayList<>();
        for (Orbit orbit : listOrbits){
            satellites.add(new Satellite(satellite.getName(),
                    satellite.getAcronym(),
                    satellite.getAgency(),
                    satellite.getMass(),
                    satellite.getDryMass(),
                    satellite.getVolume(),
                    satellite.getPower(),
                    satellite.getCommBand(),
                    satellite.getPayload(),
                    orbit,
                    satellite.getTechReadinessLevel(),
                    satellite.isGroundCommand(),
                    satellite.isSpare(),
                    satellite.getPropellantType(),
                    satellite.getStabilizationType()));
        }
        //Type is kept as DELTA_HOMOGENEOUS so that the TAT-C tool works
        Constellation constellation=new Constellation("DELTA_HOMOGENEOUS", nsat, 1, null, null,null,satellites,secondaryPayload);
        constellations.add(constellation);
    }

    public void addGeneral(Collection<SatelliteVariable> satelliteVariables, Satellite satSpecification, String epoch) {
        List<Orbit> listOrbits = new ArrayList<>();
        for (SatelliteVariable sat : satelliteVariables) {
                Orbit orbit = new Orbit("KEPLERIAN",null,
                        sat.getSma()/1000,FastMath.toDegrees(sat.getInc()),0.0,FastMath.toDegrees(sat.getArgPer()),
                        FastMath.toDegrees(sat.getRaan()), FastMath.toDegrees(sat.getTrueAnomaly()), epoch, null);
                listOrbits.add(orbit);
        }

        List<Satellite> satellites = new ArrayList<>();
        for (Orbit orbit : listOrbits){
            satellites.add(new Satellite(satSpecification.getName(),
                    satSpecification.getAcronym(),
                    satSpecification.getAgency(),
                    satSpecification.getMass(),
                    satSpecification.getDryMass(),
                    satSpecification.getVolume(),
                    satSpecification.getPower(),
                    satSpecification.getCommBand(),
                    satSpecification.getPayload(),
                    orbit,
                    satSpecification.getTechReadinessLevel(),
                    satSpecification.isGroundCommand(),
                    satSpecification.isSpare(),
                    satSpecification.getPropellantType(),
                    satSpecification.getStabilizationType()));
        }
        Constellation constellation=new Constellation("DELTA_HOMOGENEOUS", satelliteVariables.size(), null,null, null,null,satellites, null);
        constellations.add(constellation);
    }

    /**
     * Sets the ground network
     * @param groundNetwork the network to set
     */
    public void addGroundNetwork(GroundNetwork groundNetwork){
        this.groundNetwork=groundNetwork;
    }

    /**
     * Gets the ground network
     * @return the ground network
     */
    public GroundNetwork getGroundNetwork() {
        return groundNetwork;
    }

    /**
     * Gets the list of constellations inside this architecture
     * @return the list of constellations inside this architecture
     */
    public List<Constellation> getConstellations() {
        return constellations;
    }

    @Override
    public File toJSON(int counter) {
        List<GroundNetwork> groundNetworks = new ArrayList<>();
        int counterGN = 0;
        for (GroundStation groundStation : this.groundNetwork.getGroundStations()){
            groundStation.set_id("gs-"+Integer.toString(counterGN));
            counterGN++;
        }
        groundNetworks.add(this.groundNetwork);
        int counterCons = 0;
        int counterSat = 0;
        for (Constellation cons : this.constellations){
            cons.set_id("con-"+Integer.toString(counterCons));
            for (Satellite sat : cons.getSatellites()){
                sat.set_id("sat-"+Integer.toString(counterSat));
                counterSat++;
            }
            counterCons++;
        }
        Architecture arch =new Architecture("arch-"+Integer.toString(counter),constellations, groundNetworks);
        File mainPath = new File(System.getProperty("tatc.output"));
        File archPatch = new File(mainPath,"arch-"+Integer.toString(counter));
        archPatch.mkdirs();
        File file = new File (archPatch,"arch.json");
        JSONIO.writeJSON(file,arch);
//        try {
//            JSONIO.replaceTypeFieldUnderscore(file);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return file;
    }

}
