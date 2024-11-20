package tatc.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tatc.architecture.constellations.ConstellationParameters;
import tatc.architecture.constellations.HomogeneousWalkerParameters;
import tatc.architecture.specifications.Satellite;

public class EnumerationNew {
    public static ArrayList<ConstellationParameters> fullFactHomogeneousWalker(
            Map<String, List<Object>> parameters,
            double eccentricity,
            int maxNumberOfArchitecturesFF
    ) throws IllegalArgumentException {
        ArrayList<ConstellationParameters> constellations = new ArrayList<>();

        // Create the options map for cartesian product
        Map<String, List<Object>> optionsMap = new LinkedHashMap<>(parameters);

        // Generate all combinations
        List<Map<String, Object>> parameterCombinations = computeCartesianProduct(optionsMap);

        int archCounter = 0;

        for (Map<String, Object> params : parameterCombinations) {
            // Check for maximum number of architectures
            if (archCounter >= maxNumberOfArchitecturesFF) {
                System.out.println("Aborting... Too many architectures to run a full factorial enumeration. Consider using other search strategies.");
                System.exit(1);
            }

            // Parse parameters
            Double altitude = (Double) params.get("HomoAltitude");
            Object inclinationObj = params.get("HomoInclination");
            Double inclination;

            if ("SSO".equalsIgnoreCase(String.valueOf(inclinationObj))) {
                inclination = Math.toDegrees(Utilities.incSSO(altitude * 1000, eccentricity));
            } else if (inclinationObj instanceof Number) {
                inclination = ((Number) inclinationObj).doubleValue();
            } else {
                throw new IllegalArgumentException("Inclination not identified");
            }

            Integer t = (Integer) params.get("HomoNumberSatellites");
            Integer p = (Integer) params.get("HomoNumberPlanes");
            Integer f = (Integer) params.get("HomoRelativeSpacing");
            Satellite satellite = (Satellite) params.get("HomoSatellite");

            // Additional payload parameters
            Double payloadFocalLength = (Double) params.get("PayloadFocalLength");
            Integer payloadBitsPerPixel = (Integer) params.get("PayloadBitsPerPixel");
            Integer payloadNumDetectorsRows = (Integer) params.get("PayloadNumDetectorsRows");
            Double payloadApertureDia = (Double) params.get("PayloadApertureDia");

            // Create the ConstellationParameters object with all parameters
            HomogeneousWalkerParameters constellationParam = new HomogeneousWalkerParameters(
                    altitude,
                    inclination,
                    t,
                    p,
                    f,
                    satellite,
                    payloadFocalLength,
                    payloadBitsPerPixel,
                    payloadNumDetectorsRows,
                    payloadApertureDia
            );
            constellations.add(constellationParam);
            archCounter++;
        }

        return constellations;
    }

    // Compute the cartesian product of options map
    public static List<Map<String, Object>> computeCartesianProduct(Map<String, List<Object>> optionsMap) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        computeCartesianProductHelper(optionsMap, new LinkedHashMap<>(), combinations, new ArrayList<>(optionsMap.keySet()), 0);
        return combinations;
    }

    private static void computeCartesianProductHelper(
            Map<String, List<Object>> optionsMap,
            Map<String, Object> currentCombination,
            List<Map<String, Object>> combinations,
            List<String> keys,
            int index
    ) {
        if (index == keys.size()) {
            combinations.add(new LinkedHashMap<>(currentCombination));
            return;
        }
        String key = keys.get(index);
        List<Object> values = optionsMap.get(key);

        for (Object value : values) {
            currentCombination.put(key, value);
            computeCartesianProductHelper(optionsMap, currentCombination, combinations, keys, index + 1);
            currentCombination.remove(key);
        }
    }
    
}
