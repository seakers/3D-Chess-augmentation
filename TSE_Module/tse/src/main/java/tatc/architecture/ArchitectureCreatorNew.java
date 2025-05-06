package tatc.architecture;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.nio.file.Files;
import java.io.FileWriter;

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
import tatc.architecture.specifications.MissionConcept;
import tatc.architecture.specifications.Orbit;
import tatc.architecture.specifications.Orientation;
import tatc.architecture.specifications.Satellite;
import tatc.tradespaceiterator.ProblemProperties;
import tatc.util.JSONIO;
import tatc.util.TLESatellite;
import tatc.util.Utilities;
import java.time.*;
import tatc.util.OrbitalTimeUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
public class ArchitectureCreatorNew implements ArchitectureMethods{

    /**
     * List of constellations assigned to an architecture
     */
    private List<Constellation> constellations;

    /**
     * Ground network assigned to an architecture
     */
    private GroundNetwork groundNetwork;

    private ProblemProperties properties;

    /**
     * Static field to store the timestamped results path
     */
    private static String timestampedResultsPath = null;

    /**
     * Planet labs data base for ad-hoc constellations
     */
    private static final List<TLESatellite> planetDatabase = ResultIO.getPlanetLabsEphemerisDatabase();

    /**
     * Initializes an architecture creator
     */
    public ArchitectureCreatorNew(ProblemProperties properties){
        this.constellations = new ArrayList<>();
        this.groundNetwork = null;
        this.properties = properties;
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

        List<Orbit> listOrbits = getOrbitsFromConstJsonOrArchParams(constJson, archParameters);
    
        JSONArray satellitesJsonArray = constJson.getJSONArray("satellites");
        if (satellitesJsonArray.length() == 0) {
            throw new IllegalArgumentException("No satellites specified in constellation JSON");
        }
        JSONObject satelliteJson;
        List<Satellite> sats = new ArrayList<Satellite>();
        if (archParameters.containsKey("satellites")){
            Object satellitesObj = archParameters.get("satellites");

            satellitesJsonArray = new JSONArray((List<?>) satellitesObj);
        }
        else{
            satellitesJsonArray = constJson.getJSONArray("satellites");

        }
        for(Orbit orbit : listOrbits){
            for(int i = 0; i<satellitesJsonArray.length(); i++){
                JSONObject satelliJsonObject = satellitesJsonArray.getJSONObject(i);
                Satellite satellite = createSatelliteFromJson(satelliJsonObject, archParameters, orbit);
                sats.add(satellite);
            }

            // Create Satellite object

        }
        
        // Secondary payload
        Boolean secondaryPayload = archParameters.containsKey("secondaryPayload") ? 
                                (Boolean) archParameters.get("secondaryPayload") : 
                                constJson.optBoolean("secondaryPayload", false);

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
    public List<Orbit> getOrbitsFromConstJsonOrArchParams(JSONObject constJson, Map<String, Object> archParameters) {
        List<Orbit> orbits = new ArrayList<>();
    
        // Extract number of satellites
        int t = getIntFromArchOrJson("numberSatellites", archParameters, constJson);
        // Number of planes (p)
        int p = getIntFromArchOrJson("numberPlanes", archParameters, constJson);

        // Relative spacing (f)
        int f = getIntFromArchOrJson("relativeSpacing", archParameters, constJson, 1);
        String constellationType = constJson.optString("constellationType", "DELTA_HOMOGENEOUS");
    
        // Case 1: Orbit is a decision variable → Get it from archParameters
        if (constJson.has("orbit") && constJson.get("orbit") instanceof JSONArray) {
            if (!archParameters.containsKey("orbit")) {
                throw new IllegalArgumentException("Orbit decision variable is missing from archParameters.");
            }
    
            // Retrieve assigned orbit configurations from archParameters (always a JSONObject)
            JSONObject assignedOrbitsJson = (JSONObject) archParameters.get("orbit");
            
            // Add constellation parameters
            assignedOrbitsJson.put("numberPlanes", p);
            assignedOrbitsJson.put("relativeSpacing", f);
            assignedOrbitsJson.put("numberSatellites", t);

            // If there's a nested orbit object, flatten it by moving its parameters up
            if (assignedOrbitsJson.has("orbit") && assignedOrbitsJson.get("orbit") instanceof JSONObject) {
                JSONObject nestedOrbit = assignedOrbitsJson.getJSONObject("orbit");
                // Copy all parameters from nested orbit to the parent
                for (String key : nestedOrbit.keySet()) {
                    assignedOrbitsJson.put(key, nestedOrbit.get(key));
                }
                // Remove the nested orbit object
                assignedOrbitsJson.remove("orbit");
            }

            // Convert the assigned JSONObject into an Orbit list
            orbits = createOrbitFromJsonOrArchParams(assignedOrbitsJson, archParameters);
        }
        // Case 2: Orbit is explicitly defined in constJson (Fixed Orbit Case)
        else if (constJson.has("orbit") && constJson.get("orbit") instanceof JSONObject) {
            JSONObject orbitJson = constJson.getJSONObject("orbit");
            orbitJson.put("numberPlanes", p);
            orbitJson.put("relativeSpacing", f);
            orbitJson.put("numberSatellites", t);
            orbits = createOrbitFromJsonOrArchParams(orbitJson, archParameters);
        } else {
            throw new IllegalArgumentException("No valid orbit information found in constJson or archParameters.");
        }
    
        return orbits;
    }
    
    /**
     * Creates an orbit from JSON or archParameters.
     */
    private List<Orbit> createOrbitFromJsonOrArchParams(JSONObject orbitJson, Map<String, Object> archParameters) {
        String orbitType = getStringFromArchOrJson("orbitType", archParameters, orbitJson, "LEO");
        double altitude = getDoubleFromArchOrJson("altitude", archParameters, orbitJson);
        double inclination = getDoubleFromArchOrJson("inclination", archParameters, orbitJson);
        double eccentricity = getDoubleFromArchOrJson("eccentricity", archParameters, orbitJson, 0.0);
        String epoch;
        if (properties != null && 
            properties.getTradespaceSearch() != null && 
            properties.getTradespaceSearch().getMission() != null && 
            properties.getTradespaceSearch().getMission().getStart() != null) {
            epoch = properties.getTradespaceSearch().getMission().getStart();
            System.out.println("Epoch: " + epoch);
        } else {
            epoch = "2020-01-01T00:00:00Z"; // Default epoch if mission start is not available
            System.out.println("Warning: Using default epoch as mission start time is not available");
        }
        int p = orbitJson.getInt("numberPlanes");
        int f = orbitJson.getInt("relativeSpacing");
        int t = orbitJson.getInt("numberSatellites");
        double semimajorAxis = altitude + Utilities.EARTH_RADIUS_KM;
        
        // default: no LT constraint  → keep your old refRaan=0
        Double ltanHours = 0.0;

        // 1) explicit field in JSON wins
        if (orbitJson.has("localTimeAscendingNode")) {
            ltanHours = orbitJson.getDouble("localTimeAscendingNode");
        } else {
            // 2) otherwise look for keywords in orbitType
            String orbitTypeStr = orbitType.toUpperCase();
            if (orbitTypeStr.contains("SSO")) {
                if (orbitTypeStr.contains("DD")) {        // Dawn‑Dusk
                    ltanHours = 6.0;                      // ascending ≈ 06:00, descending ≈ 18:00
                } else if (orbitTypeStr.contains("PM")) { // Afternoon crossing
                    ltanHours = 13.5;                     // 13:30 LTAN, common for e.g. Aqua
                } else if (orbitTypeStr.contains("AM")) { // Morning crossing
                    ltanHours = 10.5;                     // 10:30 LTAN (MODIS/ Terra style)
                }
            }
        }
        System.out.println("LTAN: " + ltanHours);

        /*  ────── compute refRaan ────── */
        double refRaanDeg = 0.0;
        if (ltanHours != null) {
            ZonedDateTime epochUtc = ZonedDateTime.parse(epoch);
            refRaanDeg = OrbitalTimeUtils.raanFromLTAN(ltanHours, epochUtc);
        }
        String localSolarTimeAscendingNode = ltanHours != null ? 
            OrbitalTimeUtils.ltanToIsoTime(ltanHours) : 
            "00:00:00"; // Default value when no LTAN is specified

        /* keep the rest exactly as you had, but replace refRaan */
        double refRaan = FastMath.toRadians(refRaanDeg);
        // Walker parameters
        int s = t / p; // Number of satellites per plane
        if (s == 0){
            p=t;
            s=1;
        }
        final double pu = 2 * FastMath.PI / t; // Pattern unit
        final double delAnom = pu * p; // In-plane spacing between satellites
        final double delRaan = pu * s; // Node spacing
        final double phasing = pu * f;
        final double refAnom = 0;
        final double refPerigee = 0;
        double delPerigee = eccentricity != 0.0 ? delRaan : 0.0;

        // Create list of orbits
        List<Orbit> listOrbits = new ArrayList<>();
        for (int planeNum = 0; planeNum < p; planeNum++) {
            for (int satNum = 0; satNum < s; satNum++) {
                Orbit orbit = new Orbit(
                    orbitType,
                    altitude,
                    semimajorAxis,
                    inclination,
                    eccentricity,
                    FastMath.toDegrees(refPerigee + planeNum * delPerigee),
                    FastMath.toDegrees(refRaan + planeNum * delRaan),
                    FastMath.toDegrees((refAnom + satNum * delAnom + phasing * planeNum) % (2. * FastMath.PI)),
                    epoch,
                    localSolarTimeAscendingNode
                );
                listOrbits.add(orbit);
            }}
    
        return listOrbits;
    }
    
    private String getStringFromArchOrJson(String key, Map<String, Object> archParameters, JSONObject jsonObject, String defaultValue) {
        if (archParameters.containsKey(key)) {
            return (String) archParameters.get(key);
        } else if (jsonObject.has(key)) {
            return jsonObject.getString(key);
        } else {
            return defaultValue;
        }
    }
    
    
    private double getDoubleFromArchOrJson(String key, Map<String, Object> archParameters, JSONObject jsonObject) {
        return getDoubleFromArchOrJson(key, archParameters, jsonObject, 400.0);
    }
    
    private double getDoubleFromArchOrJson(String key, Map<String, Object> archParameters, JSONObject jsonObject, Double defaultValue) {
        if(jsonObject.has("orbit")){
            jsonObject = jsonObject.getJSONObject("orbit");
        }
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
    public Satellite createSatelliteFromJson(JSONObject satelliteJson, Map<String, Object> archParameters, Orbit orbit) {
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
    
        // Add orbit information to archParameters for instrument calculations
        Map<String, Object> instrumentParams = new HashMap<>(archParameters);
        instrumentParams.put("height", orbit.getAltitude());
        instrumentParams.put("inclination", orbit.getInclination());
        
        // Get payload with orbit information
        List<Instrument> payload = getPayloadFromArchOrJson(satelliteJson, instrumentParams);
        
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
    public static HashMap<String, Object> jsonToHashMap(JSONObject jsonObj) {
        HashMap<String, Object> map = new HashMap<>();
        Iterator<String> keys = jsonObj.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            Object value = jsonObj.get(key);
            map.put(key, value);
        }
        return map;
    }
    public List<Instrument> getPayloadFromArchOrJson(JSONObject satelliteJson, Map<String, Object> archParameters) {
        List<Instrument> payload = new ArrayList<>();
        
        // Check if 'payload' is defined in archParameters
        Object payloadObj = archParameters.get("payload");
    
        if (payloadObj != null) {
            // Convert payloadObj to a Collection of instrument objects
            Collection<?> instrumentCollection = null;
            if (payloadObj instanceof JSONObject) {
                // Convert JSONObject to a HashMap and then get its values
                HashMap<String, Object> payloadMap = jsonToHashMap((JSONObject) payloadObj);
                instrumentCollection = payloadMap.values();
            } else if (payloadObj instanceof HashMap) {
                instrumentCollection = ((HashMap<?, ?>) payloadObj).values();
            } else if (payloadObj instanceof Collection) {
                instrumentCollection = (Collection<?>) payloadObj;
            } else {
                System.err.println("Unsupported payload object type in archParameters: " + payloadObj.getClass().getName());
            }
            
            if (instrumentCollection != null) {
                for (Object payload_ : instrumentCollection) {
                    if(payload_ instanceof JSONObject){
                        // Create a new map with orbit information for each instrument
                        Map<String, Object> instrumentParams = new HashMap<>(archParameters);
                        instrumentParams.put("height", archParameters.get("height"));
                        instrumentParams.put("inclination", archParameters.get("inclination"));
                        
                        Instrument instrument_i = createInstrumentFromJson((JSONObject) payload_, instrumentParams);
                        payload.add(instrument_i);
                    }
                    else if (payload_ instanceof ArrayList) {
                        for(Object instrument : (ArrayList) payload_){
                            if(instrument instanceof JSONObject){
                                // Create a new map with orbit information for each instrument
                                Map<String, Object> instrumentParams = new HashMap<>(archParameters);
                                instrumentParams.put("height", archParameters.get("height"));
                                instrumentParams.put("inclination", archParameters.get("inclination"));
                                
                                // Create Instrument from JSON object
                                Instrument instrument_i = createInstrumentFromJson((JSONObject) instrument, instrumentParams);
                                payload.add(instrument_i);
                            }
                        }
                    } else if (payload_ instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> instrumentParams = new HashMap<>((Map<String, Object>) payload_);
                        instrumentParams.put("height", archParameters.get("height"));
                        instrumentParams.put("inclination", archParameters.get("inclination"));
                        
                        // Create Instrument from parameter map
                        Instrument instrument = new Instrument(instrumentParams);
                        payload.add(instrument);
                    } else {
                        System.err.println("Unsupported instrument object type in payload: " + payload_.getClass().getName());
                    }
                }
            } else {
                System.err.println("Invalid payload format in archParameters.");
            }
        } else {
            // Fallback: get payload from satelliteJson
            JSONArray payloadJsonArray = satelliteJson.optJSONArray("payload");
            if (payloadJsonArray != null) {
                for (int i = 0; i < payloadJsonArray.length(); i++) {
                    JSONObject instrumentJson = payloadJsonArray.getJSONObject(i);
                    // Create a new map with orbit information for each instrument
                    Map<String, Object> instrumentParams = new HashMap<>(archParameters);
                    instrumentParams.put("height", archParameters.get("height"));
                    instrumentParams.put("inclination", archParameters.get("inclination"));
                    
                    Instrument instrument = createInstrumentFromJson(instrumentJson, instrumentParams);
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
                        FieldOfView fov = createFieldOfViewFromJson(jsonObj, archParameters);
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
        }else if (key.equals("height") || key.equals("inclination")) {
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

private FieldOfView createFieldOfViewFromJson(JSONObject fovJson, Map<String, Object> archParameters) {
    String sensorGeometry = fovJson.optString("sensorGeometry", "");
    double fullConeAngle = getDoubleFromArchOrJson("fullConeAngle", archParameters, fovJson);
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

        // Get mission information from ProblemProperties
        MissionConcept mission = null;
        if (properties != null && properties.getTradespaceSearch() != null) {
            mission = properties.getTradespaceSearch().getMission();
            System.out.println("Found mission: " + (mission != null ? "yes" : "no"));
            if (mission != null) {
                System.out.println("Mission start: " + mission.getStart());
                System.out.println("Mission duration: " + mission.getDuration());
            }
        }

        // Create architecture with mission info
        Architecture arch = new Architecture("arch-"+Integer.toString(counter), constellations, groundNetworks);
        
        // Add mission info to the architecture JSON
        if (mission != null) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String archJsonStr = gson.toJson(arch);
            JSONObject archJson = new JSONObject(archJsonStr);
            
            // Create mission JSON object
            JSONObject missionJson = new JSONObject();
            missionJson.put("start", mission.getStart());
            missionJson.put("duration", mission.getDuration());
            archJson.put("mission", missionJson);
            
            // Convert back to Architecture object
            arch = gson.fromJson(archJson.toString(), Architecture.class);
            System.out.println("Mission info added to architecture");
        }

        // Get the project root from system property
        String projectRoot = System.getProperty("tatc.root");
        if (projectRoot == null) {
            projectRoot = System.getProperty("user.dir");
        }
        System.out.println("Project root: " + projectRoot);
        
        // Create timestamped results path only if it hasn't been created yet
        if (timestampedResultsPath == null) {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File mainPath = new File(projectRoot, "TSE_Module/tse/results");
            File timestampedPath = new File(mainPath, "results_" + timestamp);
            timestampedResultsPath = timestampedPath.getAbsolutePath();
            System.out.println("Created new results directory: " + timestampedResultsPath);
            System.out.println("Results directory exists: " + timestampedPath.exists());
            
            // Set the output directory for summary.csv
            System.setProperty("tatc.output", timestampedResultsPath);
        }
        
        // Create architecture folder
        File archPatch = new File(timestampedResultsPath, "arch-" + Integer.toString(counter));
        System.out.println("Architecture directory: " + archPatch.getAbsolutePath());
        boolean dirCreated = archPatch.mkdirs();
        System.out.println("Directory created: " + dirCreated);
        
        File file = new File(archPatch, "arch.json");
        System.out.println("JSON file path: " + file.getAbsolutePath());
        System.out.println("JSON file exists before write: " + file.exists());
        
        // Write the architecture with mission info
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String archJsonStr = gson.toJson(arch);
        JSONObject archJson = new JSONObject(archJsonStr);
        
        if (mission != null) {
            JSONObject missionJson = new JSONObject();
            missionJson.put("start", mission.getStart());
            missionJson.put("duration", mission.getDuration());
            archJson.put("mission", missionJson);
        }
        
        try (FileWriter writer = new FileWriter(file)) {
            writer.write(archJson.toString(4)); // Pretty print with 4 spaces
            System.out.println("JSON write success");
        } catch (IOException e) {
            System.out.println("Error writing JSON file: " + e.getMessage());
            return null;
        }
        
        System.out.println("JSON file exists after write: " + file.exists());
        
        if (file.exists()) {
            System.out.println("File size: " + file.length() + " bytes");
        }
        
        return file;
    }
}
