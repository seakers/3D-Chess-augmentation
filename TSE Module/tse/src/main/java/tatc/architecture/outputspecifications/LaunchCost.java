
package tatc.architecture.outputspecifications;

/**
 * Launch cost metric class
 */
public class LaunchCost {

    /**
     * Launch cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Launch cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a launch cost object
     * @param estimate the launch cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the launch cost estimate standard error
     */
    public LaunchCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the launch cost estimate
     * @return the launch cost estimate
     */
    public Double getEstimate() {
        return estimate;
    }

    /**
     * Gets the fiscal year
     * @return the fiscal year
     */
    public Integer getFiscalYear() {
        return fiscalYear;
    }

    /**
     * Gets the launch cost estimate standard error
     * @return the launch cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }

}
