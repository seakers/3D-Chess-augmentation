package tatc.architecture;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;
import org.json.JSONArray;
import org.json.JSONObject;
import org.orekit.estimation.measurements.GroundStation;

import tatc.ResultIO;
import tatc.architecture.specifications.Agency;
import tatc.architecture.specifications.Architecture;
import tatc.architecture.specifications.Constellation;
import tatc.architecture.specifications.FieldOfView;
import tatc.architecture.specifications.GroundNetwork;
import tatc.architecture.specifications.Instrument;
import tatc.architecture.specifications.Orbit;
import tatc.architecture.specifications.Orientation;
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

    public JSONObject addHomogeneousWalker(JSONObject constJson, Map<String, Object> archParameters) {
        // Create a deep copy of constJson to avoid modifying the original
        JSONObject updatedConstellation = new JSONObject(constJson.toString());
    
        // Update the constellation JSON with the parameters from archParameters
        updateJsonWithArchParameters(updatedConstellation, archParameters);
        return updatedConstellation;
    
    }

    private void updateJsonWithArchParameters(Object jsonObject, Map<String, Object> archParameters) {
        if (jsonObject instanceof JSONObject) {
            JSONObject jsonObj = (JSONObject) jsonObject;
    
            for (String key : jsonObj.keySet()) {
                Object value = jsonObj.get(key);
    
                // If the key exists in archParameters, update the value
                if (archParameters.containsKey(key)) {
                    Object archValue = archParameters.get(key);
    
                    // Handle nested JSONObjects and JSONArrays
                    if (value instanceof JSONObject && archValue instanceof Map) {
                        // Recursively update nested JSONObject
                        updateJsonWithArchParameters(value, (Map<String, Object>) archValue);
                    } else if (value instanceof JSONArray && archValue instanceof List) {
                        // Update JSONArray with the list from archParameters
                        JSONArray updatedArray = new JSONArray((List<?>) archValue);
                        jsonObj.put(key, updatedArray);
                    } else {
                        // Update the value directly
                        jsonObj.put(key, archValue);
                    }
                } else {
                    // If the value is a JSONObject or JSONArray, recurse into it
                    if (value instanceof JSONObject || value instanceof JSONArray) {
                        updateJsonWithArchParameters(value, archParameters);
                    }
                }
            }
        } else if (jsonObject instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) jsonObject;
    
            for (int i = 0; i < jsonArray.length(); i++) {
                Object element = jsonArray.get(i);
    
                // Recurse into each element of the array
                if (element instanceof JSONObject || element instanceof JSONArray) {
                    updateJsonWithArchParameters(element, archParameters);
                }
            }
        }
    }
    
    
    public void addHomogeneousWalkerOld(JSONObject constJson, Map<String, Object> archParameters) {
        // Extract and override constellation parameters
        String constellationType = constJson.optString("constellationType", "DELTA_HOMOGENEOUS");

        // Number of satellites (t)
        int t = getIntFromArchOrJson("numberSatellites", archParameters, constJson);

        // Number of planes (p)
        int p = getIntFromArchOrJson("numberPlanes", archParameters, constJson);

        // Relative spacing (f)
        int f = getIntFromArchOrJson("relativeSpacing", archParameters, constJson, 1);

        // Orbit parameters
        JSONObject orbitJson = constJson.getJSONObject("orbit");

        // Altitude
        double altitude = getDoubleFromArchOrJson("altitude", archParameters, orbitJson);

        // Inclination
        double inclination = getDoubleFromArchOrJson("inclination", archParameters, orbitJson);

        // Eccentricity
        double eccentricity = getDoubleFromArchOrJson("eccentricity", archParameters, orbitJson, 0.0);

        // Epoch
        String epoch = archParameters.containsKey("epoch") ? (String) archParameters.get("epoch") : "2020-01-01T00:00:00Z";
         // Compute semimajor axis
         double semimajoraxis = altitude + Utilities.EARTH_RADIUS_KM;

        // Walker parameters
        final int s = t / p; // Number of satellites per plane
        final double pu = 2 * FastMath.PI / t; // Pattern unit
        final double delAnom = pu * p; // In-plane spacing between satellites
        final double delRaan = pu * s; // Node spacing
        final double phasing = pu * f;
        final double refAnom = 0;
        final double refRaan = 0;
        final double refPerigee = 0;
        double delPerigee = eccentricity != 0.0 ? delRaan : 0.0;

        // Create list of orbits
        List<Orbit> listOrbits = new ArrayList<>();
        for (int planeNum = 0; planeNum < p; planeNum++) {
            for (int satNum = 0; satNum < s; satNum++) {
                Orbit orbit = new Orbit(
                    orbitJson.optString("orbitType", "CIRCULAR"),
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
            }}
        // Satellite
        JSONArray satellitesJsonArray = constJson.getJSONArray("satellites");
        if (satellitesJsonArray.length() == 0) {
            throw new IllegalArgumentException("No satellites specified in constellation JSON");
        }
        List<Satellite> sats = new ArrayList<Satellite>();
        for(Orbit orbit : listOrbits){
            JSONObject satelliteJson = satellitesJsonArray.getJSONObject(0);
            // Create Satellite object
            Satellite satellite = createSatelliteFromJson(satelliteJson, archParameters, orbit);
            sats.add(satellite);
        }
        
        // Secondary payload
        Boolean secondaryPayload = archParameters.containsKey("secondaryPayload") ? 
                                (Boolean) archParameters.get("secondaryPayload") : 
                                constJson.optBoolean("secondaryPayload", false);

        // Check for valid parameters
        if (t <= 0 || p <= 0) {
            throw new IllegalArgumentException(String.format("Expected t>0, p>0. Found t=%d and p=%d", t, p));
        }
        if ((t % p) != 0) {
            throw new IllegalArgumentException(String.format("Incompatible values for total number of satellites t=%d and number of planes p=%d. t must be divisible by p.", t, p));
        }
        // if (f < 0 || f > p - 1) {
        //     throw new IllegalArgumentException(String.format("Expected 0 <= f <= p-1. Found f=%d and p=%d.", f, p));
        // }
        if (archParameters.containsKey("payload")) {
        Object payloadObj = archParameters.get("payload");
        if (payloadObj instanceof Collection && ((Collection<?>) payloadObj).isEmpty()) {
            System.err.println("Payload is defined but empty. Constellation will not be added.");
            return; // Exit the method without adding the constellation
        } else if (payloadObj == null) {
            System.err.println("Payload is defined but null. Constellation will not be added.");
            return; // Exit the method without adding the constellation
        }
    }
        
        Constellation constellation=new Constellation("DELTA_HOMOGENEOUS", t, 1, null, listOrbits,null,sats,secondaryPayload);
        this.constellations.add(constellation);
    }

    public List<Constellation> getConstellations(){
        return this.constellations;
    }

    private int getIntFromArchOrJson(String key, Map<String, Object> archParameters, JSONObject jsonObject) {
        return getIntFromArchOrJson(key, archParameters, jsonObject, null);
    }
    
    private int getIntFromArchOrJson(String key, Map<String, Object> archParameters, JSONObject jsonObject, Integer defaultValue) {
        if (archParameters.containsKey(key)) {
            return ((Number) archParameters.get(key)).intValue();
        } else if (jsonObject.has(key)) {
            Object value = jsonObject.get(key);
            if (value instanceof Number) {
                return ((Number) value).intValue();
            } else if (value instanceof JSONArray) {
                return ((JSONArray) value).getInt(0);
            }
        }
        if (defaultValue != null) {
            return defaultValue;
        } else {
            throw new IllegalArgumentException("Missing required integer parameter: " + key);
        }
    }
    
    private double getDoubleFromArchOrJson(String key, Map<String, Object> archParameters, JSONObject jsonObject) {
        return getDoubleFromArchOrJson(key, archParameters, jsonObject, null);
    }
    
    private double getDoubleFromArchOrJson(String key, Map<String, Object> archParameters, JSONObject jsonObject, Double defaultValue) {
        if (archParameters.containsKey(key)) {
            return ((Number) archParameters.get(key)).doubleValue();
        } else if (jsonObject.has(key)) {
            Object value = jsonObject.get(key);
            if (value instanceof Number) {
                return ((Number) value).doubleValue();
            } else if (value instanceof JSONArray) {
                return ((JSONArray) value).getDouble(0);
            }
        }
        if (defaultValue != null) {
            return defaultValue;
        } else {
            throw new IllegalArgumentException("Missing required double parameter: " + key);
        }
    }
    private Satellite createSatelliteFromJson(JSONObject satelliteJson, Map<String, Object> archParameters, Orbit orbit) {
        // Extract satellite parameters, applying overrides from archParameters
        String name = satelliteJson.optString("name", "DefaultSatellite");
        String acronym = satelliteJson.optString("acronym", "SAT");
        double mass = getDoubleFromArchOrJson("mass", archParameters, satelliteJson, 0.0);
        double dryMass = getDoubleFromArchOrJson("dryMass", archParameters, satelliteJson, 0.0);
        double volume = getDoubleFromArchOrJson("volume", archParameters, satelliteJson, 0.0);
        double power = getDoubleFromArchOrJson("power", archParameters, satelliteJson, 0.0);
    
        // CommBand
        JSONArray commBandJsonArray = satelliteJson.optJSONArray("commBand");
        List<String> commBand = new ArrayList<>();
        if (commBandJsonArray != null) {
            for (int i = 0; i < commBandJsonArray.length(); i++) {
                commBand.add(commBandJsonArray.getString(i));
            }
        }
    
        // Payload
        // JSONArray payloadJsonArray = satelliteJson.optJSONArray("payload");
        // List<Instrument> payload = new ArrayList<>();
        // if (payloadJsonArray != null) {
        //     for (int i = 0; i < payloadJsonArray.length(); i++) {
        //         JSONObject instrumentJson = payloadJsonArray.getJSONObject(i);
        //         Instrument instrument = createInstrumentFromJson(instrumentJson, archParameters);
        //         payload.add(instrument);
        //     }
        // }
        List<Instrument> payload = getPayloadFromArchOrJson(satelliteJson, archParameters);
        int techReadinessLevel = satelliteJson.optInt("techReadinessLevel", 0);
        boolean isGroundCommand = satelliteJson.optBoolean("isGroundCommand", false);
        boolean isSpare = satelliteJson.optBoolean("isSpare", false);
        String propellantType = satelliteJson.optString("propellantType", "");
        String stabilizationType = satelliteJson.optString("stabilizationType", "");
    
        // Create Satellite object
        Satellite satellite = new Satellite(
            name,
            acronym,
            null, // Agency
            mass,
            dryMass,
            volume,
            power,
            commBand,
            payload,
            orbit, // Orbit will be set later
            techReadinessLevel,
            isGroundCommand,
            isSpare,
            propellantType,
            stabilizationType
        );
    
        return satellite;
    }
    private List<Instrument> getPayloadFromArchOrJson(JSONObject satelliteJson, Map<String, Object> archParameters) {
        List<Instrument> payload = new ArrayList<>();
    
        // Check if 'payload' is defined in archParameters
        Object payloadObj = archParameters.get("payload");
        if (payloadObj != null) {
            if (payloadObj instanceof HashSet) {
            // Payload is a HashSet of instrument parameters
            HashSet<?> payloadSet = (HashSet<?>) payloadObj;
            for (Object instrumentObj : payloadSet) {
                if (instrumentObj instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> instrumentParams = (Map<String, Object>) instrumentObj;
                    // Create Instrument from parameters
                    Instrument instrument = new Instrument(instrumentParams);
                    payload.add(instrument);
                } else if (instrumentObj instanceof JSONObject) {
                    JSONObject instrumentJson = (JSONObject) instrumentObj;
                    // Create Instrument from JSON
                    Instrument instrument = createInstrumentFromJson(instrumentJson, archParameters);
                    payload.add(instrument);
                } else {
                    // Handle other types if necessary
                    System.err.println("Unsupported instrument object type in archParameters payload.");
                }
            }
        } else {
            System.err.println("Invalid payload format in archParameters.");
        }
    } else {
        // Payload is not in archParameters; get it from satelliteJson
        JSONArray payloadJsonArray = satelliteJson.optJSONArray("payload");
        if (payloadJsonArray != null) {
            for (int i = 0; i < payloadJsonArray.length(); i++) {
                JSONObject instrumentJson = payloadJsonArray.getJSONObject(i);
                Instrument instrument = createInstrumentFromJson(instrumentJson, archParameters);
                payload.add(instrument);
            }
        } else {
            System.err.println("Payload is not defined in satelliteJson.");
        }
    }
    
        return payload;
    }
    
    
    private Instrument createInstrumentFromJson(JSONObject instrumentJson, Map<String, Object> archParameters) {
    // Create a map to hold instrument parameters
    Map<String, Object> instrumentParams = new HashMap<>();
    // Extract parameters from instrumentJson
    for (String key : instrumentJson.keySet()) {
        if (instrumentJson.has(key)) {
            Object value = instrumentJson.get(key);
            // Handle nested JSONObjects for complex types
            if (value instanceof JSONObject) {
                JSONObject jsonObj = (JSONObject) value;
                switch (key) {
                    case "agency":
                        // Assuming Agency has a constructor that accepts a JSONObject
                        Agency agency = createAgencyFromJson(jsonObj);
                        instrumentParams.put(key, agency);
                        break;
                    case "orientation":
                        Orientation orientation = createOrientationFromJson(jsonObj);
                        instrumentParams.put(key, orientation);
                        break;
                    case "fieldOfView":
                        FieldOfView fov = createFieldOfViewFromJson(jsonObj);
                        instrumentParams.put(key, fov);
                        break;
                    default:
                        instrumentParams.put(key, jsonObj.toMap());
                        break;
                }
            } else {
                instrumentParams.put(key, value);
            }
        }
    }

    

    // Apply overrides from archParameters
    for (String key : archParameters.keySet()) {
        if (instrumentParams.containsKey(key) || instrumentJson.keySet().contains(key)) {
            instrumentParams.put(key, archParameters.get(key));
        }
    }

    // Create the Instrument object using the instrumentParams map
    Instrument instrument = new Instrument(instrumentParams);

    // Return the constructed instrument
    return instrument;
}


private Agency createAgencyFromJson(JSONObject agencyJson) {
    String name = agencyJson.optString("name", "");
    String acronym = agencyJson.optString("acronym", "");
    String country = agencyJson.optString("country", "");

    return new Agency(name, acronym, country);
}

private Orientation createOrientationFromJson(JSONObject orientationJson) {
    String convention = orientationJson.optString("convention", "");
    double sideLookAngle = orientationJson.optDouble("sideLookAngle", 0.0);
    // Add other orientation parameters as needed

    return new Orientation(convention, sideLookAngle, sideLookAngle, sideLookAngle, sideLookAngle);
}

private FieldOfView createFieldOfViewFromJson(JSONObject fovJson) {
    String sensorGeometry = fovJson.optString("sensorGeometry", "");
    double fullConeAngle = fovJson.optDouble("fullConeAngle", 0.0);
    double alongTrackFieldOfView = fovJson.optDouble("alongTrackFieldOfView", 0.0);
    double crossTrackFieldOfView = fovJson.optDouble("crossTrackFieldOfView", 0.0);
    double fieldOfRegard = fovJson.optDouble("fieldOfRegard", 0.0);
    List<Double> customConeAnglesVector = null;
    List<Double> customClockAnglesVector = null;
    // Add other FOV parameters as needed

    return new FieldOfView(sensorGeometry, fullConeAngle,  alongTrackFieldOfView, crossTrackFieldOfView,customConeAnglesVector, customClockAnglesVector, fieldOfRegard);
}

    
    
public void addGroundNetwork(GroundNetwork groundNetwork){
    this.groundNetwork=groundNetwork;
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
