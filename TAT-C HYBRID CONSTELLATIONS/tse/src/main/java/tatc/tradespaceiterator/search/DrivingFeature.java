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
 * metric about the design). DrivingFeature is immutable
 *
 * @author Nozomi
 */

public class DrivingFeature extends AbstractFeature {

    /**
     * Name associated to the feature;
     */
    private final String name;

    public DrivingFeature(String name, BitSet matches) {
        this(name, matches, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
    }

    public DrivingFeature(String name, BitSet matches, double support, double lift, double fconfidence, double rconfidence) {
        super(matches, support, lift, fconfidence, rconfidence);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "DrivingFeature{" + "name={" + name + "}"
                + " support=" + getSupport()
                + " fconfidence=" + getFConfidence()
                + " rconfidence=" + getRConfidence()
                + " lift=" + getLift() + '}';
    }
}
