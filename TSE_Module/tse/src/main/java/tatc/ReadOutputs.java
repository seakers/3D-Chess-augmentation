package tatc;

/**
 * A pre-Phase A constellation mission analysis tool for reading and processing
 * output files from TSE evaluations. This class provides utilities for extracting
 * metrics from architecture evaluation results and generating summary reports.
 * 
 * @author TSE Development Team
 */

import tatc.architecture.outputspecifications.CostRisk;
import tatc.architecture.outputspecifications.Gbl;
import tatc.architecture.specifications.Architecture;
import tatc.util.JSONIO;
import tatc.util.Utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ReadOutputs {

    /**
     * Main method for processing TSE output files and generating metrics summaries.
     * 
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        // Setup logger for detailed output
        Level level = Level.FINEST;
        Logger.getGlobal().setLevel(level);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        Logger.getGlobal().addHandler(handler);

        // Process results from formulation1_0
        String mainpath = System.getProperty("user.dir") + File.separator + ".." + File.separator + ".." + File.separator + "results"+ File.separator + "results_formulation1_0";
        try (FileWriter fw = new FileWriter(new File(mainpath + File.separator + "metricsArchs.csv"))) {
            // Write the header
            fw.append(String.format("arch_id,altitude[km],inclination[deg],nsat,nplanes,f,altitude[km],inclination[deg],nsat,nplanes,f,altitude[km],inclination[deg],nsat,nplanes,f,AgvRevTime[s],MeanResponseTime[s],Cost[$]"));
            fw.append("\n");
            fw.flush();
            
            int numArchs = 970;
            for (int i = 0; i < numArchs; i++) {
                String outputFilePath1 = mainpath + File.separator + "arch-" + i + File.separator + "gbl.json";
                String outputFilePath2 = mainpath + File.separator + "arch-" + i + File.separator + "CostRisk_Output.json";
                String outputFilePath3 = mainpath + File.separator + "arch-" + i + File.separator + "arch.json";
                
                Gbl glb = JSONIO.readJSON(new File(outputFilePath1), Gbl.class);
                CostRisk costrisk = JSONIO.readJSON(new File(outputFilePath2), CostRisk.class);
                Architecture arch = JSONIO.readJSON(new File(outputFilePath3), Architecture.class);

                String line = "arch-" + i + ",";
                for (int j = 0; j < 3; j++) {
                    if (j < arch.getSpaceSegment().size()) {
                        double alt = arch.getSpaceSegment().get(j).getSatellites().get(0).getOrbit().getSemimajorAxis() - Utilities.EARTH_RADIUS_KM;
                        double inc = (double) arch.getSpaceSegment().get(j).getSatellites().get(0).getOrbit().getInclination();
                        int nsat = arch.getSpaceSegment().get(j).getSatellites().size();
                        int nplanes = (int) arch.getSpaceSegment().get(j).getNumberPlanes();
                        int f = (int) arch.getSpaceSegment().get(j).getRelativeSpacing();
                        line = line + alt + "," + inc + "," + nsat + "," + nplanes + "," + f + ",";
                    } else {
                        line = line + 0 + "," + 0 + "," + 0 + "," + 0 + "," + 0 + ",";
                    }
                }

                line = line + glb.getRevisitTime().getAvg() + "," + glb.getResponseTime().getAvg() + "," + costrisk.getLifecycleCost().getEstimate() + "," + glb.getCoverage();
                fw.append(line);
                fw.append("\n");
                fw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, "Error processing output files", ex);
        }
    }
}
