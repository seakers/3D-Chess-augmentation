package tatc.architecture;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.orekit.estimation.measurements.GroundStation;

import tatc.ResultIO;
import tatc.architecture.specifications.Architecture;
import tatc.architecture.specifications.Constellation;
import tatc.architecture.specifications.GroundNetwork;
import tatc.architecture.specifications.Instrument;
import tatc.architecture.specifications.Orbit;
import tatc.architecture.specifications.Satellite;
import tatc.util.JSONIO;
import tatc.util.TLESatellite;
import tatc.util.Utilities;

public class ArchitectureCreatorNew implements ArchitectureMethods{

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
    public ArchitectureCreatorNew(){
        this.constellations = new ArrayList<>();
        this.groundNetwork = null;
    }
    public void addHomogeneousWalker(Map<String, Object> params) {
    // Extract fundamental parameters from params
    Double semimajoraxis = getRequiredDoubleParam(params, "semimajoraxis");
    Double inclination = getRequiredDoubleParam(params, "inclination");
    Integer t = getRequiredIntegerParam(params, "numberSatellites");
    Integer p = getRequiredIntegerParam(params, "numberPlanes");
    Integer f = getRequiredIntegerParam(params, "relativeSpacing");
    String epoch = getRequiredStringParam(params, "epoch");
    Double eccentricity = getOptionalDoubleParam(params, "eccentricity", 0.0); // Default to 0.0 if not provided
    Boolean secondaryPayload = getOptionalBooleanParam(params, "secondaryPayload", false); // Default to false
    Satellite satellite = getRequiredSatelliteParam(params, "satellite");

    // Checks for valid parameters
    if (t <= 0 || p <= 0) {
        throw new IllegalArgumentException(String.format("Expected t>0, p>0. Found t=%d and p=%d", t, p));
    }
    if ((t % p) != 0) {
        throw new IllegalArgumentException(
            String.format("Incompatible values for total number of satellites <t=%d> and number of planes <p=%d>. t must be divisible by p.", t, p)
        );
    }
    if (f < 0 || f > p - 1) {
        throw new IllegalArgumentException(
            String.format("Expected 0 <= f <= p-1. Found f = %d and p = %d.", f, p)
        );
    }

    // Calculate orbital parameters
    final int s = t / p; // Number of satellites per plane
    final double pu = 2 * FastMath.PI / t; // Pattern unit
    final double delAnom = pu * p; // In-plane spacing between satellites
    final double delRaan = pu * s; // Node spacing
    final double phasing = pu * f;
    final double refAnom = 0;
    final double refRaan = 0;
    final double refPerigee = 0;
    double delPerigee = 0;
    if (eccentricity != 0.0) {
        delPerigee = delRaan;
    }
    double altitude = semimajoraxis - Utilities.EARTH_RADIUS_KM;
    List<Orbit> listOrbits = new ArrayList<>();
    for (int planeNum = 0; planeNum < p; planeNum++) {
        for (int satNum = 0; satNum < s; satNum++) {
            Orbit orbit = new Orbit(
                "CUSTOM",
                altitude,
                semimajoraxis,
                inclination,
                eccentricity,
                FastMath.toDegrees(refPerigee + planeNum * delPerigee),
                FastMath.toDegrees(refRaan + planeNum * delRaan),
                FastMath.toDegrees((refAnom + satNum * delAnom + phasing * planeNum) % (2. * FastMath.PI)),
                epoch,
                null
            );
            listOrbits.add(orbit);
        }
    }

    // Update the satellite's payload and instruments based on params
    updateSatellitePayload(satellite, params);

    // Create satellites with updated payloads and orbits
    List<Satellite> satellites = new ArrayList<>();
    for (Orbit orbit : listOrbits) {
        Satellite sat = new Satellite(
            satellite.getName(),
            satellite.getAcronym(),
            satellite.getAgency(),
            satellite.getMass(),
            satellite.getDryMass(),
            satellite.getVolume(),
            satellite.getPower(),
            satellite.getCommBand(),
            satellite.getPayload(), // Use updated payload
            orbit,
            satellite.getTechReadinessLevel(),
            satellite.isGroundCommand(),
            satellite.isSpare(),
            satellite.getPropellantType(),
            satellite.getStabilizationType()
        );
        satellites.add(sat);
    }

    // Create and add the constellation
    Constellation constellation = new Constellation(
        "DELTA_HOMOGENEOUS",
        t,
        p,
        f,
        listOrbits,
        null,
        satellites,
        secondaryPayload
    );
    constellations.add(constellation);
}

// Helper methods to extract required and optional parameters
private Double getRequiredDoubleParam(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value instanceof Number) {
        return ((Number) value).doubleValue();
    } else {
        throw new IllegalArgumentException("Missing or invalid required parameter: " + key);
    }
}

private Integer getRequiredIntegerParam(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value instanceof Number) {
        return ((Number) value).intValue();
    } else {
        throw new IllegalArgumentException("Missing or invalid required parameter: " + key);
    }
}

private String getRequiredStringParam(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value instanceof String) {
        return (String) value;
    } else {
        throw new IllegalArgumentException("Missing or invalid required parameter: " + key);
    }
}

private Satellite getRequiredSatelliteParam(Map<String, Object> params, String key) {
    Object value = params.get(key);
    if (value instanceof Satellite) {
        return (Satellite) value;
    } else {
        throw new IllegalArgumentException("Missing or invalid required parameter: " + key);
    }
}

private Double getOptionalDoubleParam(Map<String, Object> params, String key, Double defaultValue) {
    Object value = params.get(key);
    if (value instanceof Number) {
        return ((Number) value).doubleValue();
    } else {
        return defaultValue;
    }
}

private Boolean getOptionalBooleanParam(Map<String, Object> params, String key, Boolean defaultValue) {
    Object value = params.get(key);
    if (value instanceof Boolean) {
        return (Boolean) value;
    } else {
        return defaultValue;
    }
}

// Method to update the satellite's payload and instruments based on params
private void updateSatellitePayload(Satellite satellite, Map<String, Object> params) {
    // Extract payload parameters
    if (params.containsKey("payload")) {
        Object payloadObj = params.get("payload");
        if (payloadObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> payloadParams = (Map<String, Object>) payloadObj;
            List<Instrument> payload = new ArrayList<>();

            // Handle instruments within the payload
            if (payloadParams.containsKey("instruments")) {
                Object instrumentsObj = payloadParams.get("instruments");
                if (instrumentsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> instrumentsList = (List<Map<String, Object>>) instrumentsObj;
                    for (Map<String, Object> instrumentParams : instrumentsList) {
                        Instrument instrument = new Instrument(instrumentParams);
                        payload.add(instrument);
                    }
                } else {
                    throw new IllegalArgumentException("Invalid instruments parameter in payload");
                }
            } else {
                // If no instruments are provided, you may choose to throw an error or proceed
                throw new IllegalArgumentException("No instruments provided in payload");
            }

            // Set the updated payload to the satellite
            satellite.setPayload(payload);
        } else {
            throw new IllegalArgumentException("Invalid payload parameter");
        }
    } else {
        // If payload is not provided, throw an error or set to default
        throw new IllegalArgumentException("Payload parameter is required");
    }
}

     @Override
    public File toJSON(int counter) {
        List<GroundNetwork> groundNetworks = new ArrayList<>();
        int counterGN = 0;
        for (tatc.architecture.specifications.GroundStation groundStation : this.groundNetwork.getGroundStations()){
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
