
package tatc.architecture.outputspecifications;

/**
 * Operations cost metric class
 */
public class OperationsCost {

    /**
     * Operations cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Operations cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a operations cost object
     * @param estimate the operations cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the operations cost estimate standard error
     */
    public OperationsCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the operations cost estimate
     * @return the operations cost estimate
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
     * Gets the operations cost estimate standard error
     * @return the operations cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }

}
