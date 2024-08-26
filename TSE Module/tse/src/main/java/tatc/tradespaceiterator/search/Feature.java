/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import java.util.BitSet;

/**
 * A feature that explains data. The feature is viewed as a rule where X->Y
 * means X (a set of decision variables of design features) implies Y (some
 * metric about the design)
 * 
 * @author Prachi
 */
public interface Feature {
    /**
     * Gets the support of the feature
     *
     * @return
     */
    public double getSupport();

    /**
     * Gets the forward confidence of the feature. Given that a solution has a
     * feature, what is the likelihood of it also being in target region?
     *
     * @return
     */
    public double getFConfidence();

    /**
     * Gets the reverse confidence of the feature. Given that a solution is in
     * the target region, what is the likelihood of it also containing feature?
     *
     * @return
     */
    public double getRConfidence();

    /**
     * Gets the lift of the feature
     *
     * @return
     */
    public double getLift();

    /**
     * Gets the bit set that contains 1 for every observation that matches the
     * feature
     *
     * @return the bit set that contains 1 for every observation that matches
     * the feature
     */
    public BitSet getMatches();

}
