
package tatc.architecture.outputspecifications;

/**
 * Program cost metric class
 */
public class ProgramCost {

    /**
     * Program cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Program cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a program cost object
     * @param estimate the program cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the program cost estimate standard error
     */
    public ProgramCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the program cost estimate
     * @return the program cost estimate
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
     * Gets the program cost estimate standard error
     * @return the program cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }


}
