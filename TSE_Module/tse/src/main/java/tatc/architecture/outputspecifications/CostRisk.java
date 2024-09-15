
package tatc.architecture.outputspecifications;

import java.util.List;

/**
 * Class that contains all the outputs/metrics from cost and risk module
 */
public class CostRisk {
    /**
     * Ground cost
     */
    private final GroundCost groundCost;
    /**
     * Hardware cost
     */
    private final HardwareCost hardwareCost;
    /**
     * Integration, assurance and test cost
     */
    private final IatCost iatCost;
    /**
     * Launch cost
     */
    private final LaunchCost launchCost;
    /**
     * Lifecycle cost
     */
    private final LifecycleCost lifecycleCost;
    /**
     * Non-recurring cost
     */
    private final NonRecurringCost nonRecurringCost;
    /**
     * Operations cost
     */
    private final OperationsCost operationsCost;
    /**
     * Program cost
     */
    private final ProgramCost programCost;
    /**
     * Recurring cost
     */
    private final RecurringCost recurringCost;
    /**
     * System risk
     */
    private final List<SystemRisk> systemRisk;

    /**
     * Constructs a cost and risk object with all of the metrics
     * @param groundCost the ground cost
     * @param hardwareCost the hardware cost
     * @param iatCost the integration, assurance and test cost
     * @param launchCost the launch cost
     * @param lifecycleCost the lifecycle cost
     * @param nonRecurringCost the non-recurring cost
     * @param operationsCost the operations cost
     * @param programCost the program cost
     * @param recurringCost the recurring cost
     * @param systemRisk the system risk
     */
    public CostRisk(GroundCost groundCost, HardwareCost hardwareCost, IatCost iatCost, LaunchCost launchCost, LifecycleCost lifecycleCost, NonRecurringCost nonRecurringCost, OperationsCost operationsCost, ProgramCost programCost, RecurringCost recurringCost, List<SystemRisk> systemRisk) {
        this.groundCost = groundCost;
        this.hardwareCost = hardwareCost;
        this.iatCost = iatCost;
        this.launchCost = launchCost;
        this.lifecycleCost = lifecycleCost;
        this.nonRecurringCost = nonRecurringCost;
        this.operationsCost = operationsCost;
        this.programCost = programCost;
        this.recurringCost = recurringCost;
        this.systemRisk = systemRisk;
    }

    /**
     * Gets the ground cost
     * @return the ground cost
     */
    public GroundCost getGroundCost() {
        return groundCost;
    }

    /**
     * Gets the hardware cost
     * @return the hardware cost
     */
    public HardwareCost getHardwareCost() {
        return hardwareCost;
    }

    /**
     * Gets the integration, assurance and test cost
     * @return the integration, assurance and test cost
     */
    public IatCost getIatCost() {
        return iatCost;
    }

    /**
     * Gets the launch cost
     * @return the launch cost
     */
    public LaunchCost getLaunchCost() {
        return launchCost;
    }

    /**
     * Gets the lifecycle cost
     * @return the lifecycle cost
     */
    public LifecycleCost getLifecycleCost() {
        return lifecycleCost;
    }

    /**
     * Gets the non-recurring cost
     * @return the non-recurring cost
     */
    public NonRecurringCost getNonRecurringCost() {
        return nonRecurringCost;
    }

    /**
     * Gets the operations cost
     * @return the operations cost
     */
    public OperationsCost getOperationsCost() {
        return operationsCost;
    }

    /**
     * Gets the program cost
     * @return the program cost
     */
    public ProgramCost getProgramCost() {
        return programCost;
    }

    /**
     * Gets the recurring cost
     * @return the recurring cost
     */
    public RecurringCost getRecurringCost() {
        return recurringCost;
    }

    /**
     * Gets the system risk
     * @return the system risk
     */
    public List<SystemRisk> getSystemRisk() {
        return systemRisk;
    }
}
