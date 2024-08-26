
package tatc.architecture.outputspecifications;

/**
 * Lifecycle cost metric class
 */
public class LifecycleCost {

    /**
     * Lifecycle cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Lifecycle cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a lifecycle cost object
     * @param estimate the lifecycle cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the lifecycle cost estimate standard error
     */
    public LifecycleCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the lifecycle cost estimate
     * @return the lifecycle cost estimate
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
     * Gets the lifecycle cost estimate standard error
     * @return the lifecycle cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }



}
