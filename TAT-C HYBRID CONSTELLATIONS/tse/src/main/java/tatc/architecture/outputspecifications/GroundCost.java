
package tatc.architecture.outputspecifications;

/**
 * Ground cost metric class
 */
public class GroundCost {

    /**
     * Ground cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Ground cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a ground cost object
     * @param estimate the ground cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the ground cost estimate standard error
     */
    public GroundCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the ground cost estimate
     * @return the ground cost estimate
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
     * Gets the ground cost estimate standard error
     * @return the ground cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }


}
