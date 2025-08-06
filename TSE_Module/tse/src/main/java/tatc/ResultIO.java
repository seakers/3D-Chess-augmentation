package tatc;

/**
 * Utility class for input and output operations in the TSE system.
 * Provides methods for saving and loading various data formats including
 * populations, objectives, hypervolume metrics, and search results.
 * 
 * @author Prachi
 * @author TSE Development Team
 */

import org.moeaframework.core.Population;
import org.moeaframework.core.PopulationIO;
import org.moeaframework.core.Solution;

import com.google.gson.internal.LinkedTreeMap;

import tatc.architecture.ArchitectureCreator;
import tatc.architecture.outputspecifications.CostRisk;
import tatc.architecture.outputspecifications.Gbl;
import tatc.architecture.outputspecifications.ValueOutput;
import tatc.architecture.specifications.Constellation;
import tatc.architecture.specifications.Instrument;
import tatc.architecture.specifications.PassiveOpticalScanner;
import tatc.architecture.specifications.Satellite;
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
     * Saves the measured hypervolume at NFE in a CSV file.
     * The file will contain columns for NFE (Number of Function Evaluations),
     * HV (Hypervolume), and IGD (Inverted Generational Distance).
     *
     * @param searchMetrics Map containing algorithm hypervolume at different NFE values
     * @param filename Base filename (without extension) for the output file
     */
    public static void saveHyperVolume(HashMap<Integer, Double[]> searchMetrics, String filename) {
        File results = new File(filename + ".csv");
        System.out.println("Saving performance metrics");

        try (FileWriter writer = new FileWriter(results)) {
            // Write the headers
            writer.append("NFE");
            writer.append(",");
            writer.append("HV");
            writer.append(",");
            writer.append("IGD");
            writer.append("\n");

            // Write data rows
            Set<Integer> keys = searchMetrics.keySet();
            Iterator<Integer> keyIterator = keys.iterator();
            while (keyIterator.hasNext()) {
                Integer key = keyIterator.next();
                writer.append(key.toString()).append(",");
                writer.append(searchMetrics.get(key)[0].toString());
                writer.append(",");
                writer.append(searchMetrics.get(key)[1].toString());
                writer.append("\n");
            }
            writer.flush();
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error saving hypervolume data", ex);
        }
    }

    /**
     * Saves only the objective values of the solutions in the population.
     * Uses the MOEA Framework's PopulationIO for standardized output format.
     *
     * @param pop The population containing solutions
     * @param filename Base filename (without extension) for the output file
     */
    public static void saveObjectives(Population pop, String filename) {
        System.out.println("Saving objectives");

        try {
            PopulationIO.writeObjectives(new File(filename + ".txt"), pop);
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error saving objectives", ex);
        }
    }

    /**
     * Saves the objective values, decision values, and attributes of the
     * solutions in the population to a .res file.
     *
     * @param pop The population containing solutions
     * @param filename Base filename (without extension) for the output file
     * @return true if the file has been saved successfully, false otherwise
     */
    public static boolean saveSearchResults(Population pop, String filename) {
        if (pop.isEmpty()) {
            return false;
        }
        System.out.println("Saving search results");
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename + ".res")))) {
            // Write the headers
            for (int i = 0; i < pop.get(0).getNumberOfObjectives(); i++) {
                bw.append(String.format("obj%d", i));
                bw.append(" ");
            }
            for (int i = 0; i < pop.get(0).getNumberOfVariables(); i++) {
                bw.append(String.format("dec%d", i));
                bw.append(" ");
            }
            Set<String> attrSet = pop.get(0).getAttributes().keySet();
            for (String attr : attrSet) {
                bw.append(attr + " ");
            }
            bw.newLine();

            // Write values for each solution
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
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error saving search results", ex);
            return false;
        }
        return true;
    }

    /**
     * Reads a set of objective vectors from the specified file.
     * Files read using this method should only have been created using the
     * {@code saveObjectives} method.
     *
     * @param file The file containing the objective vectors
     * @return A population containing all objective vectors in the specified file
     * @throws IOException if an I/O exception occurred
     */
    public static Population readObjectives(File file) throws IOException {
        return PopulationIO.readObjectives(file);
    }

    /**
     * Writes a collection of solutions to the specified file.
     * This saves all the explanations as well as any computed objectives.
     * Files written using this method should only be read using the
     * {@code loadPopulation} method. This method relies on serialization.
     *
     * @param pop The solutions to be written in the specified file
     * @param filename The filename including the path to which the solutions are written
     */
    public static void savePopulation(Population pop, String filename) {
        System.out.println("Saving population");

        try {
            PopulationIO.write(new File(filename + ".pop"), pop);
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error saving population", ex);
        }
    }

    /**
     * Reads a population from the specified file.
     * Files read using this method should only have been created using the
     * {@code savePopulation} method. This method relies on serialization.
     *
     * @param filename The filename including the path to which the solutions are written
     * @return A population containing all solutions in the specified file
     * @throws IOException if an I/O exception occurred
     */
    public static Population loadPopulation(String filename) throws IOException {
        return PopulationIO.read(new File(filename));
    }

    /**
     * Saves the label of each individual stored in the population to a delimited file.
     * Only individuals with a label attribute will be saved. In addition to the
     * label, the decision values and objective values will be saved in the file as well.
     * If the population is empty, this method does not attempt to save to any file
     * and returns false.
     *
     * @param population The population containing the individuals to be saved
     * @param filename The base filename (without extension) for the output file
     * @param separator The delimiter to use between values
     * @return true if the file was successfully saved, false otherwise
     */
    public static boolean saveLabels(Population population, String filename, String separator) {
        if (population.isEmpty()) {
            return false;
        }

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename + ".dlm")))) {
            // Write header
            bw.append("Label");
            bw.append(separator);
            for (int i = 0; i < population.get(0).getNumberOfVariables(); i++) {
                bw.append("dec" + i);
                bw.append(separator);
            }
            for (int i = 0; i < population.get(0).getNumberOfObjectives(); i++) {
                bw.append("obj" + i);
                if (i < population.get(0).getNumberOfObjectives() - 1) {
                    bw.append(separator);
                }
            }
            bw.newLine();

            // Write data for each solution
            for (Solution solution : population) {
                String label = (String) solution.getAttribute("label");
                if (label != null) {
                    bw.append(label);
                    bw.append(separator);
                    for (int i = 0; i < solution.getNumberOfVariables(); i++) {
                        bw.append(solution.getVariable(i).toString());
                        bw.append(separator);
                    }
                    for (int i = 0; i < solution.getNumberOfObjectives(); i++) {
                        bw.append(String.valueOf(solution.getObjective(i)));
                        if (i < solution.getNumberOfObjectives() - 1) {
                            bw.append(separator);
                        }
                    }
                    bw.newLine();
                }
            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error saving labels", ex);
            return false;
        }
    }

    /**
     * Saves driving features to a delimited file.
     * Each feature is saved with its name, value, and other relevant information.
     *
     * @param features List of driving features to save
     * @param filename Base filename (without extension) for the output file
     * @param separator The delimiter to use between values
     * @return true if the file was successfully saved, false otherwise
     */
    /**
     * Saves driving features to a file with their metrics.
     * 
     * @param features List of driving features to save
     * @param filename Base filename for the output file
     * @param separator Separator character for the CSV format
     * @return true if the file was saved successfully, false otherwise
     */
    public static boolean saveFeatures(List<DrivingFeature> features, String filename, String separator) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename + ".dlm")))) {
            // Write header
            bw.append("Feature");
            bw.append(separator);
            bw.append("Support");
            bw.append(separator);
            bw.append("Lift");
            bw.append(separator);
            bw.append("ForwardConfidence");
            bw.append(separator);
            bw.append("ReverseConfidence");
            bw.newLine();

            // Write data for each feature
            for (DrivingFeature feature : features) {
                bw.append(feature.getName());
                bw.append(separator);
                bw.append(String.valueOf(feature.getSupport()));
                bw.append(separator);
                bw.append(String.valueOf(feature.getLift()));
                bw.append(separator);
                bw.append(String.valueOf(feature.getFConfidence()));
                bw.append(separator);
                bw.append(String.valueOf(feature.getRConfidence()));
                bw.newLine();
            }
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error saving features", ex);
            return false;
        }
    }

    /**
     * Reads output values from files generated by the evaluation process.
     * This method reads specific output values based on the output name and architecture ID.
     *
     * @param outputName The name of the output to read
     * @param archId The architecture ID
     * @return The output value as a double
     */
    public static double readOutput(String outputName, int archId) {
        // Implementation details would go here
        // This is a placeholder for the actual implementation
        return 0.0;
    }

    /**
     * Reads reduction metrics objective values from a file.
     * Objective names should match the keys in the JSON file.
     *
     * @param file The file containing the reduction metrics
     * @param objectiveName The name of the objective to read
     * @return The objective value as a double
     */
    public static double readReductionMetricsObjective(File file, String objectiveName) {
        // Implementation details would go here
        // This is a placeholder for the actual implementation
        return 0.0;
    }

    /**
     * Reads cost risk objective values from a file.
     * Objective names should match the keys in the JSON file.
     *
     * @param file The file containing the cost risk data
     * @param objectiveName The name of the objective to read
     * @return The objective value as a double
     */
    public static double readCostRiskObjective(File file, String objectiveName) {
        // Implementation details would go here
        // This is a placeholder for the actual implementation
        return 0.0;
    }

    /**
     * Reads value objective values from a file.
     * Objective names should match the keys in the JSON file.
     *
     * @param file The file containing the value data
     * @param objectiveName The name of the objective to read
     * @return The objective value as a double
     */
    public static double readValueObjective(File file, String objectiveName) {
        // Implementation details would go here
        // This is a placeholder for the actual implementation
        return 0.0;
    }

    /**
     * Reads level 2 metrics instrument module values from a file.
     *
     * @param file The file containing the level 2 metrics
     * @param objectiveName The name of the objective to read
     * @return The objective value as a double
     */
    public static double readLevel2MetricsInstrumentModule(File file, String objectiveName) {
        // Implementation details would go here
        // This is a placeholder for the actual implementation
        return 0.0;
    }

    /**
     * Creates a summary file with the specified number of objectives.
     *
     * @param file The file to create
     * @param numberObjectives The number of objectives
     * @return true if the file was created successfully, false otherwise
     */
    public static boolean createSummaryFile(File file, int numberObjectives) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.append("Architecture");
            bw.append(",");
            bw.append("ExecutionTime");
            for (int i = 0; i < numberObjectives; i++) {
                bw.append(",");
                bw.append("Objective" + i);
            }
            bw.newLine();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error creating summary file", ex);
            return false;
        }
    }

    /**
     * Adds a summary line to an existing summary file.
     *
     * @param file The summary file to append to
     * @param line The line to add
     * @return true if the line was added successfully, false otherwise
     */
    public static boolean addSummaryLine(File file, String line) {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, true))) {
            bw.append(line);
            bw.newLine();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error adding summary line", ex);
            return false;
        }
    }

    /**
     * Generates a summary line for an architecture with execution time.
     *
     * @param architecture The architecture creator
     * @param archCounter The architecture counter
     * @param execTime The execution time
     * @return A formatted summary line
     */
    public static String getLineSummaryData(ArchitectureCreator architecture, int archCounter, double execTime) {
        // Implementation details would go here
        // This is a placeholder for the actual implementation
        return "";
    }

    /**
     * Generates a summary line for an architecture with execution time and objectives.
     *
     * @param architecture The architecture creator
     * @param archCounter The architecture counter
     * @param execTime The execution time
     * @param objectives The objective values
     * @return A formatted summary line
     */
    public static String getLineSummaryDataWithObjectives(ArchitectureCreator architecture, int archCounter, double execTime, double[] objectives) {
        // Implementation details would go here
        // This is a placeholder for the actual implementation
        return "";
    }

    /**
     * Retrieves the Planet Labs ephemeris database as a list of TLE satellites.
     *
     * @return List of TLE satellites from the Planet Labs database
     */
    public static List<TLESatellite> getPlanetLabsEphemerisDatabase() {
        // Implementation details would go here
        // This is a placeholder for the actual implementation
        return new ArrayList<>();
    }

    /**
     * Deletes files with a specific extension from a directory.
     *
     * @param directory The directory to search in
     * @param extension The file extension to match
     * @throws IOException if an I/O error occurs
     */
    public static void deleteFileWithExtension(String directory, String extension) throws IOException {
        File dir = new File(directory);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.endsWith(extension));
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Deletes files that start with a specific prefix from a directory.
     *
     * @param directory The directory to search in
     * @param start The prefix to match
     * @throws IOException if an I/O error occurs
     */
    public static void deleteFileWithStarting(String directory, String start) throws IOException {
        File dir = new File(directory);
        if (dir.exists() && dir.isDirectory()) {
            File[] files = dir.listFiles((d, name) -> name.startsWith(start));
            if (files != null) {
                for (File file : files) {
                    if (!file.delete()) {
                        throw new IOException("Failed to delete file: " + file.getAbsolutePath());
                    }
                }
            }
        }
    }

    /**
     * Recursively deletes a directory and all its contents.
     *
     * @param dir The directory to delete
     * @return true if the directory was deleted successfully, false otherwise
     */
    public static boolean deleteDirectory(File dir) {
        if (dir.exists()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory()) {
                        deleteDirectory(file);
                    } else {
                        file.delete();
                    }
                }
            }
        }
        return dir.delete();
    }
}