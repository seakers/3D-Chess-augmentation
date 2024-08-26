/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

import java.util.BitSet;

/**
 * An abstract class for a feature that explains the data
 * 
 * @author Prachi
 */

public abstract class AbstractFeature implements Feature {
    /**
     * The bitset for the observations this feature matches
     */
    private final BitSet matches;
    
    private final double support;
    private final double lift;
    private final double fconfidence;
    private final double rconfidence;

    public AbstractFeature(BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        this.matches = matches;
        this.support = support;
        this.lift = lift;
        this.fconfidence = fconfidence;
        this.rconfidence = rconfidence;
    }
    
    @Override
    public BitSet getMatches() {
        return matches;
    }
    
    @Override
    public double getSupport() {
        return support;
    }

    @Override
    public double getFConfidence() {
        return fconfidence;
    }

    @Override
    public double getRConfidence() {
        return rconfidence;
    }

    @Override
    public double getLift() {
        return lift;
    }
}
