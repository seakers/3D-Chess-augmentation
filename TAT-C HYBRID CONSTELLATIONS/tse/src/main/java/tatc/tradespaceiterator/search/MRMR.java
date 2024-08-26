/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.tradespaceiterator.search;

/**
 *
 * @author Nozomi
 */

import org.hipparchus.util.FastMath;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.logging.Logger;

public class MRMR {

    public static List<DrivingFeature> minRedundancyMaxRelevance(int numberOfObservations, BitSet label, List<DrivingFeature> features, int finalNFeatures) {

        long t0 = System.currentTimeMillis();
        Logger.getGlobal().finer("...[mRMR] running mRMR");
        
        //create the bitset saying which solutions have the features
        BitSet[] dataFeatureMat = new BitSet[features.size()];
        int bi = 0;
        for (DrivingFeature feat : features) {
            dataFeatureMat[bi] = feat.getMatches();
            bi++;
        }

        ArrayList<Integer> selectedFeatures = new ArrayList<>();

        while (selectedFeatures.size() < finalNFeatures) {

            int bestFeatInd = -1;
            double phi = Double.NEGATIVE_INFINITY;

            // Implement incremental search for each feature
            for (int i = 0; i < features.size(); i++) {

                if (selectedFeatures.contains(i)) {
                    continue;
                }

                //data relevancy 
                double D = computeMutualInformation(dataFeatureMat[i], label, numberOfObservations);
                
                //data redundancy
                double R = 0;
                for (int j : selectedFeatures) {
                    R = R + computeMutualInformation(dataFeatureMat[i], dataFeatureMat[j], numberOfObservations);
                }

                if (!selectedFeatures.isEmpty()) {
                    R /= (double) selectedFeatures.size();
                }

                if (D - R > phi) {
                    phi = D - R;
                    bestFeatInd = i;
                }
            }
            selectedFeatures.add(bestFeatInd);
        }

        ArrayList<DrivingFeature> out = new ArrayList<>();
        for (int index : selectedFeatures) {
            out.add(features.get(index));
        }

        long t1 = System.currentTimeMillis();
        Logger.getGlobal().finer(
                    String.format("...[mRMR] Finished running mRMR in %.2f sec",(t1 - t0)/1000.));
        return out;
    }

    private static double computeMutualInformation(BitSet set1, BitSet set2, int numberOfObservations) {
        double x1 = set1.cardinality();
        double x2 = set2.cardinality();
        BitSet bx1x2 = (BitSet) set1.clone();
        bx1x2.and(set2);
        double x1x2 = bx1x2.cardinality();

        BitSet bnx1x2 = (BitSet) set1.clone();
        bnx1x2.flip(0, numberOfObservations);
        bnx1x2.and(set2);
        double nx1x2 = bnx1x2.cardinality();

        BitSet bx1nx2 = (BitSet) set2.clone();
        bx1nx2.flip(0, numberOfObservations);
        bx1nx2.and(set1);
        double x1nx2 = bx1nx2.cardinality();

        BitSet bnx1 = (BitSet) set1.clone();
        BitSet bnx2 = (BitSet) set2.clone();
        bnx1.flip(0, numberOfObservations);
        bnx2.flip(0, numberOfObservations);
        bnx1.and(bnx2);
        double nx1nx2 = bnx1.cardinality();

        double p_x1 = (double) x1 / numberOfObservations;
        double p_nx1 = (double) 1 - p_x1;
        double p_x2 = (double) x2 / numberOfObservations;
        double p_nx2 = (double) 1 - p_x2;
        double p_x1x2 = (double) x1x2 / numberOfObservations;
        double p_nx1x2 = (double) nx1x2 / numberOfObservations;
        double p_x1nx2 = (double) x1nx2 / numberOfObservations;
        double p_nx1nx2 = (double) nx1nx2 / numberOfObservations;

        double i1, i2, i3, i4;
        //handle cases when there p(x) = 0
        if (p_x1 == 0 || p_x2 == 0 || p_x1x2 == 0) {
            i1 = 0;
        } else {
            i1 = p_x1x2 * FastMath.log(2,p_x1x2 / (p_x1 * p_x2));
        }

        if (p_x1 == 0 || p_nx2 == 0 || p_x1nx2 == 0) {
            i2 = 0;
        } else {
            i2 = p_x1nx2 * FastMath.log(2,p_x1nx2 / (p_x1 * p_nx2));
        }

        if (p_nx1 == 0 || p_x2 == 0 || p_nx1x2 == 0) {
            i3 = 0;
        } else {
            i3 = p_nx1x2 * FastMath.log(2,p_nx1x2 / (p_nx1 * p_x2));
        }

        if (p_nx1 == 0 || p_nx2 == 0 || p_nx1nx2 == 0) {
            i4 = 0;
        } else {
            i4 = p_nx1nx2 * FastMath.log(2,p_nx1nx2 / (p_nx1 * p_nx2));
        }

        double sumI = i1 + i2 + i3 + i4;
        if (sumI < 0) {
            throw new IllegalStateException("Mutual information must be positive. Computed a negative value.");
        } else {
            return sumI;
        }
    }
}