package tatc.architecture.specifications;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Class that sets the configuration options to filter analysis outputs based on ranges of parameters
 */
public class Outputs implements Serializable {
    /**
     * Type tag for JSON file
     */
    @SerializedName("@type")
    private final String _type="AnalysisOutputs";

    /**
     * Desired time step to record spacecraft state observations. True uses minimum simulation time step. False toggles
     * outputs off.
     */
    private final boolean obsTimeStep;

    /**
     * Optionally toggles orbits module outputs for global (per target region) coverage metrics.
     */
    private final boolean orbitsGlobal;

    /**
     * Optionally toggles orbits module outputs for local (per point of interest) metrics
     */
    private final boolean orbitsLocal;

    /**
     * Optionally toggles orbits module outputs for states (spacecraft state variables) metrics
     */
    private final boolean orbitsStates;

    /**
     * Optionally toggles orbits module outputs for access (per point of interest) metrics
     */
    private final boolean orbitsAccess;

    /**
     * Optionally toggles orbits module outputs for instrument access (per point of interset) metrics
     */
    private final boolean orbitsInstrumentAccess;

    /**
     * Optionally keeps the low level data after architectures have been enumerated and evaluated (accessInfo,
     * level0_data_metrics.csv and obs.csv
     */
    private final boolean keepLowLevelData;

    /**
     * Constructs an outputs object to configure options to filter analysis outputs based on ranges of parameters
     * @param obsTimeStep desired time step to record spacecraft state observations (True uses minimum simulation time
     *                    step and False toggles outputs off)
     * @param orbitsGlobal option to output (true) orbits module outputs for global (per target region) coverage metrics
     * @param orbitsLocal option to output (true) orbits module outputs for local (per point of interest) metrics
     * @param orbitsStates option to output (true) orbits module outputs for states (spacecraft state variables) metrics
     * @param orbitsAccess option to output (true) orbits module outputs for access (per point of interest) metrics
     * @param orbitsInstrumentAccess option to output (true) orbits module outputs for instrument access (per point of
     *                               interset) metrics
     * @param keepLowLevelData controls the elimination of the low level data
     */
    public Outputs(boolean obsTimeStep, boolean orbitsGlobal, boolean orbitsLocal, boolean orbitsStates,
                   boolean orbitsAccess, boolean orbitsInstrumentAccess, boolean keepLowLevelData) {
        this.obsTimeStep = obsTimeStep;
        this.orbitsGlobal = orbitsGlobal;
        this.orbitsLocal = orbitsLocal;
        this.orbitsStates = orbitsStates;
        this.orbitsAccess = orbitsAccess;
        this.orbitsInstrumentAccess = orbitsInstrumentAccess;
        this.keepLowLevelData = keepLowLevelData;
    }

    /**
     * Checks the desired time step to record spacecraft state observations (True uses minimum simulation time
     * step and False toggles outputs off)
     * @return
     */
    public boolean isObsTimeStep() {
        return obsTimeStep;
    }
    /**
     * Checks the option to output (true) orbits module outputs for global (per target region) coverage metrics
     * @return true if they have to be output and false otherwise
     */
    public boolean isOrbitsGlobal() {
        return orbitsGlobal;
    }
    /**
     * Checks option to output (true) orbits module outputs for local (per point of interest) metrics
     * @return true if they have to be output and false otherwise
     */
    public boolean isOrbitsLocal() {
        return orbitsLocal;
    }
    /**
     * Checks the option to output (true) orbits module outputs for states (spacecraft state variables) metrics
     * @return true if they have to be output and false otherwise
     */
    public boolean isOrbitsStates() {
        return orbitsStates;
    }
    /**
     * Checks the option to output (true) orbits module outputs for access (per point of interest) metrics
     * @return true if they have to be output and false otherwise
     */
    public boolean isOrbitsAccess() {
        return orbitsAccess;
    }
    /**
     * Checks the option to output (true) orbits module outputs for instrument access (per point of interset) metrics
     * @return true if they have to be output and false otherwise
     */
    public boolean isOrbitsInstrumentAccess() {
        return orbitsInstrumentAccess;
    }

    /**
     * Checks if the low level data has to be eliminated after an architecture has been evaluated
     * @return true if the low level data has to be kept and false otherwise
     */
    public boolean isKeepLowLevelData() {
        return keepLowLevelData;
    }
}
