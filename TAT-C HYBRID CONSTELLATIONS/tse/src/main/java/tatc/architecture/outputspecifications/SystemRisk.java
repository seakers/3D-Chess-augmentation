
package tatc.architecture.outputspecifications;

/**
 * System risk metric class
 */
public class SystemRisk {

    /**
     * Category of the risk
     */
    private final String category;
    /**
     * Consequence of the risk
     */
    private final Integer consequence;
    /**
     * Likelihood of the risk
     */
    private final Integer likelihood;
    /**
     * Description of the risk
     */
    private final String risk;

    /**
     * Constructs a system risk object
     * @param category the category of the risk
     * @param consequence the consequence of the risk
     * @param likelihood the likelihood of the risk
     * @param risk the description of the risk
     */
    public SystemRisk(String category, Integer consequence, Integer likelihood, String risk) {
        this.category = category;
        this.consequence = consequence;
        this.likelihood = likelihood;
        this.risk = risk;
    }

    /**
     * Gets the category of the risk
     * @return the category of the risk
     */
    public String getCategory() {
        return category;
    }

    /**
     * Gets the consequence of the risk
     * @return the consequence of the risk
     */
    public Integer getConsequence() {
        return consequence;
    }

    /**
     * Gets the likelihood of the risk
     * @return the likelihood of the risk
     */
    public Integer getLikelihood() {
        return likelihood;
    }

    /**
     * Gets the description of the risk
     * @return the description of the risk
     */
    public String getRisk() {
        return risk;
    }

}
