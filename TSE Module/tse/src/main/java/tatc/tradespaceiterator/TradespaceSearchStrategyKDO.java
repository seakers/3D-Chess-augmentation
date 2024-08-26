//package tatc.tradespaceiterator;
//
//import org.apache.commons.lang3.ArrayUtils;
//import org.apache.commons.lang3.StringUtils;
//import org.moeaframework.algorithm.AbstractEvolutionaryAlgorithm;
//import org.moeaframework.algorithm.EpsilonMOEA;
//import org.moeaframework.core.*;
//import org.moeaframework.core.comparator.ParetoDominanceComparator;
//import org.moeaframework.core.indicator.QualityIndicator;
//import org.moeaframework.core.operator.CompoundVariation;
//import org.moeaframework.core.operator.RandomInitialization;
//import org.moeaframework.core.operator.TournamentSelection;
//import org.moeaframework.core.operator.TwoPointCrossover;
//import seakers.aos.aos.AOSMOEA;
//import seakers.aos.creditassignment.setimprovement.SetImprovementDominance;
//import seakers.aos.history.AOSHistoryIO;
//import seakers.aos.operator.AOSVariation;
//import seakers.aos.operator.AOSVariationSI;
//import seakers.aos.operatorselectors.AdaptivePursuit;
//import seakers.aos.operatorselectors.OperatorSelector;
//import seakers.aos.operatorselectors.replacement.*;
//import tatc.ResultIO;
//import tatc.interfaces.GUIInterface;
//import tatc.tradespaceiterator.search.OperatorCreator;
//import tatc.tradespaceiterator.search.*;
//import tatc.util.Utilities;
//import java.io.BufferedReader;
//import java.io.File;
//import java.io.FileReader;
//import java.io.IOException;
//import java.nio.file.Paths;
//import java.util.*;
//import java.util.logging.Level;
//import java.util.logging.Logger;
//
///**
// * Knowledge Driven Optimization (KDO) search strategy. It uses an MOEA with an adaptive operator selector controlling
// * the use of the operators. It also includes online creation of new knowledge operators based on learning the good features
// * of the evaluated designs.
// */
//public class TradespaceSearchStrategyKDO extends TradespaceSearchStrategyGA {
//
//    private HashMap<String, Integer> baseFeatures;
//
//    /**
//     * Constructs a Knowledge Driven Optimization (KDO) search strategy
//     *
//     * @param properties the problem properties
//     */
//    public TradespaceSearchStrategyKDO(ProblemProperties properties) {
//        super(properties);
//        baseFeatures = new HashMap<>();
//    }
//
//    public void start() {
//
//        long startTime = System.nanoTime();
//        //create a collection of operators
//        Collection<Variation> operators = new ArrayList<>();
//
//        //get individual operators from a list
//        //domain independent operators
//        ArrayList<Variation> iOperators = properties.getTradespaceSearch().getSettings().getSearchParameters().getiOperators();
//        CompoundVariation compoundVariation = new CompoundVariation();
//        for (int i = 0; i < iOperators.size(); i++) {
//            compoundVariation.appendOperator(iOperators.get(i));
//        }
//
//        //domain specific operators from the knowledge base
//        //make a call to Knowledge Base Interface class to get operators specific to this problem
//        ArrayList<Variation> dOperators = properties.getTradespaceSearch().getSettings().getSearchParameters().getdOperators();
//
//        operators.add(compoundVariation);
//        operators.addAll(dOperators);
//
//        //initialize the population
//        Initialization initialization = new RandomInitialization(this.problem, populationSize);
//        Population initialPopulation = new Population();
//        ParetoDominanceComparator comparator = new ParetoDominanceComparator();
//        NondominatedPopulation nondominatedPopulation = new NondominatedPopulation(comparator);
//
//        //set up OperatorReplacementStrategy
//        int epochLength = getNFEtriggerDM;
//        EpochTrigger epochTrigger = new EpochTrigger(epochLength, 0);
//        InitialTrigger initialTrigger = new InitialTrigger(150);
//        CompoundTrigger compoundTrigger = new CompoundTrigger(Arrays.asList(epochTrigger, initialTrigger));
//
//        //creates operators during mining
//        OperatorCreator operatorCreator = new OperatorCreator();
//
//        //removes N operators during search
//        RemoveNLowest operatorRemover = new RemoveNLowest(operators, nOperRepl);
//        OperatorReplacementStrategy operatorReplacementStrategy = new OperatorReplacementStrategy(compoundTrigger, operatorRemover, operatorCreator);
//
//        //create operator selector
//        OperatorSelector operatorSelector = new AdaptivePursuit(operators, alpha, beta, pmin);
//
//        //create credit assignment strategy
//        //receives credit = 1 if the new solution is nondominated, credit = 0 if new solution is dominated
//        EpsilonBoxDominanceArchive archive = new EpsilonBoxDominanceArchive(epsilon);
//        SetImprovementDominance creditAssignment = new SetImprovementDominance(archive, 1, 0);
//
//        //set population labeler
//        AbstractPopulationLabeler labeler = new NondominatedSortingLabeler(.25);
//
//        TournamentSelection selection = new TournamentSelection(properties.getTradespaceSearch().getSettings().getSearchParameters().getSizeTournament(), comparator);
//
//        //create AOS
//        AOSVariation aosStrategy = new AOSVariationSI(operatorSelector, creditAssignment, populationSize);
//        EpsilonMOEA emoea = new EpsilonMOEA(problem, initialPopulation, archive,
//                selection, aosStrategy, initialization, comparator);
//        AOSMOEA aos = new AOSMOEA(emoea, aosStrategy, true);
//
//        System.out.println("Starting " + aos.getClass().getSimpleName() + " on " + aos.getProblem().getName() + " with pop size: " + populationSize);
//        aos.step();
//
//        //keep track of each solution that is ever created, but only keep the unique ones
//        HashSet<Solution> allSolutions = new HashSet();
//        Population initPop = ((AbstractEvolutionaryAlgorithm) aos).getPopulation();
//        for (int i = 0; i < initPop.size(); i++) {
//            initPop.get(i).setAttribute("NFE", 0);
//            allSolutions.add(initPop.get(i));
//        }
//
//        //calculate HV of initial population
//        QualityIndicator qualityIndicator = new QualityIndicator(problem, aos.getResult());
//        HashMap<Integer, Double[]> hypervolume = new HashMap<>();
//
//        //count the number of times we reset operators
//        int operatorResetCount = 0;
//
//        //get base features
//        System.out.println("Getting base features for the problem...");
//        this.baseFeatures = getBaseFeatures();
//
//        while (!aos.isTerminated() && aos.getNumberOfEvaluations() < maxNFE) {
//            Population pop = ((AbstractEvolutionaryAlgorithm) aos).getPopulation();
//
//            double currentTime = ((System.nanoTime() - startTime) / Math.pow(10, 9)) / 60.;
//            System.out.println(
//                    String.format("%d NFE out of %d NFE: Time elapsed = %10f min."
//                                    + "Approximate time remaining %10f min.",
//                            aos.getNumberOfEvaluations(), maxNFE, currentTime,
//                            currentTime / emoea.getNumberOfEvaluations() * (maxNFE - aos.getNumberOfEvaluations())));
//
//            //check to see if the operators need to be replaced
//            if (operatorReplacementStrategy.checkTrigger(aos)) {
//                System.out.println(String.format("Operator replacement event triggered at %d func eval", aos.getNumberOfEvaluations()));
//
//                //reset properties of the algorithm
//                aos.getOperatorSelector().reset();
//                operatorResetCount++;
//
//                //remove inefficient operators from the pool
//                Collection<Variation> removedOperators = operatorReplacementStrategy.removeOperators(aos);
//                for (Variation removeOperator : removedOperators) {
//                    if (removeOperator instanceof CompoundVariation) {
//                        System.out.println(String.format("Removed: %s", ((CompoundVariation) removeOperator).getName()));
//                    } else {
//                        System.out.println(String.format("Removed: %s", operators.toString()));
//                    }
//                }
//
//                Population allSolnPop = new Population(allSolutions);
//
//                /*
//                 * Label all top 50% solutions with an integer 1 and 0 otherwise.
//                 * Then save the labeled population in labels.csv file.
//                 */
//                labeler.label(allSolnPop);
//                File savePath = new File(Paths.get(System.getProperty("tatc.output")).toString());
//                String labledDataFile = savePath + File.separator + "labels_" + operatorResetCount + ".csv";
//                System.out.println("Saving all solutions with labels...");
//                ResultIO.saveLabels(allSolnPop, labledDataFile, ",");
//
//                /*
//                 * Get all base features, run Data Mining and
//                 * save compound features in features.csv file.
//                 */
//                String featureDataFile = savePath + File.separator + "features_" + operatorResetCount + ".csv";
//
//                ArrayList<Boolean> behavioralArchitectures = new ArrayList<>();
//
//                int numberOfArchitectures = 0;
//
//                ArrayList<BitSet> architectureToFeatureBitsetList = new ArrayList<>();
//
//                File file = new File(labledDataFile);
//                try (BufferedReader br = new BufferedReader(new FileReader(file))) {
//                    String nextLine = br.readLine();
//
//                    while (nextLine != null) {
//
//                        BitSet architectureToFeatureBitset = new BitSet();
//                        boolean hasHomo = false, hasHetero = false, hasTrain = false;
//                        int typeOfConstellations = 0;
//
//                        String[] str = nextLine.split(",");
//                        if (str[0].equals("[1]")) {
//                            behavioralArchitectures.add(true);
//                        } else {
//                            behavioralArchitectures.add(false);
//                        }
//
//                        for (int i = 1; i < str.length - 1; i++) {
//                            String[] decisionsPerVariable = StringUtils.substringBetween(str[i], "[", "]").split(";");
//                            int numDecisionsPerVariable = decisionsPerVariable.length;
//
//                            /*
//                             * Add all the base features for Homogeneous Constellations
//                             * Variable---> [a;i;t;p;f]
//                             * numDecisionsPerVariable == 5
//                             */
//                            if (numDecisionsPerVariable == 5) {
//                                hasHomo = true;
//                                typeOfConstellations++;
//                                String homogeneousList = StringUtils.substringBetween(str[i], "[", "]");
//                                String[] homogeneousValues = homogeneousList.split(";");
//
//                                for (int j = 0; j < 5; j++) {
//                                    switch (j) {
//                                        case 0:
//                                            if (Double.parseDouble(homogeneousValues[j]) < 600) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomAltLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomAltHigh"), true);
//                                            }
//                                            break;
//                                        case 1:
//                                            if (homogeneousValues[j].equals("SSO")) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomIncEqualSSO"), true);
//                                                break;
//                                            }
//                                            if (Double.parseDouble(homogeneousValues[j]) < 60) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomIncLow"), true);
//                                            } else {
//                                                if (Double.parseDouble(homogeneousValues[j]) == 90) {
//                                                    architectureToFeatureBitset.set(baseFeatures.get("HomIncEqualPolar"), true);
//                                                } else {
//                                                    architectureToFeatureBitset.set(baseFeatures.get("HomIncHigh"), true);
//                                                }
//                                            }
//                                            break;
//                                        case 2:
//                                            if (Double.parseDouble(homogeneousValues[j]) < 5) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomSatLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomSatHigh"), true);
//                                            }
//                                            break;
//                                        case 3:
//                                            if (Double.parseDouble(homogeneousValues[j]) == 1) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomPlnEqualOne"), true);
//                                            } else if (Double.parseDouble(homogeneousValues[j]) == Double.parseDouble(homogeneousValues[j - 1])) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomPlnEqualN"), true);
//                                            }
//                                            if (Double.parseDouble(homogeneousValues[j]) < 4) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomPlnLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomPlnHigh"), true);
//                                            }
//                                            break;
//                                        case 4:
//                                            if (Double.parseDouble(homogeneousValues[j]) < 4) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomPhsLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HomPhsHigh"), true);
//                                            }
//                                            break;
//                                    }
//                                }
//                            }
//
//                            /*
//                             * Get all the base features for Heterogeneous Constellations
//                             * Variable---> [(a;i)(a;i)....;t]
//                             * numDecisionsPerVariable == 2
//                             */
//                            if (numDecisionsPerVariable == 2) {
//                                hasHetero = true;
//                                typeOfConstellations++;
//                                String heterogeneousList = StringUtils.substringBetween(str[i], "[", "]");
//                                String[] heterogeneousValues = heterogeneousList.split(";");
//                                for (int j = 0; j < 2; j++) {
//                                    switch (j) {
//                                        case 0:
//                                            String[] altIncPairedValues = heterogeneousValues[j].substring(1, heterogeneousValues[0].length() - 1).split("\\)\\(");
//
//                                            int numPlanes = altIncPairedValues.length;
//                                            if (numPlanes == 1) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetPlnEqualOne"), true);
//                                            }
//                                            if (numPlanes <= 3) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetPlnLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetPlnHigh"), true);
//                                            }
//
//                                            String[] altitudes = new String[altIncPairedValues.length];
//                                            String[] inclinations = new String[altIncPairedValues.length];
//                                            for (int k = 0; k < altIncPairedValues.length; k++) {
//                                                altitudes[k] = StringUtils.substringBefore(altIncPairedValues[k], ":");
//                                                inclinations[k] = StringUtils.substringAfter(altIncPairedValues[k], ":");
//                                            }
//                                            architectureToFeatureBitset.set(baseFeatures.get("HetAltEqual"), checkEqualValues(altitudes));
//                                            architectureToFeatureBitset.set(baseFeatures.get("HetIncEqual"), checkEqualValues(inclinations));
//
//                                            ArrayList<String> altitudesList = new ArrayList<>(Arrays.asList(altitudes));
//                                            if (Double.parseDouble(Collections.max(altitudesList)) < 600) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetAltLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetAltHigh"), true);
//                                            }
//
//                                            //check for polar and SSO inclinations
//                                            for (int k = 0; k < inclinations.length; k++) {
//                                                if (inclinations[k].equals("SSO")) {
//                                                    architectureToFeatureBitset.set(baseFeatures.get("HetIncEqualSSO"), true);
//                                                    inclinations = ArrayUtils.remove(inclinations, k);
//                                                    break;
//                                                }
//                                                if (Double.parseDouble(inclinations[k]) == 90) {
//                                                    architectureToFeatureBitset.set(baseFeatures.get("HetIncEqualPolar"), true);
//                                                }
//                                            }
//
//                                            ArrayList<String> inclinationsList = new ArrayList<>(Arrays.asList(inclinations));
//                                            if (Double.parseDouble(Collections.max(inclinationsList)) < 60) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetIncLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetIncHigh"), true);
//                                            }
//                                            break;
//                                        case 1:
//                                            if (Double.parseDouble(heterogeneousValues[j]) < 5) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetSatLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("HetSatHigh"), true);
//                                            }
//                                            break;
//                                    }
//                                }
//                            }
//
//                            /*
//                             * Get all the base features for Train Constellations
//                             * Variable---> [a;LTAN;DeltaLTAN;t]
//                             * numDecisionsPerVariable == 4
//                             */
//                            if (numDecisionsPerVariable == 4) {
//                                hasTrain = true;
//                                typeOfConstellations++;
//                                String trainList = StringUtils.substringBetween(str[i], "[", "]");
//                                String[] trainValues = trainList.split(";");
//                                for (int j = 0; j < 4; j++) {
//                                    switch (j) {
//                                        case 0:
//                                            if (Double.parseDouble(trainValues[j]) < 600) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("TrnAltLow"), true);
//                                            } else if (Double.parseDouble(trainValues[j]) >= 600) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("TrnAltHigh"), true);
//                                            }
//                                            break;
//                                        case 1:
//                                            double seconds = Utilities.hhmmssToSeconds(trainValues[j]);
//                                            double hours = seconds / 3600;
//                                            if (hours < 12) {
//                                                if (hours >= 5 && hours <= 7) {
//                                                    architectureToFeatureBitset.set(baseFeatures.get("TrnOrbEqualDD"), true);
//                                                } else {
//                                                    architectureToFeatureBitset.set(baseFeatures.get("TrnOrbEqualAM"), true);
//                                                }
//                                            }
//                                            if (hours >= 12) {
//                                                if (hours >= 17 && hours <= 19) {
//                                                    architectureToFeatureBitset.set(baseFeatures.get("TrnOrbEqualDD"), true);
//                                                } else {
//                                                    architectureToFeatureBitset.set(baseFeatures.get("TrnOrbEqualPM"), true);
//                                                }
//                                            }
//                                            break;
//                                        case 2:
//                                            if (Utilities.DurationToSeconds(trainValues[j]) < 900) { //if less than 15 minutes of interval
//                                                architectureToFeatureBitset.set(baseFeatures.get("TrnSpcLow"), true);
//                                            } else {
//                                                architectureToFeatureBitset.set(baseFeatures.get("TrnSpcHigh"), true);
//                                            }
//                                            break;
//                                        case 3:
//                                            if (Double.parseDouble(trainValues[j]) < 5) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("TrnSatLow"), true);
//                                            } else if (Double.parseDouble(trainValues[j]) >= 5) {
//                                                architectureToFeatureBitset.set(baseFeatures.get("TrnSatHigh"), true);
//                                            }
//                                            break;
//                                    }
//                                }
//                            }
//                        }
//
//                        //add features for hybrid constellations
//                        if (typeOfConstellations > 1) {
//                            if (hasHomo) {
//                                architectureToFeatureBitset.set(baseFeatures.get("HybHasConstelHomo"), true);
//                            }
//                            if (hasHetero) {
//                                architectureToFeatureBitset.set(baseFeatures.get("HybHasConstelHetero"), true);
//                            }
//                            if (hasTrain) {
//                                architectureToFeatureBitset.set(baseFeatures.get("HybHasConstelTrain"), true);
//                            }
//                        }
//
//                        //add architecture -> feature bitset dataset
//                        if (!architectureToFeatureBitset.isEmpty()) {
//                            architectureToFeatureBitsetList.add(architectureToFeatureBitset);
//                        }
//
//                        nextLine = br.readLine();
//                        numberOfArchitectures++;
//                    }
//                } catch (IOException ex) {
//                    Logger.getLogger(TradespaceSearchStrategyKDO.class.getName()).log(Level.SEVERE, null, ex);
//                }
//
//                //convert rows -> columns and vice versa to get feature to architecture matrix
//                ArrayList<BitSet> featuresToArchitecturesList = new ArrayList<>();
//                for (int i = 0; i < baseFeatures.size(); i++) {
//                    BitSet bitSet = new BitSet();
//                    for (int j = 0; j < architectureToFeatureBitsetList.size(); j++) {
//                        bitSet.set(j, architectureToFeatureBitsetList.get(j).get(i));
//                    }
//                    featuresToArchitecturesList.add(bitSet);
//                }
//
//                /*
//                 * Create base features using Bitset for each architecture
//                 */
//                ArrayList<DrivingFeature> drivingFeatures = new ArrayList<>();
//                for (Map.Entry<String, Integer> entry : baseFeatures.entrySet()) {
//                    String key = entry.getKey();
//                    Integer value = entry.getValue();
//                    DrivingFeature drivingFeature = new DrivingFeature(key, featuresToArchitecturesList.get(value));
//                    drivingFeatures.add(drivingFeature);
//                }
//
//                /*
//                 * This Bitset defines which architectures are behavioral and which are not.
//                 */
//                BitSet behavioralArchitecturesSet = new BitSet(behavioralArchitectures.size());
//                for (int i = 0; i < behavioralArchitectures.size(); i++) {
//                    behavioralArchitecturesSet.set(i, behavioralArchitectures.get(i));
//                }
//
//                /*
//                 * Start running the data mining algorithm
//                 */
//                AssociationRuleMining arm = new AssociationRuleMining(numberOfArchitectures, drivingFeatures);
//                arm.run(behavioralArchitecturesSet, 0.1, 0.41, 3);
//                List<DrivingFeature> topFeatures = arm.getTopFeatures(3, FeatureMetric.FCONFIDENCE);
//                if (!topFeatures.isEmpty()) {
//                    List<DrivingFeature> bestFeatures = MRMR.minRedundancyMaxRelevance(behavioralArchitectures.size(), behavioralArchitecturesSet, topFeatures, nOperRepl);
//                    ResultIO.saveFeatures(bestFeatures, featureDataFile, ",");
//                    //start the feature learning process
//                    try {
//                        operatorCreator.learnFeatures(new File(featureDataFile));
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//
//                    //start creating new operators based on features learned
//                    Collection<Variation> newOperators = operatorCreator.createOperator(nOperRepl);
//                    while (newOperators.remove(null)) ;
//
//                    for (Variation operator : newOperators) {
//                        //create Compound Variation = crossover + domain specific operator + mutation
//                        HeterogeneousWalkerVariation[] cutAndSpliceAndPairing = {new CutAndSpliceOperator(pCrossover), new PairingOperator(pCrossover)};
//                        HybridOperatorCrossover cross = new HybridOperatorCrossover(new TwoPointCrossover(pCrossover), cutAndSpliceAndPairing);
//                        HybridOperatorMutation mutation = new HybridOperatorMutation(new IntegerUM(pMutation), new HeterogeneousWalkerMutation(new IntegerUM(pMutation)), pMutation);
//                        CompoundVariation repair = new CompoundVariation(cross, operator, mutation);
//
//                        //add Compound Variation operator to the pool
//                        StringBuilder sb = new StringBuilder();
//                        sb.append(cross.getClass().getSimpleName()).append("+");
//                        sb.append(((CompoundVariation) operator).getName()).append("+");
//                        sb.append(mutation.getClass().getSimpleName());
//                        repair.setName(sb.toString());
//                        aos.getOperatorSelector().addOperator(repair);
//
//                        System.out.println("Feature { " + sb.toString() + " } added to the pool.");
//                    }
//                } else {
//                    System.out.println("No features found by data mining algorithm.");
//                }
//            }
//
//            for (Variation op : aos.getOperatorSelector().getOperators()) {
//                System.out.println(String.format("Using: %s", op.getClass().getSimpleName()));
//            }
//            aos.getOperatorSelector().reset();
//
//            aos.step();
//
//            //since new solutions are put at end of population, only check the last few to see if any new solutions entered population
//            for (int i = 1; i < 3; i++) {
//                Solution s = pop.get(pop.size() - i);
//                s.setAttribute("NFE", aos.getNumberOfEvaluations());
//                allSolutions.add(s);
//            }
//
//            //calculate metrics for search
//            qualityIndicator.calculate(aos.getResult());
//
//            Double[] metrics = new Double[2];
//            double hvValue = qualityIndicator.getHypervolume();
//            double igd = qualityIndicator.getInvertedGenerationalDistance();
//            metrics[0] = hvValue;
//            metrics[1] = igd;
//
//            System.out.println(String.format("NFE = %d, HV = %f, IGD = %f", aos.getNumberOfEvaluations(), hvValue, igd));
//
//            hypervolume.put(aos.getNumberOfEvaluations(), metrics);
//            ResultIO.saveLabels(aos.getResult(), Paths.get(System.getProperty("tatc.output"), String.format("results_%d", aos.getNumberOfEvaluations())).toString(), ",");
//        }
//        ResultIO.savePopulation(new Population(allSolutions), Paths.get(System.getProperty("tatc.output"), String.format("uniqueSolutions")).toString());
//        ResultIO.savePopulation(aos.getArchive(), Paths.get(System.getProperty("tatc.output"), String.format("nonDominatedSolutions")).toString());
//        ResultIO.saveLabels(aos.getResult(), Paths.get(System.getProperty("tatc.output"), String.format("results")).toString(), ",");
//        ResultIO.saveHyperVolume(hypervolume, Paths.get(System.getProperty("tatc.output"), String.format("hypervolume")).toString());
//        AOSHistoryIO.saveQualityHistory(aos.getQualityHistory(), new File(System.getProperty("tatc.output") + File.separator + String.format("quality.text")), ",");
//        AOSHistoryIO.saveCreditHistory(aos.getCreditHistory(), new File(System.getProperty("tatc.output") + File.separator + String.format("credit.text")), ",");
//        AOSHistoryIO.saveSelectionHistory(aos.getSelectionHistory(), new File(System.getProperty("tatc.output") + File.separator + String.format("history.text")), ",");
//        aos.terminate();
//    }
//
//    /**
//     * Checks if all the values in an array are equal.
//     *
//     * @return flag = true if all values in the array are equal, false otherwise.
//     */
//    private boolean checkEqualValues(String[] valueArray) {
//
//        boolean flag = false;
//        for (int k = 0; k < valueArray.length; k++) {
//            if (valueArray[0].equals(valueArray[k])) {
//                flag = true;
//            } else {
//                flag = false;
//                return flag;
//            }
//        }
//        return flag;
//    }
//
//    /**
//     * This method adds the names of base features to be used in data mining as Strings.
//     * The value for each key value pair is an integer which identifies each
//     * feature. This value can later be used as specific bit in the BitSet used to identify
//     * which architecture has that particular feature, if it is set, and which doesn't, if it isn't set.
//     *
//     * @return Hashmap of base features to be used in data mining
//     */
//
//    private HashMap<String, Integer> getBaseFeatures() {
//
//        HashMap<String, Integer> basefeatures = new HashMap<>();
//
//        //for Homogeneous Constellations
//        basefeatures.put("HomAltHigh", 0);
//        basefeatures.put("HomIncHigh", 1);
//        basefeatures.put("HomSatHigh", 2);
//        basefeatures.put("HomPlnHigh", 3);
//        basefeatures.put("HomPhsHigh", 4);
//        basefeatures.put("HomAltLow", 5);
//        basefeatures.put("HomIncLow", 6);
//        basefeatures.put("HomSatLow", 7);
//        basefeatures.put("HomPlnLow", 8);
//        basefeatures.put("HomPhsLow", 9);
//        basefeatures.put("HomIncEqualSSO", 10);
//        basefeatures.put("HomIncEqualPolar", 11);
//        basefeatures.put("HomPlnEqualOne", 12);
//        basefeatures.put("HomPlnEqualN", 13);
//
//        //for Heterogeneous Constellations
//        basefeatures.put("HetAltHigh", 14);
//        basefeatures.put("HetIncHigh", 15);
//        basefeatures.put("HetSatHigh", 16);
//        basefeatures.put("HetPlnHigh", 17);
//        basefeatures.put("HetAltLow", 18);
//        basefeatures.put("HetIncLow", 19);
//        basefeatures.put("HetSatLow", 20);
//        basefeatures.put("HetPlnLow", 21);
//        basefeatures.put("HetAltEqual", 22);
//        basefeatures.put("HetIncEqual", 23);
//        basefeatures.put("HetPlnEqualOne", 24);
//        basefeatures.put("HetIncEqualSSO", 25);
//        basefeatures.put("HetIncEqualPolar", 26);
//
//        //for Train Constellations
//        basefeatures.put("TrnAltHigh", 27);
//        basefeatures.put("TrnSatHigh", 28);
//        basefeatures.put("TrnSpcHigh", 29);
//        basefeatures.put("TrnAltLow", 30);
//        basefeatures.put("TrnSatLow", 31);
//        basefeatures.put("TrnSpcLow", 32);
//        basefeatures.put("TrnOrbEqualAM", 33);
//        basefeatures.put("TrnOrbEqualPM", 34);
//        basefeatures.put("TrnOrbEqualDD", 35);
//
//        //for Hybrid Constellations
//        basefeatures.put("HybHasConstelHomo", 36);
//        basefeatures.put("HybHasConstelHetero", 37);
//        basefeatures.put("HybHasConstelTrain", 38);
//
//        return basefeatures;
//    }
//
//    @Override
//    public void validate() {
//        GUIInterface gui = new GUIInterface();
//
//        if (epsilon < 0 || epsilon > 1) {
//            gui.sendResponses("GUIURL", "urlparams", "Epsilon values must lie between 0 and 1.");
//        }
//        if (pCrossover < 0 || pCrossover > 1) {
//            gui.sendResponses("GUIURL", "urlparams", "Probability of crossover must lie between 0 and 1.");
//        }
//        if (pMutation < 0 || pMutation > 1) {
//            gui.sendResponses("GUIURL", "urlparams", "Probability of mutation must lie between 0 and 1.");
//        } else {
//            gui.sendResponses("GUIURL", "urlparams", "Validation complete. Search has been initiated....");
//        }
//    }
//}
