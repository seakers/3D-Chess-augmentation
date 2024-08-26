
package tatc.architecture.outputspecifications;

/**
 * Non-recurring cost metric class
 */
public class NonRecurringCost {

    /**
     * Non-recurring cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Non-recurring cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a non-recurring cost object
     * @param estimate the non-recurring cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the non-recurring cost estimate standard error
     */
    public NonRecurringCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the non-recurring cost estimate
     * @return the non-recurring cost estimate
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
     * Gets the non-recurring cost estimate standard error
     * @return the non-recurring cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }

}
