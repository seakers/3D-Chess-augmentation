///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package tatc.tradespaceiterator.search;
//
//import org.moeaframework.core.Variation;
//import org.moeaframework.core.operator.CompoundVariation;
//import java.io.*;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * This class creates operators using
// * a file that contains top features found during the data mining process.
// * @author Prachi
// */
//public class OperatorCreator implements seakers.aos.operatorselectors.replacement.OperatorCreator {
//
//    private final HashMap<String, Variation> operatorSet;
//
//    public OperatorCreator() {
//        this.operatorSet = new HashMap<>();
//    }
//
//    /**
//     * Learns a new set of potential operators based on the feature file
//     *
//     * @param featureFile
//     */
//    public void learnFeatures(File featureFile) throws Exception {
//        operatorSet.clear(); // clear previously created operators
//        Collection<String> features = readFeatures(featureFile);
//        for (String feature : features) {
//            Variation operator = featureToOperator(feature);
//            operatorSet.put(feature, operator);
//        }
//    }
//
//    /**
//     * Reads the feature file to create new operators
//     *
//     * @param featureFile
//     * @return
//     */
//    private Collection<String> readFeatures(File featureFile) {
//        ArrayList<String> features = new ArrayList<>();
//
//        try (BufferedReader br = new BufferedReader(new FileReader(featureFile))) {
//            String line = br.readLine(); //skip the header
//
//            while ((line = br.readLine()) != null) {
//                String[] str = line.split(",");
//                for (int i = 0; i < str.length; i++) {
//                    features.add(str[1]);
//                }
//            }
//        } catch (FileNotFoundException ex) {
//            Logger.getLogger(OperatorCreator.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (IOException ex) {
//            Logger.getLogger(File.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return features;
//    }
//
//    /**
//     * Creates new operators based on features learned
//     *
//     * @param featureString
//     * @return
//     */
//    public Variation featureToOperator(String featureString) throws Exception {
//        CompoundVariation operator = new CompoundVariation();
//        String operatorName = "";
//
//        String[] atomicFeature = featureString.split(" & ");
//
//        for (int i = 0; i < atomicFeature.length; i++) {
//            Operator op = new Operator(atomicFeature[i]);
//            operator.appendOperator(op);
//            if (i < atomicFeature.length - 1) {
//                operatorName += op.toString() + "+";
//            } else {
//                operatorName += op.toString();
//            }
//        }
//
//        if (operatorName.equalsIgnoreCase("")) {
//            throw new IllegalArgumentException(String.format("%s does not fit feature pattern.", featureString));
//        }
//        operator.setName(operatorName);
//        return operator;
//    }
//
//    /**
//     * Returns a random operator from the set
//     *
//     * @return
//     */
//    @Override
//    public Variation createOperator() {
//        Collections.shuffle((List<?>) operatorSet);
//        return operatorSet.get(0);
//    }
//
//    /**
//     * Returns all the operators created from the feature file
//     *
//     * @return
//     */
//    public HashMap<String, Variation> getOperatorSet() {
//        return operatorSet;
//    }
//
//    /**
//     * Returns a new set of operators randomly selected from those available to
//     * create
//     *
//     * @param numOfOperators
//     * @return
//     */
//    public Collection<Variation> createOperator(int numOfOperators) {
//        if (numOfOperators > operatorSet.size()) {
//            throw new IllegalArgumentException(String.format("Cannot create "
//                    + "more operators than are available. Tried to create %d "
//                    + "operators but only %d available", numOfOperators, operatorSet.size()));
//        }
//        ArrayList<Variation> out = new ArrayList<>(operatorSet.values());
//        for (int i = 0; i < numOfOperators; i++) {
//            out.add((operatorSet).get(i));
//        }
//        return out;
//    }
//}