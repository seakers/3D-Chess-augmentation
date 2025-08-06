package tatc.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import tatc.architecture.constellations.ConstellationParameters;
import tatc.architecture.constellations.HomogeneousWalkerParameters;
import tatc.architecture.specifications.Satellite;

/**
 * New implementation of constellation enumeration utilities.
 * This class provides an alternative approach to generating full factorial designs
 * for homogeneous Walker constellations using a more flexible parameter mapping approach.
 * 
 * @author TSE Development Team
 */
public class EnumerationNew {
    
    /**
     * Enumerates all possible homogeneous Walker constellation parameters using a parameter map approach.
     * This method provides a more flexible way to specify constellation parameters compared to the original
     * Enumeration class.
     *
     * @param parameters Map containing parameter names and their possible values
     * @param eccentricity The eccentricity of the orbits
     * @param maxNumberOfArchitecturesFF Maximum number of architectures allowed
     * @return List of constellation parameters containing all possible homogeneous Walker constellations
     * @throws IllegalArgumentException if input parameters are invalid
     * @throws Enumeration.DesignSpaceTooLargeException if the design space contains too many architectures
     */
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
                throw new Enumeration.DesignSpaceTooLargeException(
                    "Too many architectures (" + archCounter + ") to run a full factorial enumeration. " +
                    "Consider using other search strategies."
                );
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

    /**
     * Computes the cartesian product of all parameter combinations.
     * This method generates all possible combinations of the provided parameter values.
     *
     * @param optionsMap Map containing parameter names and their possible values
     * @return List of maps, where each map represents one parameter combination
     */
    public static List<Map<String, Object>> computeCartesianProduct(Map<String, List<Object>> optionsMap) {
        List<Map<String, Object>> combinations = new ArrayList<>();
        computeCartesianProductHelper(optionsMap, new LinkedHashMap<>(), combinations, new ArrayList<>(optionsMap.keySet()), 0);
        return combinations;
    }

    /**
     * Helper method for computing the cartesian product recursively.
     *
     * @param optionsMap Map containing parameter names and their possible values
     * @param currentCombination Current combination being built
     * @param combinations List to store all generated combinations
     * @param keys List of parameter keys to process
     * @param index Current index in the keys list
     */
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
