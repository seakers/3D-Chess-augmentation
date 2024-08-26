
package tatc.architecture.outputspecifications;

/**
 * Recurring cost metric class
 */
public class RecurringCost {

    /**
     * Recurring cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Recurring cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a recurring cost object
     * @param estimate the recurring cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the recurring cost estimate standard error
     */
    public RecurringCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the recurring cost estimate
     * @return the recurring cost estimate
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
     * Gets the recurring cost estimate standard error
     * @return the recurring cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }

}
