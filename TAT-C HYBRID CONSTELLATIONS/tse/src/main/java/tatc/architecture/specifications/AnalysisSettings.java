package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Configuration options specific to TAT-C outputs, search and module parameters.
 */
public class AnalysisSettings implements Serializable {

    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="AnalysisSettings";
    /**
     * Boolean to include propulsion
     */
    private final boolean includePropulsion;
    /**
     * Configuration options specific to TAT-C outputs
     */
    private final Outputs outputs;
    /**
     * Search strategy (FF, MOEA, AOS or KDO)
     */
    private final String searchStrategy;
    /**
     * Configuration options for the different parameters of the genetic algorithm
     */
    private final SearchParameters searchParameters;
    /**
     * Boolean to use either the instrument module proxy (true) or the actual module (false) when evaluating an architecture
     */
    private final boolean proxyInstrument;
    /**
     * Boolean to use either the cost module proxy (true) or the actual module (false) when evaluating an architecture
     */
    private final boolean proxyCostRisk;
    /**
     * Boolean to use either the launch module proxy (true) or the actual module (false) when evaluating an architecture
     */
    private final boolean proxyLaunch;
    /**
     * Boolean to use either the orbits module proxy (true) or the actual module (false) when evaluating an architecture
     */
    private final boolean proxyOrbits;
    /**
     * Boolean to use either the value module proxy (true) or the actual module (false) when evaluating an architecture
     */
    private final boolean proxyValue;
    /**
     * Boolean to use either the maintenance module proxy (true) or the actual module (false) when evaluating an architecture
     */
    private final boolean proxyMaintenance;
    /**
     * Boolean to use a cache to improve computational performance
     */
    private final boolean useCache;
    /**
     * Maximum cache size in gigabytes
     */
    private final double maxCacheSize;

    /**
     * Toggle to either override (True) or append (False) to the launch vehicle database (default: False).
     */
    private final boolean overrideLaunchDatabase;

    /**
     * Minimum launch reliability (probability between 0.0 and 1.0) required for launch vehicle selection (default: 0.0).
     */
    private final double minLaunchReliability;
    /**
     * Maximum number of grid points to generate for coverage analysis
     */
    private final double maxGridSize;
    /**
     * Constructs an analysis settings object
     * @param includePropulsion the boolean to include propulsion or not
     * @param outputs the outputs configuration options
     * @param searchStrategy the search strategy (FF, MOEA, AOS or KDO)
     * @param searchParameters the search parameters for the genetic algorithm
     * @param proxyInstrument the boolean to use the instrument module proxy (true) or the actual module (false)
     * @param proxyCostRisk the boolean to use the cost module proxy (true) or the actual module (false)
     * @param proxyLaunch the boolean to use the launch module proxy (true) or the actual module (false)
     * @param proxyOrbits the boolean to use the orbits module proxy (true) or the actual module (false)
     * @param proxyValue the boolean to use the value module proxy (true) or the actual module (false)
     * @param proxyMaintenance the boolean to use the maintenance module proxy (true) or the actual module (false)
     * @param useCache the boolean to use a cache to improve computational performance
     * @param maxCacheSize the cache maximum size in gigabytes
     * @param overrideLaunchDatabase the boolean to toggle to either override (True) or append (False) to the launch vehicle database
     * @param minLaunchReliability the minimum launch reliability (probability between 0.0 and 1.0)
     * @param maxGridSize the maximum number of grid points to generate for coverage analysis
     */
    public AnalysisSettings(boolean includePropulsion, Outputs outputs, String searchStrategy, SearchParameters searchParameters, boolean proxyInstrument, boolean proxyCostRisk, boolean proxyLaunch, boolean proxyOrbits, boolean proxyValue, boolean proxyMaintenance, boolean useCache, double maxCacheSize, boolean overrideLaunchDatabase, double minLaunchReliability, double maxGridSize) {
        this.includePropulsion = includePropulsion;
        this.outputs = outputs;
        this.searchStrategy = searchStrategy;
        this.searchParameters = searchParameters;
        this.proxyInstrument = proxyInstrument;
        this.proxyCostRisk = proxyCostRisk;
        this.proxyLaunch = proxyLaunch;
        this.proxyOrbits = proxyOrbits;
        this.proxyValue = proxyValue;
        this.proxyMaintenance = proxyMaintenance;
        this.useCache = useCache;
        this.maxCacheSize = maxCacheSize;
        this.overrideLaunchDatabase = overrideLaunchDatabase;
        this.minLaunchReliability = minLaunchReliability;
        this.maxGridSize = maxGridSize;
    }

    /**
     * Returns whether propulsion is included or not
     * @return true if propulsion is included and false otherwise
     */
    public boolean isIncludePropulsion() {
        return includePropulsion;
    }

    /**
     * Gets the configuration options specific to TAT-C outputs
     * @return the configuration options specific to TAT-C outputs
     */
    public Outputs getOutputs() {
        return outputs;
    }


    /**
     * Gets the search strategy (FF, MOEA, AOS or KDO)
     * @return the search strategy
     */
    public String getSearchStrategy() {
        return searchStrategy;
    }

    /**
     * Gets the configuration options for the different parameters of the genetic algorithm
     * @return the configuration options for the different parameters of the genetic algorithm
     */
    public SearchParameters getSearchParameters() {
        return searchParameters;
    }

    /**
     * Returns whether the instrument proxy or the actual module is used
     * @return true if the instrument module proxy is used and false otherwise
     */
    public boolean isProxyInstrument() {
        return proxyInstrument;
    }

    /**
     * Returns whether the cost proxy or the actual module is used
     * @return true if the cost module proxy is used and false otherwise
     */
    public boolean isProxyCostRisk() {
        return proxyCostRisk;
    }

    /**
     * Returns whether the launch proxy or the actual module is used
     * @return true if the launch module proxy is used and false otherwise
     */
    public boolean isProxyLaunch() {
        return proxyLaunch;
    }

    /**
     * Returns whether the orbits proxy or the actual module is used
     * @return true if the orbits module proxy is used and false otherwise
     */
    public boolean isProxyOrbits() {
        return proxyOrbits;
    }

    /**
     * Returns whether the value proxy or the actual module is used
     * @return true if the value module proxy is used and false otherwise
     */
    public boolean isProxyValue() {
        return proxyValue;
    }

    /**
     * Returns whether the maintenance proxy or the actual module is used
     * @return true if the maintenance module proxy is used and false otherwise
     */
    public boolean isProxyMaintenance() {
        return proxyMaintenance;
    }

    /**
     * Returns whether we either override (True) or append (False) to the launch vehicle database
     * @return true if override and false otherwise
     */
    public boolean isOverrideLaunchDatabase() {
        return overrideLaunchDatabase;
    }

    /**
     * Gets the minimum launch reliability (probability between 0.0 and 1.0)
     * @return the minimum launch reliability (probability between 0.0 and 1.0)
     */
    public double getMinLaunchReliability() {
        return minLaunchReliability;
    }

    /**
     * Gets the boolean to use a cache to improve computational performance
     * @return the boolean to use a cache to improve computational performance
     */
    public boolean isUseCache() {
        return useCache;
    }

    /**
     * Gets the cache maximum size in gigabytes
     * @return the cache maximum size in gigabytes
     */
    public double getMaxCacheSize() {
        return maxCacheSize;
    }

    /**
     * Gets the maximum number of grid points to generate for coverage analysis
     * @return the maximum number of grid points to generate for coverage analysis
     */
    public double getMaxGridSize() {
        return maxGridSize;
    }
}
