
package tatc.architecture.outputspecifications;

/**
 * Hardware cost metric class
 */
public class HardwareCost {

    /**
     * Hardware cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Hardware cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a hardware cost object
     * @param estimate the hardware cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the hardware cost estimate standard error
     */
    public HardwareCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the hardware cost estimate
     * @return the hardware cost estimate
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
     * Gets the hardware cost estimate standard error
     * @return the hardware cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }

}
