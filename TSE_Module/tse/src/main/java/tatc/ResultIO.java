/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc;

/**
 * Class required for input and output operations
 * @author Prachi
 */

import org.moeaframework.core.Population;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Solution;
import tatc.architecture.ArchitectureCreator;
import tatc.architecture.outputspecifications.CostRisk;
import tatc.architecture.outputspecifications.Gbl;
import tatc.architecture.outputspecifications.ValueOutput;
import tatc.architecture.specifications.Constellation;
import tatc.architecture.variable.*;
import tatc.tradespaceiterator.search.DrivingFeature;
import tatc.tradespaceiterator.search.PopulationLabeler;
import tatc.util.JSONIO;
import tatc.util.TLESatellite;
import tatc.util.Utilities;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResultIO implements Serializable {

    /**
     * Saves the measured hypervolume at NFE in a csv file
     *
     * @param  searchMetrics lists the algorithm hypervolume at NFE
     * @param filename filename including the path
     */
    public static void saveHyperVolume(HashMap<Integer, Double[]> searchMetrics, String filename) {

        File results = new File(filename + ".csv");
        System.out.println("Saving performance metrics");


        try (FileWriter writer = new FileWriter(results)) {
            //write the headers
            writer.append("NFE");
            writer.append(",");
            writer.append("HV");
            writer.append(",");
            writer.append("IGD");
            writer.append("\n");

            Set<Integer> keys = searchMetrics.keySet();
            Iterator<Integer> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                Integer key = keyIterator.next();
                writer.append(key.toString()).append(",");
                writer.append(searchMetrics.get(key)[0].toString());
                writer.append(key.toString()).append(",");
                writer.append(searchMetrics.get(key)[1].toString());
                writer.append("\n");
            }
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saves only the objective values of the solutions in the population
     *
     * @param pop the population
     * @param filename the filename
     */
    public static void saveObjectives(Population pop, String filename) {
        System.out.println("Saving objectives");

        try {
            PopulationIO.writeObjectives(new File(filename + ".txt"), pop);
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saves the objective values, decision values, and attributes of the
     * solutions in the population
     *
     * @param pop the population
     * @param filename the filename
     * @return true if the file has been saved successfully and false otherwise
     */
    public static boolean saveSearchResults(Population pop, String filename) {
        if (pop.isEmpty()) {
            return false;
        }
        System.out.println("Saving search results");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename + ".res")))) {
            //write the headers
            for (int i = 0; i < pop.get(0).getNumberOfObjectives(); i++) {
                bw.append(String.format("obj", i));
                bw.append(" ");
            }
            for (int i = 0; i < pop.get(0).getNumberOfVariables(); i++) {
                bw.append(String.format("dec", i));
                bw.append(" ");
            }
            Set<String> attrSet = pop.get(0).getAttributes().keySet();
            for (String attr : attrSet) {
                bw.append(attr + " ");
            }
            bw.newLine();

            //record values for each solution
            for (Solution soln : pop) {
                for (int i = 0; i < soln.getNumberOfObjectives(); i++) {
                    bw.append(String.valueOf(soln.getObjective(i)));
                    bw.append(" ");
                }
                for (int i = 0; i < soln.getNumberOfVariables(); i++) {
                    bw.append(soln.getVariable(i).toString());
                    bw.append(" ");
                }
                for (String attr : attrSet) {
                    bw.append(String.valueOf((soln.getAttribute(attr))));
                    bw.append(" ");
                }
                bw.newLine();
            }
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Reads a set of objective vectors from the specified file. Files read
     * using this method should only have been created using the
     * {@code saveObjectives} method.
     *
     * @param file the file containing the objective vectors
     * @return a population containing all objective vectors in the specified
     * file
     * @throws IOException if an I/O exception occurred
     */
    public static Population readObjectives(File file) throws IOException {
        return PopulationIO.readObjectives(file);
    }

    /**
     * Writes a collection of solutions to the specified file. This saves all
     * the explanations as well as any computed objectives Files written using
     * this method should only be read using the method. This method relies on
     * serialization.
     *
     * @param pop      the solutions to be written in the specified file
     * @param filename the filename including the path to which the solutions
     *                 are written
     */
    public static void savePopulation(Population pop, String filename) {
        System.out.println("Saving population");

        try {
            PopulationIO.write(new File(filename + ".pop"), pop);
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads a population from the specified file. Files read using this method
     * should only have been created using the method. This method relies on
     * serialization.
     *
     * @param filename the filename including the path to which the solutions
     *                 are written
     * @return a population containing all solutions in the specified file
     * @throws IOException if an I/O exception occurred
     */
    public static Population loadPopulation(String filename) throws IOException {
        return PopulationIO.read(new File(filename));
    }


    /**
     * This method will save the label of each individual stored in the
     * population to a dlm file with a user specified separator. Only
     * individuals with a label attribute will be saved. In addition to the
     * label, the decision values and objective values will be saved in the file
     * as well. If the population is empty, this method does not attempt to save
     * to any file and returns false.
     *
     * @param population the population
     * @param filename the filename
     * @param separator the user specified separator
     * @return True if a file is successfully saved. Else false.
     */
    public static boolean saveLabels(Population population, String filename, String separator) {

        if (population.isEmpty()) {
            return false;
        }
        //Only try saving populations that are not empty
        try (FileWriter fw = new FileWriter(new File(filename))) {

            //Write decision information of each individual
            for (int i = 0; i < population.size(); i++) {

                //Writes labels
                if (population.get(i).hasAttribute(PopulationLabeler.LABELATTRIB)) {
                    fw.append("[" + population.get(i).getAttribute(PopulationLabeler.LABELATTRIB) + "]" + separator);
                }

                for (int j = 0; j < population.get(i).getNumberOfVariables(); j++) {

                    //Writes Ground Network decisions
                    if (population.get(i).getVariable(j) instanceof GroundNetworkVariable) {
                        GroundNetworkVariable variable = (GroundNetworkVariable) population.get(i).getVariable(j);
                        fw.append("[");
                        for (int k = 0; k < variable.getGroundNetwork().getGroundStations().size(); k++) {
                            fw.append(variable.getGroundNetwork().getGroundStations().get(k).get_id());
                            if (k == variable.getGroundNetwork().getGroundStations().size() - 1) {
                                fw.append("]");
                            } else {
                                fw.append(";");
                            }
                        }
                        fw.append(separator);
                    }

                    //Writes Homogeneous Walker decisions
                    if (population.get(i).getVariable(j) instanceof HomogeneousWalkerVariable) {
                        HomogeneousWalkerVariable variable = (HomogeneousWalkerVariable) population.get(i).getVariable(j);
                        int plane = 0;
                        int phase = 0;
                        int sats = variable.getT();
                        double planes = variable.getpReal();
                        double phasing = variable.getfReal();
                        if (sats != 0) {
                            List<Integer> tradespacePlanes = variable.getpAllowed();
                            ArrayList<Integer> planeAndPhase = Utilities.obtainPlanesAndPhasingFromChromosome(sats, tradespacePlanes, planes, phasing);
                            plane = planeAndPhase.get(0);
                            phase = planeAndPhase.get(1);
                        }
                        fw.append("[");
                        fw.append(variable.getAlt().toString() + ";");
                        fw.append(variable.getInc().toString() + ";");
                        fw.append(variable.getT().toString() + ";");
                        fw.append(String.valueOf(plane) + ";");
                        fw.append(String.valueOf(phase));
                        fw.append("]");
                        fw.append(separator);
                    }

                    //Writes Heterogeneous Walker decisions
                    if (population.get(i).getVariable(j) instanceof HeterogeneousWalkerVariable) {
                        HeterogeneousWalkerVariable variable = (HeterogeneousWalkerVariable) population.get(i).getVariable(j);
                        fw.append("[");
                        int planes = variable.getNumberOfPlanes();
                        for (int k = 0; k < planes; k++) {
                            fw.append("(");
                            fw.append(variable.getPlaneVariables().get(k).getAlt().toString() + ":");
                            fw.append(variable.getPlaneVariables().get(k).getInc().toString());
                            fw.append(")");
                        }
                        fw.append(";" + variable.getT().toString());
                        fw.append("]");
                        fw.append(separator);
                    }

                    //Writes Train decisions
                    if (population.get(i).getVariable(j) instanceof TrainVariable) {
                        TrainVariable variable = (TrainVariable) population.get(i).getVariable(j);
                        fw.append("[");
                        fw.append(variable.getAlt().toString() + ";");
                        fw.append(variable.getLTAN() + ";");
                        fw.append(variable.getSatInterval() + ";");
                        fw.append(variable.getT().toString());
                        fw.append("]");
                        fw.append(separator);
                    }
                }

                //Write objective information of each individual
                int numObj = population.get(i).getNumberOfObjectives();
                fw.append("[");
                for (int j = 0; j < numObj; j++) {
                    if (j == numObj - 1) {
                        fw.append(String.format("%f]\n", population.get(i).getObjective(j)));
                    } else {
                        fw.append(String.format("%f;", population.get(i).getObjective(j)));
                    }
                }
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    /**
     * This method will save the top features extracted from Apriori algorithm
     * to a dlm file with a user specified separator. If the population is
     * empty, this method does not attempt to save to any file and returns false
     *
     * @param features the top features extracted from the a priori algorithm
     * @param filename the filename
     * @param separator the user specified separator
     * @return True if a file is successfully saved. Else false.
     */
    public static boolean saveFeatures(List<DrivingFeature> features, String filename, String separator) {
        if (features.isEmpty()) {
            return false;
        }

        //Only try saving populations that are not empty
        try (FileWriter fw = new FileWriter(new File(filename))) {

            //write the header
            fw.append(String.format("ID,Name,Support,FConfidence,RConfidence,Lift"));
            fw.append("\n");

            //write feature
            for (int i = 0; i < features.size(); i++) {
                DrivingFeature thisFeature = features.get(i);
                fw.append(String.format("%d%s", i, separator));
                fw.append(String.format("%s%s", thisFeature.getName(), separator));
                fw.append(String.format("%f%s", thisFeature.getSupport(), separator));
                fw.append(String.format("%f%s", thisFeature.getFConfidence(), separator));
                fw.append(String.format("%f%s", thisFeature.getRConfidence(), separator));
                fw.append(String.format("%f%s", thisFeature.getLift(), separator));

                fw.append("\n");
            }
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }

    public static double readOutput(String outputName, int archId){
        double value;
        if (outputName.equals("MinTime") || outputName.equals("MaxTime")
                || outputName.equals("MinTimeToCoverage") || outputName.equals("MaxTimeToCoverage") || outputName.equals("MeanTimeToCoverage")
                || outputName.equals("MinAccessTime") || outputName.equals("MaxAccessTime") || outputName.equals("MeanAccessTime")
                || outputName.equals("MinRevisitTime") || outputName.equals("MaxRevisitTime") || outputName.equals("MeanRevisitTime")
                || outputName.equals("MinResponseTime") || outputName.equals("MaxResponseTime") || outputName.equals("MeanResponseTime")
                || outputName.equals("Coverage")
                || outputName.equals("MinNumOfPOIpasses") || outputName.equals("MaxNumOfPOIpasses") || outputName.equals("MeanNumOfPOIpasses")
                || outputName.equals("MinDataLatency") || outputName.equals("MaxDataLatency") || outputName.equals("MeanDataLatency")
                || outputName.equals("NumGSpassesPD")
                || outputName.equals("TotalDownlinkTimePD")
                || outputName.equals("MinDownlinkTimePerPass") || outputName.equals("MaxDownlinkTimePerPass") || outputName.equals("MeanDownlinkTimePerPass") ) {
            File gbl = new File(System.getProperty("tatc.output"),
                    "arch-" + archId + File.separator + "gbl.json");
            value = readReductionMetricsObjective(gbl,outputName);
        }else if (outputName.equals("groundCost") || outputName.equals("hardwareCost")
                || outputName.equals("iatCost") || outputName.equals("launchCost")
                || outputName.equals("lifecycleCost") || outputName.equals("nonRecurringCost")
                || outputName.equals("operationsCost") || outputName.equals("programCost")
                || outputName.equals("recurringCost") ) {
            File costrisk = new File(System.getProperty("tatc.output"),
                    "arch-" + archId + File.separator + "CostRisk_Output.json");
            value = readCostRiskObjective(costrisk,outputName);
        }else if(outputName.equals("Mean of Mean of Incidence angle [deg]") || outputName.equals("SD of Mean of Incidence angle [deg]")
                || outputName.equals("Mean of SD of Incidence angle [deg]") || outputName.equals("Mean of Mean of Look angle [deg]")
                || outputName.equals("SD of Mean of Look angle [deg]") || outputName.equals("Mean of SD of Look angle [deg]")
                || outputName.equals("Mean of Mean of Observation Range [km]") || outputName.equals("SD of Mean of Observation Range [km]")
                || outputName.equals("Mean of SD of Observation Range [km]") || outputName.equals("Mean of Mean of Noise-Equivalent delta T [K]")
                || outputName.equals("SD of Mean of Noise-Equivalent delta T [K]") || outputName.equals("Mean of SD of Noise-Equivalent delta T [K]")
                || outputName.equals("Mean of Mean of DR") || outputName.equals("SD of Mean of DR")
                || outputName.equals("Mean of SD of DR") || outputName.equals("Mean of Mean of SNR")
                || outputName.equals("SD of Mean of SNR") || outputName.equals("Mean of SD of SNR")
                || outputName.equals("Mean of Mean of Ground Pixel Along-Track Resolution [m]") || outputName.equals("SD of Mean of Ground Pixel Along-Track Resolution [m]")
                || outputName.equals("Mean of SD of Ground Pixel Along-Track Resolution [m]") || outputName.equals("Mean of Mean of Ground Pixel Cross-Track Resolution [m]")
                || outputName.equals("SD of Mean of Ground Pixel Cross-Track Resolution [m]") || outputName.equals("Mean of SD of Ground Pixel Cross-Track Resolution [m]")
                || outputName.equals("Mean of Mean of Swath-Width [m]") || outputName.equals("SD of Mean of Swath-Width [m]")
                || outputName.equals("Mean of SD of Swath-Width [m]") || outputName.equals("Mean of Mean of Sigma NEZ Nought [dB]")
                || outputName.equals("SD of Mean of Sigma NEZ Nought [dB]") || outputName.equals("Mean of SD of Sigma NEZ Nought [dB]")){
            File instrumentLevel2Metrics = new File(System.getProperty("tatc.output"),
                    "arch-" + archId + File.separator + "level2_data_metrics.csv");
            value = readLevel2MetricsInstrumentModule(instrumentLevel2Metrics,outputName);

        }else if(outputName.equals("Total Architecture Value [Mbits]") || outputName.equals("Total Lifecycle Cost [$M]")
                || outputName.equals("Ratio of Value to Cost [Mbits/$M]") || outputName.equals("Total Data Collected [Mbits]")
                || outputName.equals("EDA Scaling Factor") || outputName.equals("Average GP Resolution [km^2]")
                || outputName.equals("Average GP Resolution [m^2]") || outputName.equals("Number of Satellites")){
            File valueOutputFile = new File(System.getProperty("tatc.output"),
                    "arch-" + archId + File.separator + "value_output.json");
            value = readValueObjective(valueOutputFile,outputName);
        } else {
            throw new IllegalArgumentException("Objective name defined in TradespaceSearch.json is not valid.");
        }

        return value;
    }

    /**
     * Reads the objective value given the path of the gbl.json file and the objective name to retrieve from that file.
     * Objective names are built the following way. For a metric contained in the gbl.json file with name "MetricName",
     * "MaxMetricName" obtains the maximum value, "MinMetricName" obtains the minimum value, and "MeanMetricName" obtains
     * the average value
     * @param file the file containing the gbl.json
     * @param objectiveName the objective name
     * @return the value of the objective
     */
    public static double readReductionMetricsObjective(File file, String objectiveName) {
        //TODO: Where results are actually read from the output json files
        Gbl glb = JSONIO.readJSON( file, Gbl.class);
        if (objectiveName.equals("MinTime")){
            return glb.getTime().getMin();
        }else if (objectiveName.equals("MaxTime")){
            return glb.getTime().getMax();
        }else if (objectiveName.equals("MinTimeToCoverage")){
            return glb.getTimeToCoverage().getMin();
        }else if (objectiveName.equals("MaxTimeToCoverage")){
            return glb.getTimeToCoverage().getMax();
        }else if (objectiveName.equals("MeanTimeToCoverage")){
            return glb.getTimeToCoverage().getAvg();
        }else if (objectiveName.equals("MinAccessTime")){
            return glb.getAccessTime().getMin();
        }else if (objectiveName.equals("MaxAccessTime")){
            return glb.getAccessTime().getMax();
        }else if (objectiveName.equals("MeanAccessTime")){
            return glb.getAccessTime().getAvg();
        }else if (objectiveName.equals("MinRevisitTime")){
            return glb.getRevisitTime().getMin();
        }else if (objectiveName.equals("MaxRevisitTime")){
            return glb.getRevisitTime().getMax();
        }else if (objectiveName.equals("MeanRevisitTime")){
            return glb.getRevisitTime().getAvg();
        }else if (objectiveName.equals("MinResponseTime")){
            return glb.getResponseTime().getMin();
        }else if (objectiveName.equals("MaxResponseTime")){
            return glb.getResponseTime().getMax();
        }else if (objectiveName.equals("MeanResponseTime")){
            return glb.getResponseTime().getAvg();
        }else if (objectiveName.equals("Coverage")){
            return glb.getCoverage();
        }else if (objectiveName.equals("MinNumOfPOIpasses")){
            return glb.getNumOfPOIpasses().getMin();
        }else if (objectiveName.equals("MaxNumOfPOIpasses")){
            return glb.getNumOfPOIpasses().getMax();
        }else if (objectiveName.equals("MeanNumOfPOIpasses")){
            return glb.getNumOfPOIpasses().getAvg();
        }else if (objectiveName.equals("MinDataLatency")){
            return glb.getDataLatency().getMin();
        }else if (objectiveName.equals("MaxDataLatency")){
            return glb.getDataLatency().getMax();
        }else if (objectiveName.equals("MeanDataLatency")){
            return glb.getDataLatency().getAvg();
        }else if (objectiveName.equals("NumGSpassesPD")){
            return glb.getNumGSpassesPD();
        }else if (objectiveName.equals("TotalDownlinkTimePD")){
            return glb.getTotalDownlinkTimePD();
        }else if (objectiveName.equals("MinDownlinkTimePerPass")){
            return glb.getDownlinkTimePerPass().getMin();
        }else if (objectiveName.equals("MaxDownlinkTimePerPass")){
            return glb.getDownlinkTimePerPass().getMax();
        }else if (objectiveName.equals("MeanDownlinkTimePerPass")){
            return glb.getDownlinkTimePerPass().getAvg();
        }else{
            throw new IllegalArgumentException("Objective name not found in glb.json");
        }
    }

    /**
     * Reads the objective value given the path of the CostRisk_Output.json file and the objective name to retrieve
     * from that file. Objective names match the keys in that json file.
     * @param file the file containing the CostRisk_Output.json
     * @param objectiveName the objective name
     * @return the value of the objective
     */
    public static double readCostRiskObjective(File file, String objectiveName) {
        CostRisk costRisk = JSONIO.readJSON( file, CostRisk.class);
        if (objectiveName.equals("groundCost")){
            return costRisk.getGroundCost().getEstimate();
        }else if (objectiveName.equals("hardwareCost")){
            return costRisk.getHardwareCost().getEstimate();
        }else if (objectiveName.equals("iatCost")){
            return costRisk.getIatCost().getEstimate();
        }else if (objectiveName.equals("launchCost")){
            return costRisk.getLaunchCost().getEstimate();
        }else if (objectiveName.equals("lifecycleCost")){
            return costRisk.getLifecycleCost().getEstimate();
        }else if (objectiveName.equals("nonRecurringCost")){
            return costRisk.getNonRecurringCost().getEstimate();
        }else if (objectiveName.equals("operationsCost")){
            return costRisk.getOperationsCost().getEstimate();
        }else if (objectiveName.equals("programCost")){
            return costRisk.getProgramCost().getEstimate();
        }else if (objectiveName.equals("recurringCost")){
            return costRisk.getRecurringCost().getEstimate();
        }else{
            throw new IllegalArgumentException("Objective name not found in glb.json");
        }
    }

    /**
     * Reads the objective value given the path of the value_output.json file and the objective name to retrieve
     * from that file. Objective names match the keys in that json file.
     * @param file the file containing the value_output.json
     * @param objectiveName the objective name
     * @return the value of the objective
     */
    public static double readValueObjective(File file, String objectiveName) {
        ValueOutput valueOutput = JSONIO.readJSON( file, ValueOutput.class);
        if (objectiveName.equals("Total Architecture Value [Mbits]")){
            return Double.valueOf(valueOutput.getTotalArchitectureValue());
        }else if (objectiveName.equals("Total Lifecycle Cost [$M]")){
            return Double.valueOf(valueOutput.getTotalLifecycleCost());
        }else if (objectiveName.equals("Ratio of Value to Cost [Mbits/$M]")){
            return Double.valueOf(valueOutput.getRatioOfValueToCost());
        }else if (objectiveName.equals("Total Data Collected [Mbits]")){
            return Double.valueOf(valueOutput.getTotalDataCollected());
        }else if (objectiveName.equals("EDA Scaling Factor")){
            return Double.valueOf(valueOutput.getEDAScalingFactor());
        }else if (objectiveName.equals("Average GP Resolution [km^2]")){
            return Double.valueOf(valueOutput.getAverageGPResolution());
        }else if (objectiveName.equals("Average GP Resolution [m^2]")){
            return Double.valueOf(valueOutput.getAverageGPResolution());
        }else if (objectiveName.equals("Number of Satellites")){
            return valueOutput.getNumberOfSatellites();
        }else{
            throw new IllegalArgumentException("Objective name not found in value_output.json");
        }
    }

    /**
     * Reads the objective value given the path of the level2_data_metrics.csv file and the objective name to retrieve
     * from that file. Objective names match the column names in the csv file.
     * @param file the file containing the level2_data_metrics.csv
     * @param objectiveName the objective name
     * @return the value of the objective
     */
    public static double readLevel2MetricsInstrumentModule(File file, String objectiveName){
        HashMap<String, Double> mapMetrics = new HashMap<>();
        List<List<String>> records = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                records.add(Arrays.asList(values));
            }
            for (int i=0; i<records.get(0).size(); i++){
                if (!records.get(1).get(i).equalsIgnoreCase("nan")){
                    mapMetrics.put(records.get(0).get(i),Double.valueOf(records.get(1).get(i)));
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mapMetrics.get(objectiveName);
    }

    /**
     * Creates the summary file with its header
     * @param file the path to create the summary file
     * @param numberObjectives the number of objectives in the search (0 if FF)
     * @return true if the creation of the summary file was successful and false otherwise
     */
    public static boolean createSummaryFile(File file, int numberObjectives) {

        try (FileWriter fw = new FileWriter(file)) {
            //write the header
            fw.append(String.format("arch_id,constellation_type,num_satellites,num_planes,sat_ids,num_stations,gs_ids,exec_time[s]"));
            for (int i=1; i<=numberObjectives; i++){
                fw.append(String.format(",objective_%d",i));
            }
            fw.append("\n");
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Adds a line to the summary file
     * @param file the summary file path
     * @param line the line to add
     * @return true if the line has been added successfully and false otherwise
     */
    public static boolean addSummaryLine(File file, String line) {
        try (FileWriter fw = new FileWriter(file,true)) {
            //add line
            fw.append(line);
            fw.append("\n");
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Creates the line of the summary file for a given architecture, its id number and execution time
     * @param architecture the architecture creator object
     * @param archCounter the architecture id number
     * @param execTime the computational time needed to evaluate the architecture
     * @return the summary line for the architecture
     */
    public static String getLineSummaryData(ArchitectureCreator architecture, int archCounter,double execTime){
        String str = "arch-"+archCounter+",";
        for (int i=0; i<architecture.getConstellations().size(); i++){
            Constellation c = architecture.getConstellations().get(i);
            if (i==architecture.getConstellations().size()-1){
                str = str + c.getConstellationType()+",";
            }else{
                str = str + c.getConstellationType()+" ";
            }
        }

        int numSat=0;
        int numPlanes=0;
        for (Constellation c : architecture.getConstellations()){
            numSat = numSat + c.getSatellites().size();
            if (c.getNumberPlanes()!=null){
                numPlanes = numPlanes + (Integer) c.getNumberPlanes();
            }
        }
        str = str + numSat + ",";
        str = str + numPlanes + ",";

        for (int j=0; j<architecture.getConstellations().size(); j++){
            Constellation c = architecture.getConstellations().get(j);
            for (int i=0; i<c.getSatellites().size(); i++){
                if (j==architecture.getConstellations().size()-1){
                    if (i==c.getSatellites().size()-1){
                        str = str + c.getSatellites().get(i).get_id()+",";
                    }else{
                        str = str + c.getSatellites().get(i).get_id()+" ";
                    }
                }else{
                    str = str + c.getSatellites().get(i).get_id()+" ";
                }

            }
        }

        str = str + architecture.getGroundNetwork().getGroundStations().size()+",";
        for (int i=0; i<architecture.getGroundNetwork().getGroundStations().size(); i++){
            if (i==architecture.getGroundNetwork().getGroundStations().size()-1){
                str = str + architecture.getGroundNetwork().getGroundStations().get(i).get_id()+",";
            }else{
                str = str + architecture.getGroundNetwork().getGroundStations().get(i).get_id()+" ";
            }
        }
        str = str + execTime;
        return str;
    }

    /**
     * Creates the line of the summary file for a given architecture, its id number, execution time and objectives (GA only)
     * @param architecture the architecture creator object
     * @param archCounter the architecture id number
     * @param execTime the computational time needed to evaluate the architecture
     * @param objectives the objective values of the architecture
     * @return the summary line for the architecture
     */
    public static String getLineSummaryDataWithObjectives(ArchitectureCreator architecture, int archCounter,double execTime, double[] objectives){
        String str = getLineSummaryData(architecture,archCounter,execTime);
        for (int i=0; i<objectives.length; i++){
            str = str + "," + objectives[i];
        }
        return str;
    }

    /**
     * Reads the planet labs ephemeris database
     * @return list of TLE satellites
     */
    public static List<TLESatellite> getPlanetLabsEphemerisDatabase(){
        List<TLESatellite> planetSats = new ArrayList<>();
        try {
            String fileName = "planet_mc.tle";
            String resourcePath = System.getProperty("tatc.root") + File.separator + "TSE_Module" +File.separator + "tse" + File.separator + "resources";
            File file = new File(resourcePath,fileName);
            FileReader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            String line;
            String line1 = "";
            String line2 = "";
            int counter = 0;
            while ((line = br.readLine()) != null) {
                if (counter == 0){
                    counter ++;
                }else if (counter == 1){
                    line1=line;
                    counter ++;
                }else if (counter ==2){
                    line2=line;
                    planetSats.add(new TLESatellite(line1,line2));
                    counter = 0;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return planetSats;
    }

    /**
     * Deleting files with an extension within a directory.
     *
     * @param directory Path to the directory where files to delete are located.
     * @param extension Extension of files to delete.
     * @throws IOException Thrown exception if can't delete a file.
     */
    public static void deleteFileWithExtension(String directory, String extension) throws IOException {

        File dir = new File(directory);

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(extension) && !file.delete()) {
                throw new IOException();
            }
        }
    }

    /**
     * Deleting files that start with a certain name within a directory.
     *
     * @param directory Path to the directory where files to delete are located.
     * @param start Starting of files to delete.
     * @throws IOException Thrown exception if can't delete a file.
     */
    public static void deleteFileWithStarting(String directory, String start) throws IOException {

        File dir = new File(directory);

        for (File file : dir.listFiles()) {
            if (file.getName().startsWith(start) && !file.delete()) {
                throw new IOException();
            }
        }
    }


    public static boolean deleteDirectory(File dir) {
        if (dir.isDirectory()) {
            File[] children = dir.listFiles();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDirectory(children[i]);
                if (!success) {
                    return false;
                }
            }
        }
        // either file or an empty directory
        //System.out.println("removing file or directory : " + dir.getName());
        return dir.delete();
    }

}