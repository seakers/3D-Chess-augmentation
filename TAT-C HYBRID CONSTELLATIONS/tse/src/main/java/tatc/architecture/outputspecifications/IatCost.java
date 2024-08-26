
package tatc.architecture.outputspecifications;

/**
 * Integration, assurance and test cost metric class
 */
public class IatCost {

    /**
     * Integration, assurance and test cost estimate
     */
    private final Double estimate;
    /**
     * Fiscal year
     */
    private final Integer fiscalYear;
    /**
     * Integration, assurance and test cost estimate standard error
     */
    private final Double standardError;

    /**
     * Constructs a integration, assurance and test cost object
     * @param estimate the integration, assurance and test cost estimate
     * @param fiscalYear the fiscal year
     * @param standardError the integration, assurance and test cost estimate standard error
     */
    public IatCost(Double estimate, Integer fiscalYear, Double standardError) {
        this.estimate = estimate;
        this.fiscalYear = fiscalYear;
        this.standardError = standardError;
    }

    /**
     * Gets the integration, assurance and test cost estimate
     * @return the integration, assurance and test cost estimate
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
     * Gets the integration, assurance and test cost estimate standard error
     * @return the integration, assurance and test cost estimate standard error
     */
    public Double getStandardError() {
        return standardError;
    }

}
