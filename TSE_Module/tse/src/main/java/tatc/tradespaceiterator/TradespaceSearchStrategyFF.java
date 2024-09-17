package tatc.tradespaceiterator;

import tatc.ResultIO;
import tatc.architecture.constellations.*;
import tatc.architecture.ArchitectureCreator;
import tatc.architecture.specifications.GroundNetwork;
import tatc.architecture.specifications.Satellite;
import tatc.architecture.variable.Decision;
import tatc.util.AbsoluteDate;
import tatc.util.Combinatorics;
import tatc.util.Enumeration;
import tatc.util.Utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Full factorial search strategy. It enumerates all possible architectures contained in the design space and
 * evaluates them one by one.
 */
public class TradespaceSearchStrategyFF implements TradespaceSearchStrategy {
    /**
     * The problem properties
     */
    ProblemProperties properties;

    /**
     * The evaluated architecture counter
     */
    private int counter = 0;

    /**
     * Constructs a full factorial search strategy
     * @param properties the problem properties
     */
    public TradespaceSearchStrategyFF(ProblemProperties properties){
        this.properties=properties;
    }

    @SuppressWarnings("unchecked")
    public void start() throws IllegalArgumentException{
        HashMap<String,Decision<?>> decisions = properties.getDecisions();
        List<List<ConstellationParameters>> listConstellationParmeters = new ArrayList<>();
        for (int constellationCount = 0; constellationCount<properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().size(); constellationCount++){
            if (!properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).getConstellationType().equalsIgnoreCase("EXISTING")){
                double eccentricity = -1;
                switch (properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).getConstellationType()){
                    case "DELTA_HOMOGENEOUS":
                        eccentricity = properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).getOrbit().get(0).getEccentricity();
                        ArrayList<ConstellationParameters> constellationParamsHomo = Enumeration.fullFactHomogeneousWalker(((Decision<Double>) decisions.get(String.format("HomoAltitude%d",constellationCount))).getAllowedValues(),
                                ((Decision<Object>)decisions.get(String.format("HomoInclination%d",constellationCount))).getAllowedValues(),
                                ((Decision<Integer>)decisions.get(String.format("HomoNumberSatellites%d",constellationCount))).getAllowedValues(),
                                ((Decision<Integer>)decisions.get(String.format("HomoNumberPlanes%d",constellationCount))).getAllowedValues(),
                                ((Decision<Integer>)decisions.get(String.format("HomoRelativeSpacing%d",constellationCount))).getAllowedValues(),
                                ((Decision<Satellite>)decisions.get(String.format("HomoSatellite%d",constellationCount))).getAllowedValues(),
                                eccentricity);
                        for (ConstellationParameters params : constellationParamsHomo){
                            params.setSecondaryPayload(properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).isSecondaryPayload());
                            params.setEccentricity(eccentricity);
                        }
                        listConstellationParmeters.add(constellationParamsHomo);
                        break;
                    case "DELTA_HETEROGENEOUS":
                        eccentricity = properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).getOrbit().get(0).getEccentricity();
                        ArrayList<ConstellationParameters> constellationParamsHet = Enumeration.fullFactHeterogeneousWalker(((Decision<Double>) decisions.get(String.format("HeteroAltitude%d",constellationCount))).getAllowedValues(),
                                ((Decision<Object>)decisions.get(String.format("HeteroInclination%d",constellationCount))).getAllowedValues(),
                                ((Decision<Integer>)decisions.get(String.format("HeteroNumberSatellites%d",constellationCount))).getAllowedValues(),
                                ((Decision<Integer>)decisions.get(String.format("HeteroNumberPlanes%d",constellationCount))).getAllowedValues(),
                                ((Decision<Integer>)decisions.get(String.format("HeteroRelativeSpacing%d",constellationCount))).getAllowedValues(),
                                ((Decision<Satellite>)decisions.get(String.format("HeteroSatellite%d",constellationCount))).getAllowedValues(),
                                eccentricity);
                        for (ConstellationParameters params : constellationParamsHet){
                            params.setSecondaryPayload(properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).isSecondaryPayload());
                            params.setEccentricity(eccentricity);
                        }
                        listConstellationParmeters.add(constellationParamsHet);
                        break;
                    case "TRAIN":
                        ArrayList<ConstellationParameters> constellationParamsTrain = Enumeration.fullFactTrain(((Decision<Double>) decisions.get(String.format("TrainAltitude%d",constellationCount))).getAllowedValues(),
                                ((Decision<Integer>)decisions.get(String.format("TrainNumberSatellites%d",constellationCount))).getAllowedValues(),
                                ((Decision<String>)decisions.get(String.format("TrainLTANs%d",constellationCount))).getAllowedValues(),
                                ((Decision<String>)decisions.get(String.format("TrainSatelliteInterval%d",constellationCount))).getAllowedValues(),
                                ((Decision<Satellite>)decisions.get(String.format("TrainSatellite%d",constellationCount))).getAllowedValues());
                        for (ConstellationParameters params : constellationParamsTrain){
                            params.setSecondaryPayload(properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).isSecondaryPayload());
                            params.setEccentricity(properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).getOrbit().get(0).getEccentricity());
                        }
                        listConstellationParmeters.add(constellationParamsTrain);
                        break;
                    case "AD_HOC":
                        ArrayList<ConstellationParameters> constellationParamsAdHoc = Enumeration.fullFactAdHoc(((Decision<Integer>)decisions.get(String.format("AdhocNumberSatellites%d",constellationCount))).getAllowedValues(),
                                ((Decision<Satellite>)decisions.get(String.format("AdhocSatellite%d",constellationCount))).getAllowedValues());
                        for (ConstellationParameters params : constellationParamsAdHoc){
                            params.setSecondaryPayload(properties.getTradespaceSearch().getDesignSpace().getSpaceSegment().get(constellationCount).isSecondaryPayload());
                        }
                        listConstellationParmeters.add(constellationParamsAdHoc);
                        break;
                    default:
                        throw new IllegalArgumentException("Constellation type has to be either DELTA_HOMOGENEOUS, DELTA_HETEROGENOUS, TRAIN OR AD_HOC.");
                }
            }


        }

        ResultIO.createSummaryFile(new File(System.getProperty("tatc.output") + File.separator + "summary.csv"),0);
        List<List<ConstellationParameters>> cartesianProduct = Combinatorics.cartesianProduct(listConstellationParmeters);
        Decision<GroundNetwork> decisionGroundNetwork = (Decision<GroundNetwork>)decisions.get("groundNetwork");
        String epoch = properties.getTradespaceSearch().getMission().getStart();
        for (GroundNetwork gn : decisionGroundNetwork.getAllowedValues()) {
            for (List<ConstellationParameters> constellationParams : cartesianProduct) {
                ArchitectureCreator architecture = new ArchitectureCreator();
                architecture.addGroundNetwork(gn);
                for (ConstellationParameters constellation : constellationParams) {
                    switch (constellation.getType()) {
                        case "DELTA_HOMOGENEOUS":
                            if (((HomogeneousWalkerParameters) constellation).getT() != 0) {
                                architecture.addHomogeneousWalker(((HomogeneousWalkerParameters) constellation).getA()+ Utilities.EARTH_RADIUS_KM,
                                        ((HomogeneousWalkerParameters) constellation).getI(), ((HomogeneousWalkerParameters) constellation).getT(),
                                        ((HomogeneousWalkerParameters) constellation).getP(), ((HomogeneousWalkerParameters) constellation).getF(),
                                        ((HomogeneousWalkerParameters) constellation).getSatellite(), constellation.getSecondaryPayload(), epoch,
                                        constellation.getEccentricity());
                            }
                            break;
                        case "DELTA_HETEROGENEOUS":
                            if (((HeterogeneousWalkerParameters) constellation).getT() != 0) {
                                architecture.addHeterogenousWalker(((HeterogeneousWalkerParameters) constellation).getT(),
                                        ((HeterogeneousWalkerParameters) constellation).getPlanes(),
                                        ((HeterogeneousWalkerParameters) constellation).getSatellite(), constellation.getSecondaryPayload(), epoch,
                                        constellation.getEccentricity());
                            }
                            break;
                        case "TRAIN":
                            if (((TrainParameters) constellation).getNsat() != 0) {
                                AbsoluteDate startDate = Utilities.DateTimeToAbsoluteDate(properties.getTradespaceSearch().getMission().getStart());
                                architecture.addTrain(startDate,((TrainParameters) constellation).getA(),
                                        ((TrainParameters) constellation).getNsat(),
                                        ((TrainParameters) constellation).getLTANref(),
                                        ((TrainParameters) constellation).getSatelliteInterval(),
                                        ((TrainParameters) constellation).getSatellite(), constellation.getSecondaryPayload(), epoch,
                                        constellation.getEccentricity());
                            }
                            break;
                        case "AD_HOC":
                            if (((AdHocParameters) constellation).getNsat() != 0) {
                                architecture.addAdHoc(((AdHocParameters) constellation).getNsat(),
                                        ((AdHocParameters) constellation).getSatellite(), constellation.getSecondaryPayload(), epoch);
                            }
                            break;
                        default:
                            throw new IllegalArgumentException("Constellation type has to be either DELTA_HOMOGENEOUS, DELTA_HETEROGENOUS, TRAIN OR AD_HOC.");
                    }
                }
                if (!architecture.getConstellations().isEmpty()){
                    // create the Architecture JSON file
                    File architectureJsonFile = architecture.toJSON(this.getCounter());
                    // Evaluate architecture
                    long startTime = System.nanoTime();
                    try{
                        TradespaceSearchExecutive.evaluateArchitecture(architectureJsonFile, properties);
                    }catch(IOException e){
                    System.out.println("Error reading the JSON file: " + e.getMessage());
                    e.printStackTrace();

                    };      
                    long endTime = System.nanoTime();
                    double execTime = (endTime - startTime) / Math.pow(10, 9);
                    // Add line in summaryData
                    ResultIO.addSummaryLine(new File(System.getProperty("tatc.output") + File.separator + "summary.csv"),ResultIO.getLineSummaryData(architecture,this.getCounter(),execTime));
                    // increment the counter at each architecture evaluation
                    this.incrementCounter();
                }
            }
        }
    }

    /**
     * Gets the number of architectures evaluated
     * @return the number of architectures evaluated
     */
    public int getCounter() {
        return counter;
    }

    /**
     * Increments by 1 unit the counter of architectures evaluated
     */
    private void incrementCounter(){
        this.counter++;
    }

}
