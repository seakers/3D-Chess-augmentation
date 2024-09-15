///*
// * To change this license header, choose License Headers in Project Properties.
// * To change this template file, choose Tools | Templates
// * and open the template in the editor.
// */
//package tatc.tradespaceiterator.search;
//
//import org.moeaframework.core.PRNG;
//import org.moeaframework.core.Solution;
//import org.moeaframework.core.Variable;
//import org.moeaframework.core.Variation;
//import tatc.architecture.StandardFormArchitecture;
//import tatc.architecture.variable.*;
//import tatc.tradespaceiterator.ProblemProperties;
//import tatc.util.Factor;
//import tatc.util.Utilities;
//import java.util.*;
//
///**
// * This operator class works as a repair operator that
// * biases the search towards good regions of tradespace by changing
// * the values of decisions in the chromosomes based on the features
// * learned by KDO. It is only meant for Homogeneous, Heterogeneous,
// * Hybrid, and Train Variables. It does not work for Adhoc or GroundNetworks.
// *
// * @author Prachi
// */
//public class Operator implements Variation  {
//
//    public String name;
//
//    /**
//     * Identifies the type of constellation
//     * from operator name. e.g. Homo = 0, Hetero = 1, Train = 2,
//     * Hybrid = 3
//     */
//    private int constellationType;
//
//    /**
//     * Identifies the level of attribute
//     * from operator name. e.g. Equal = 0, High = 1, Low = 2
//     */
//    private int level;
//
//    /**
//     * Saves the values of the decisions that need to be set.
//     */
//    private String equalValues;
//
//    /**
//     * Identifies the attribute/decision that needs to be set
//     * from operator name. e.g. Alt = 0, Inc = 1,
//     * Sat = 2, Pln = 3, Phs = 4, etc.
//     */
//    private int attribute;
//
//    /**
//     * The "name" of the operator identifies it and also explains
//     * what it is supposed to do. e.g. HighAltitudeOperator would change the altitude
//     * decision of the chromosome to high.
//     *
//     * @param name
//     */
//    public Operator(String name) throws Exception {
//        this.name = name;
//        Exception ex;
//        String string, substring, subsubstring;
//
//        if (name.startsWith("Hom")) {
//            this.constellationType = 0;
//            string = name.replaceFirst("Hom", "");
//        } else if (name.startsWith("Het")) {
//            this.constellationType = 1;
//            string = name.replaceFirst("Het", "");
//        } else if (name.startsWith("Trn")) {
//            this.constellationType = 2;
//            string = name.replaceFirst("Trn", "");
//        } else if (name.startsWith("Hyb")) {
//            this.constellationType = 3;
//            string = name.replaceFirst("Hyb", "");
//        } else {
//            ex = new Exception("Unsupported constellation type. KDO is only valid for Homogeneous, Heterogeneous, " +
//                    "Train and Hybrid constellation types.");
//            throw ex;
//        }
//
//        if (string.startsWith("Alt")) {
//            this.attribute = 0;
//            substring = string.replaceFirst("Alt", "");
//        } else if (string.startsWith("Inc")) {
//            this.attribute = 1;
//            substring = string.replaceFirst("Inc", "");
//        } else if (string.startsWith("Sat")) {
//            this.attribute = 2;
//            substring = string.replaceFirst("Sat", "");
//        } else if (string.startsWith("Pln")) {
//            this.attribute = 3;
//            substring = string.replaceFirst("Pln", "");
//        } else if (string.startsWith("Phs")) {
//            this.attribute = 4;
//            substring = string.replaceFirst("Phs", "");
//        } else if (string.startsWith("Spc")) {
//            this.attribute = 5;
//            substring = string.replaceFirst("Spc", "");
//        } else if (string.startsWith("Orb")) {
//            this.attribute = 6;
//            substring = string.replaceFirst("Orb", "");
//        } else if (string.startsWith("Has")) {
//            this.attribute = 7;
//            substring = string.replaceFirst("Has", "");
//        }
//        else {
//            ex = new Exception("Unsupported attribute type.");
//            throw ex;
//        }
//
//        if (substring.startsWith("Equal")) {
//            this.level = 0;
//            subsubstring = substring.replaceFirst("Equal", "");
//        } else if (substring.startsWith("High")) {
//            this.level = 1;
//            subsubstring = substring.replaceFirst("High", "");
//        } else if (substring.startsWith("Low")) {
//            this.level = 2;
//            subsubstring = substring.replaceFirst("Low", "");
//        } else if (substring.startsWith("Constel")) {
//            this.level = 3;
//            subsubstring = substring.replaceFirst("Constel", "");
//        }
//        else {
//            ex = new Exception("Unsupported level type. KDO is only valid for High, Low and Equal level types.");
//            throw ex;
//        }
//
//        this.equalValues = subsubstring;
//    }
//
//    /**
//     * This operator only takes in one solution and modifies it
//     */
//    @Override
//    public int getArity() {
//        return 1;
//    }
//
//    @Override
//    public Solution[] evolve(Solution[] parent) {
//
//        Solution child = parent[0].copy();
//
//        try {
//            switch (this.constellationType) {
//                case 0: //Homogeneous
//                    for (int i = 0; i < child.getNumberOfVariables(); i++) {
//
//                        if (child.getVariable(i) instanceof HomogeneousWalkerVariable) {
//
//                            HomogeneousWalkerVariable constellation = (HomogeneousWalkerVariable) child.getVariable(i);
//
//                            switch (this.attribute) {
//                                case 0: //Altitude
//                                    List<Double> aAllowed = new ArrayList<>(constellation.getAltAllowed());
//                                    List<Double> aLeveled = new ArrayList<>();
//
//                                    switch (this.level) {
//                                        case 1: //High
//                                            for(int x = 0; x < aAllowed.size(); x++) {
//                                                if (aAllowed.get(x) >= 600) {
//                                                    aLeveled.add(aAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for(int x = 0; x < aAllowed.size(); x++) {
//                                                if (aAllowed.get(x) < 600) {
//                                                    aLeveled.add(aAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(aLeveled);
//                                    if (aLeveled.size() != 0) {
//                                        ((HomogeneousWalkerVariable) child.getVariable(i)).setAlt(aLeveled.get(0));
//                                    }
//                                    break;
//                                case 1: //Inclination
//                                    List<Object> iAllowed = new ArrayList<>(constellation.getIncAllowed());
//                                    List<Object> iLeveled = new ArrayList<>();
//
//                                    for (int x = 0; x < iAllowed.size(); x++) {
//                                        String doubleIncl = iAllowed.get(x).toString();
//                                        if (doubleIncl.equals("SSO") || doubleIncl.equals("Polar")) {
//                                            iAllowed.remove(x);
//                                            x = x - 1;
//                                        }
//                                    }
//
//                                    switch (this.level) {
//                                        case 0: //Equal
//                                                if (equalValues.contains("Polar")) {
//                                                    iLeveled.add(90.0);
//                                                }
//                                                else if (equalValues.contains("SSO")) {
//                                                    iLeveled.add("SSO");
//                                                }
//                                            break;
//                                        case 1: //High
//                                            for (int x = 0; x < iAllowed.size(); x++) {
//                                                Double doubleIncl = new Double(iAllowed.get(x).toString());
//                                                Double.parseDouble(iAllowed.get(x).toString());
//                                                if (doubleIncl >= 60)
//                                                    iLeveled.add(doubleIncl);
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for (int x = 0; x < iAllowed.size(); x++) {
//                                                Double doubleIncl = new Double(iAllowed.get(x).toString());
//                                                if (doubleIncl < 60)
//                                                    iLeveled.add(doubleIncl);
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(iLeveled);
//                                    if (iLeveled.size() != 0) {
//                                        ((HomogeneousWalkerVariable) child.getVariable(i)).setInc(iLeveled.get(0));
//                                    }
//                                    break;
//                                case 2: //NSats
//                                    List<Integer> tAllowed = new ArrayList<>(constellation.gettAllowed());
//                                    List<Integer> tLeveled = new ArrayList<>();
//
//                                    switch (this.level) {
//                                        case 1: //High
//                                            for (int x = 0; x < tAllowed.size(); x++) {
//                                                if (tAllowed.get(x) >= 5) {
//                                                    tLeveled.add(tAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for (int x = 0; x < tAllowed.size(); x++) {
//                                                if (tAllowed.get(x) < 5) {
//                                                    tLeveled.add(tAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(tLeveled);
//                                    if (tLeveled.size() != 0) {
//                                        ((HomogeneousWalkerVariable) child.getVariable(i)).setT(tLeveled.get(0));
//                                    }
//                                    break;
//                                case 3: //NPlanes
//                                    List<Integer> pAllowed = new ArrayList<>();
//                                    List<Integer> pLeveled = new ArrayList<>();
//
//                                    if (constellation.getpAllowed().size() != 0) {
//                                        pAllowed = new ArrayList<>(constellation.getpAllowed());
//                                    }
//                                    else {
//                                        pAllowed = Factor.divisors((int)constellation.getT());
//                                    }
//
//                                    HashMap<Integer, Double> mappedPlanes = new HashMap<>();
//
//                                    int counterPlanes = 0; //counter for possible planes index
//
//                                    for (double x = 1. / (2*pAllowed.size()); x <= 1; x += 1. / pAllowed.size()) {
//                                        mappedPlanes.put(pAllowed.get(counterPlanes), x);
//                                        counterPlanes = counterPlanes + 1;
//                                    }
//
//                                    switch (this.level) {
//                                        case 0: //Equal
//                                            if (this.equalValues.contains("One")) {
//                                                for (int x = 0; x < pAllowed.size(); x++) {
//                                                    pLeveled.add(1);
//                                                }
//                                            }
//                                            else if (this.equalValues.contains("N")) {
//                                                    pLeveled.add(counterPlanes);
//                                            }
//                                            break;
//                                        case 1: //High
//                                            for (int x = 0; x < pAllowed.size(); x++) {
//                                                if (pAllowed.get(x) > 4) {
//                                                    pLeveled.add(pAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for (int x = 0; x < pAllowed.size(); x++) {
//                                                if (pAllowed.get(x) <= 4) {
//                                                    pLeveled.add(pAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(pLeveled);
//                                    if (pLeveled.size() != 0 && (int)constellation.getT() != 0) {
//                                        if (pLeveled.get(0) <= (int)constellation.getT()) {
//                                            ((HomogeneousWalkerVariable) child.getVariable(i)).setpReal(mappedPlanes.get(pLeveled.get(0)));
//                                        } else {
//                                            ((HomogeneousWalkerVariable) child.getVariable(i)).setT(pLeveled.get(0)); //change the number of satellites to get each satellite in 1 plane
//                                        }
//                                    }
//                                    break;
//                                case 4: //Phasing
//                                    List<Integer> fAllowed = new ArrayList<>();
//                                    List<Integer> fLeveled = new ArrayList<>();
//
//                                    if (constellation.getpAllowed() != null) {
//                                        for (int x = 0; x < constellation.getpAllowed().size(); x++) {
//                                            fAllowed.add(constellation.getpAllowed().get(x) - 1);
//                                        }
//                                    }
//                                    else {
//                                        fAllowed = Factor.divisors((int)constellation.getT());
//                                        for (int x = 0; x < fAllowed.size(); x++) {
//                                            fAllowed.add(x, fAllowed.get(x) - 1);
//                                        }
//                                    }
//
//                                    HashMap<Integer, Double> mappedPhases = new HashMap<>();
//
//                                    int counterPhases = 0; //counter for possible phases index
//
//                                    for (double x = 1. / (2*fAllowed.size()); x <= 1; x += 1. / fAllowed.size()) {
//                                        mappedPhases.put(fAllowed.get(counterPhases), x);
//                                        counterPhases = counterPhases + 1;
//                                    }
//
//                                    switch (this.level) {
//                                        case 1: //High
//                                            for (int x = 0; x < fAllowed.size(); x++) {
//                                                if (fAllowed.get(x) > 4) {
//                                                    fLeveled.add(fAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for (int x = 0; x < fAllowed.size(); x++) {
//                                                if (fAllowed.get(x) <= 4) {
//                                                    fLeveled.add(fAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(fLeveled);
//                                    if (fLeveled.size() != 0) {
//                                        ((HomogeneousWalkerVariable) child.getVariable(i)).setfReal(mappedPhases.get(fLeveled.get(0)));
//                                    }
//                                    break;
//                            }
//                        }
//                    }
//                    break;
//                case 1: //Heterogeneous
//                    for (int i = 0; i < child.getNumberOfVariables(); i++) {
//
//                        if (child.getVariable(i) instanceof HeterogeneousWalkerVariable) {
//
//                            HeterogeneousWalkerVariable constellation = (HeterogeneousWalkerVariable) child.getVariable(i);
//                            List<PlaneVariable> planeVariable = new ArrayList<>(constellation.getPlaneVariables());
//
//                            switch (this.attribute) {
//                                case 0: //Altitude
//                                    List<Double> aAllowed = new ArrayList<>(constellation.getAltAllowed());
//                                    List<Double> aLeveled = new ArrayList<>();
//
//                                    switch (this.level) {
//                                        case 0: //Equal
//                                            Collections.shuffle(aAllowed);
//                                            for(int x = 0; x < aAllowed.size(); x++) {
//                                                aLeveled.add(aAllowed.get(0));
//                                            }
//                                            break;
//                                        case 1: //High
//                                            for(int x = 0; x < aAllowed.size(); x++) {
//                                                if (aAllowed.get(x) >= 600) {
//                                                    aLeveled.add(aAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for(int x = 0; x < aAllowed.size(); x++) {
//                                                if (aAllowed.get(x) < 600) {
//                                                    aLeveled.add(aAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                            }
//                                    for (int x = 0; x < planeVariable.size(); x++) {
//                                        Collections.shuffle(aLeveled);
//                                        planeVariable.get(x).setAlt(aLeveled.get(0));
//                                    }
//                                    ((HeterogeneousWalkerVariable) child.getVariable(i)).setPlaneVariablesAndTAndSatellite(planeVariable, constellation.getT(), constellation.getSatellite());
//                                    break;
//                                case 1: //Inclination
//                                    List<Object> iAllowed = new ArrayList<>(constellation.getIncAllowed());
//                                    List<Object> iLeveled = new ArrayList<>();
//
//                                    switch (this.level) {
//                                        case 0: //Equal
//                                            if (this.equalValues.contains("SSO")) {
//                                                iLeveled.add("SSO");
//                                            }
//                                            else if (this.equalValues.contains("Polar")) {
//                                                iLeveled.add(90.0);
//                                            }
//                                            break;
//                                        case 1: //High
//                                            for(int x = 0; x < iAllowed.size(); x++) {
//                                                if (Double.compare(Double.parseDouble(iAllowed.get(x).toString()), 60) >= 0) {
//                                                    iLeveled.add(Double.parseDouble(iAllowed.get(x).toString()));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for(int x = 0; x < iAllowed.size(); x++) {
//                                                if (Double.compare(Double.parseDouble(iAllowed.get(x).toString()), 60) < 0) {
//                                                    iLeveled.add(Double.parseDouble(iAllowed.get(x).toString()));
//                                                }
//                                            }
//                                            break;
//                                            }
//                                    for (int x = 0; x < planeVariable.size(); x++) {
//                                        Collections.shuffle(iLeveled);
//                                        planeVariable.get(x).setInc(iLeveled.get(0));
//                                    }
//                                    ((HeterogeneousWalkerVariable) child.getVariable(i)).setPlaneVariablesAndTAndSatellite(planeVariable, constellation.getT(), constellation.getSatellite());
//                                    break;
//                                case 2: //Satellites
//                                    List<Integer> tAllowed = new ArrayList<>(constellation.gettAllowed());
//                                    List<Integer> tLeveled = new ArrayList<>();
//                                    switch (this.level) {
//                                        case 1: //High
//                                            for (int x = 0; x < tAllowed.size(); x++) {
//                                                if (tAllowed.get(x) >= 5 && tAllowed.get(x)%(int)constellation.getNumberOfPlanes() == 0) {
//                                                    tLeveled.add(tAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for (int x = 0; x < tAllowed.size(); x++) {
//                                                if (tAllowed.get(x) < 5 && tAllowed.get(x)%(int)constellation.getNumberOfPlanes() == 0) {
//                                                    tLeveled.add(tAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(tLeveled);
//                                    if (tLeveled.size() != 0) {
//                                        if (planeVariable.size() > tLeveled.get(0)) {
//                                            List<PlaneVariable> newPlaneVariable = new ArrayList<>();
//                                            for (int x = 0; x < tLeveled.get(0); x++) {
//                                                newPlaneVariable.add(planeVariable.get(x));
//                                            }
//                                            ((HeterogeneousWalkerVariable) child.getVariable(i)).setPlaneVariablesAndTAndSatellite(newPlaneVariable, tLeveled.get(0), con, constellation.getSatellite());
//                                        } else {
//                                            ((HeterogeneousWalkerVariable) child.getVariable(i)).setPlaneVariablesAndTAndSatellite(planeVariable, tLeveled.get(0), constellation.getSatellite());
//                                        }
//                                    }
//                                    break;
//                                case 3: //Planes
//                                    List<Integer> pAllowed = new ArrayList<>(constellation.getPlanesAllowed());
//                                    List<Integer> pLeveled = new ArrayList<>();
//                                    switch (this.level) {
//                                        case 0: //Plane = 1
//                                            pLeveled.add(1);
//                                            break;
//                                        case 1: //High
//                                            for (int x = 0; x < pAllowed.size(); x++) {
//                                                if ((int)constellation.getT()%pAllowed.get(x) == 0 && pAllowed.get(x) > 3) {
//                                                    pLeveled.add(pAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for (int x = 0; x < pAllowed.size(); x++) {
//                                                if ((int)constellation.getT()%pAllowed.get(x) == 0 && pAllowed.get(x) <= 3) {
//                                                    pLeveled.add(pAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(pLeveled);
//                                    List<PlaneVariable> newPlaneVariable = new ArrayList<>();
//                                    for (int x = 0; x < pLeveled.get(0); x++) {
//                                        PlaneVariable plane = new PlaneVariable(new ArrayList<>(constellation.getAltAllowed()), new ArrayList<>(constellation.getIncAllowed()));
//                                        plane.randomize();
//                                        newPlaneVariable.add(plane);
//                                    }
//                                    if (newPlaneVariable.size() != 0) {
//                                        ((HeterogeneousWalkerVariable) child.getVariable(i)).setPlaneVariablesAndTAndSatellite(newPlaneVariable, constellation.getT(), constellation.getSatellite());
//                                    }
//                                    break;
//                            }
//                        }
//                    }
//                    break;
//                case 2: //Train
//                    for (int i = 0; i < child.getNumberOfVariables(); i++) {
//
//                        if (child.getVariable(i) instanceof TrainVariable) {
//
//                            TrainVariable constellation = (TrainVariable) child.getVariable(i);
//
//                            switch (this.attribute) {
//                                case 0: //Altitude
//                                    List<Double> aAllowed = new ArrayList<>(constellation.getAltAllowed());
//                                    List<Double> aLeveled = new ArrayList<>();
//                                    switch (this.level) {
//                                        case 1: //High
//                                            for(int x = 0; x < aAllowed.size(); x++) {
//                                                if (aAllowed.get(x) >= 600) {
//                                                    aLeveled.add(aAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for(int x = 0; x < aAllowed.size(); x++) {
//                                                if (aAllowed.get(x) < 600) {
//                                                    aLeveled.add(aAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(aLeveled);
//                                    if (aLeveled.size() != 0) {
//                                        ((TrainVariable) child.getVariable(i)).setAlt(aLeveled.get(0));
//                                    }
//                                    break;
//                                case 2: //Satellite
//                                    List<Integer> tAllowed = new ArrayList<>(constellation.gettAllowed());
//                                    List<Integer> tLeveled = new ArrayList<>();
//
//                                    switch (this.level) {
//                                        case 1: //High
//                                            for (int x = 0; x < tAllowed.size(); x++) {
//                                                if (tAllowed.get(x) >= 5) {
//                                                    tLeveled.add(tAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for (int x = 0; x < tAllowed.size(); x++) {
//                                                if (tAllowed.get(x) < 5) {
//                                                    tLeveled.add(tAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(tLeveled);
//                                    if (tLeveled.size() != 0) {
//                                        ((TrainVariable) child.getVariable(i)).setT(tLeveled.get(0));
//                                    }
//                                    break;
//                                case 5: //Relative Spacing
//                                    List<String> spcAllowed = new ArrayList<>(constellation.getSatIntervalsAllowed());;
//                                    List<String> spcLeveled = new ArrayList<>();
//
//                                    switch (this.level) {
//                                        case 1: //High
//                                            for (int x = 0; x < spcAllowed.size(); x++) {
//                                                if (Utilities.DurationToSeconds(spcAllowed.get(x)) >= 900) {
//                                                    spcLeveled.add(spcAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                        case 2: //Low
//                                            for (int x = 0; x < spcAllowed.size(); x++) {
//                                                if (Utilities.DurationToSeconds(spcAllowed.get(x)) < 900) {
//                                                    spcLeveled.add(spcAllowed.get(x));
//                                                }
//                                            }
//                                            break;
//                                    }
//                                    Collections.shuffle(spcLeveled);
//                                    if (spcLeveled.size() != 0) {
//                                        ((TrainVariable) child.getVariable(i)).setSatInterval(spcLeveled.get(0));
//                                    }
//                                    break;
//                                case 6: //Orbit
//                                    List<String> orbAllowed = new ArrayList<>(constellation.getLtanAllowed());
//                                    List<String> orbLeveled = new ArrayList<>();
//
//                                    for (int x = 0; x < orbAllowed.size(); x++) {
//                                        double seconds = Utilities.hhmmssToSeconds(orbAllowed.get(x));
//                                        double hours = seconds/3600;
//                                        orbAllowed.set(x, String.valueOf(hours));
//                                    }
//
//                                    switch (this.level) {
//                                        case 0: //Equal
//                                           if (this.equalValues.contains("AM")) {
//                                               for (String orbit : orbAllowed) {
//                                                   if ((Double.parseDouble(orbit) > 0 && Double.parseDouble(orbit) < 4) ||
//                                                           (Double.parseDouble(orbit) > 7 && Double.parseDouble(orbit) < 12)) {
//                                                       orbLeveled.add(Utilities.secondsToHHmmss(Double.parseDouble(orbit)*3600));
//                                                   }
//                                               }
//                                           } else if (this.equalValues.contains("PM")) {
//                                               for (String orbit : orbAllowed) {
//                                                   if ((Double.parseDouble(orbit) >= 12 && Double.parseDouble(orbit) < 17) ||
//                                                           (Double.parseDouble(orbit) > 19 && Double.parseDouble(orbit) <= 24)) {
//                                                       orbLeveled.add(Utilities.secondsToHHmmss(Double.parseDouble(orbit)*3600));
//                                                   }
//                                               }
//
//                                           } else if (this.equalValues.contains("DD")) {
//                                               for (String orbit : orbAllowed) {
//                                                   if ((Double.parseDouble(orbit) >= 5 && Double.parseDouble(orbit) <= 7) ||
//                                                           (Double.parseDouble(orbit) >= 17 && Double.parseDouble(orbit) <= 19)) {
//                                                       orbLeveled.add(Utilities.secondsToHHmmss(Double.parseDouble(orbit)*3600));
//                                                   }
//                                               }
//                                           }
//                                            break;
//                                    }
//                                    Collections.shuffle(orbLeveled);
//                                    if (orbLeveled.size() != 0) {
//                                        ((TrainVariable) child.getVariable(i)).setLTAN(orbLeveled.get(0));
//                                    }
//                                    break;
//                            }
//                        }
//                    }
//                    break;
//                case 3: //Hybrid
//                    // if the child only has 1 ground network variable and 1 constellation, then apply this operator
//                    if (child.getNumberOfVariables() == 2) {
//                        Variable newChildVariable = null;
//                        HashMap<String,Decision<?>> decisionHashMap = ProblemProperties.getInstance().getTradespaceSearch().TradespaceSearch2Decisions();
//                        int numOfConstellations = ProblemProperties.getInstance().getTradespaceSearch().getNumberOfConstellations();
//                        List<String> keys = new ArrayList<>(decisionHashMap.keySet());
//                        HashMap<String, Decision<?>> decisionList = new HashMap<>();
//                        for (String key : keys) {
//                            if (key.contains(this.equalValues) && key.contains(String.valueOf(PRNG.nextInt(0, numOfConstellations - 1)))) {
//                                decisionList.put(key, decisionHashMap.get(key));
//                            }
//                        }
//
//                        if ((int)this.attribute == 7) { //Has
//                            if ((int)this.level == 3) { //Constellation
//                                switch (this.equalValues) {
//                                    case "Homo":
//                                        Decision aDecisionHomo = null;
//                                        Decision iDecisionHomo = null;
//                                        Decision tDecisionHomo = null;
//                                        Decision pDecisionHomo = null;
//
//                                        for (String key : keys) {
//                                            if (key.contains("Altitude")) {
//                                                aDecisionHomo = decisionList.get(key);
//                                            } else if (key.contains("Inclination")) {
//                                                iDecisionHomo = decisionList.get(key);
//                                            } else if (key.contains("NumberSatellites")) {
//                                                tDecisionHomo = decisionList.get(key);
//                                            } else if (key.contains("NumberPlanes")) {
//                                                pDecisionHomo = decisionList.get(key);
//                                            }
//                                        }
//                                        HomogeneousWalkerVariable oldChildHomo = ((HomogeneousWalkerVariable) child.getVariable(1));
//                                        newChildVariable = new HomogeneousWalkerVariable(aDecisionHomo.getAllowedValues(), iDecisionHomo.getAllowedValues(),
//                                                tDecisionHomo.getAllowedValues(), pDecisionHomo.getAllowedValues(), oldChildHomo.getSatAllowed(), oldChildHomo.getSecondaryPayload(), oldChildHomo.getEccentricity());
//                                        newChildVariable.randomize();
//                                        break;
//                                    case "Hetero":
//                                        Decision aDecisionHetero = null;
//                                        Decision iDecisionHetero = null;
//                                        Decision tDecisionHetero = null;
//                                        Decision pDecisionHetero = null;
//
//                                        for (String key : keys) {
//                                            if (key.contains("Altitude")) {
//                                                aDecisionHetero = decisionList.get(key);
//                                            } else if (key.contains("Inclination")) {
//                                                iDecisionHetero = decisionList.get(key);
//                                            } else if (key.contains("NumberSatellites")) {
//                                                tDecisionHetero = decisionList.get(key);
//                                            } else if (key.contains("NumberPlanes")) {
//                                                pDecisionHetero = decisionList.get(key);
//                                            }
//                                        }
//                                        HeterogeneousWalkerVariable oldChildHetero = ((HeterogeneousWalkerVariable) child.getVariable(1));
//                                        newChildVariable = new HeterogeneousWalkerVariable(aDecisionHetero.getAllowedValues(), iDecisionHetero.getAllowedValues(),
//                                                tDecisionHetero.getAllowedValues(), pDecisionHetero.getAllowedValues(), oldChildHetero.getSatsAllowed(), oldChildHetero.getSecondaryPayload(), oldChildHetero.getEccentricity());
//                                        newChildVariable.randomize();
//                                        break;
//                                    case "Train":
//                                        Decision aDecisionTrain = null;
//                                        Decision tDecisionTrain = null;
//                                        Decision LTANDecisionTrain = null;
//                                        Decision deltaLTANDecisionTrain = null;
//
//                                        for (String key : keys) {
//                                            if (key.contains("Altitude")) {
//                                                aDecisionTrain = decisionList.get(key);
//                                            } else if (key.contains("NumberSatellites")) {
//                                                tDecisionTrain = decisionList.get(key);
//                                            } else if (key.contains("LTAN")) {
//                                                LTANDecisionTrain = decisionList.get(key);
//                                            } else if (key.contains("Interval")) {
//                                                deltaLTANDecisionTrain = decisionList.get(key);
//                                            }
//                                        }
//                                        TrainVariable oldChildTrain = ((TrainVariable) child.getVariable(1));
//                                        newChildVariable = new TrainVariable(aDecisionTrain.getAllowedValues(), tDecisionTrain.getAllowedValues(), LTANDecisionTrain.getAllowedValues(),
//                                                deltaLTANDecisionTrain.getAllowedValues(), oldChildTrain.getSatAllowed(), oldChildTrain.getSecondaryPayload(), oldChildTrain.getEccentricity());
//                                        newChildVariable.randomize();
//                                        break;
//                                }
//                                if (child.getVariable(1).hashCode() != newChildVariable.hashCode()) {
//                                    Solution sol = new StandardFormArchitecture(3, child.getNumberOfObjectives(), child.getNumberOfConstraints());
//                                    sol.setVariable(0, child.getVariable(0));
//                                    sol.setVariable(1, child.getVariable(1));
//                                    sol.setVariable(2, newChildVariable);
//                                    return new Solution[]{sol};
//                                }
//                            }
//                        }
//                    }
//                    break;
//            }
//        } catch (Exception e) {
//            System.out.println(e.getMessage());
//        }
//        return new Solution[]{child};
//    }
//
//    /**
//     * Returns the name of the current operator
//     * @return
//     */
//    @Override
//    public String toString() {
//        return name;
//    }
//
//    @Override
//    public int hashCode() {
//        int hash = 7;
//        hash = 83 * hash + this.name.hashCode();
//        return hash;
//    }
//
//    /**
//     * Checks if the operator matches the current object.
//     * @param obj
//     * @return
//     */
//    @Override
//    public boolean equals(Object obj) {
//        if (obj == null) {
//            return false;
//        }
//        if (getClass() != obj.getClass()) {
//            return false;
//        }
//        final Operator other = (Operator) obj;
//        if (this.constellationType != other.constellationType) {
//            return false;
//        }
//        if (this.level != other.level) {
//            return false;
//        }
//        if (this.attribute != other.attribute) {
//            return false;
//        }
//        return true;
//    }
//}