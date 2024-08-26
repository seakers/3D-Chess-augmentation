/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc;

/*
 * A pre-Phase A constellation mission analysis tool
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
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //setup logger
        Level level = Level.FINEST;
        Logger.getGlobal().setLevel(level);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        Logger.getGlobal().addHandler(handler);

//        String mainpath = System.getProperty("user.dir") + File.separator + "problems" + File.separator + "FF_numSats" + File.separator + "problems";
//        try (FileWriter fw = new FileWriter(new File(mainpath + File.separator + "metricsArchs.csv"))) {
//            //write the header
//            fw.append(String.format("arch_id,AgvRevTime[s],MaxRevTime[s]"));
//            fw.append("\n");
//            fw.flush();
//            int numArchs = 75;
//            for (int i=0; i<numArchs; i++){
//                String outputFilePath = mainpath + File.separator + "arch-" + i + File.separator + "gbl.json";
//                Gbl glb = JSONIO.readJSON( new File(outputFilePath), Gbl.class);
//                String line = "arch-" + i + "," + glb.getRevisitTime().getAvg() + "," + glb.getRevisitTime().getMax();
//                fw.append(line);
//                fw.append("\n");
//                fw.flush();
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
//        }


//        String mainpath = System.getProperty("user.dir") + File.separator + "problems" + File.separator + "TROPICS_FF" + File.separator + "problems";
//        //String mainpath = System.getProperty("user.dir") + File.separator + "problems" + File.separator + "FF_final";
//        String mainpath = System.getProperty("user.dir") + File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + "resultsTROPICS_5000_MOEA1";
//        try (FileWriter fw = new FileWriter(new File(mainpath + File.separator + "metricsArchs.csv"))) {
//            //write the header
//            fw.append(String.format("arch_id,altitude[km],inclination[deg],nsat,nplanes,f,AgvRevTime[s],MaxRevTime[s],Cost[$]"));
//            fw.append("\n");
//            fw.flush();
//            int numArchs = 500;
//            for (int i=0; i<numArchs; i++){
//                String outputFilePath1 = mainpath + File.separator + "arch-" + i + File.separator + "gbl.json";
//                String outputFilePath2 = mainpath + File.separator + "arch-" + i + File.separator + "CostRisk_Output.json";
//                String outputFilePath3 = mainpath + File.separator + "arch-" + i + File.separator + "arch.json";
//                Gbl glb = JSONIO.readJSON( new File(outputFilePath1), Gbl.class);
//                CostRisk costrisk = JSONIO.readJSON( new File(outputFilePath2), CostRisk.class);
//                Architecture arch = JSONIO.readJSON( new File(outputFilePath3), Architecture.class);
//                double alt = arch.getSpaceSegment().get(0).getSatellites().get(0).getOrbit().getSemimajorAxis()- Utilities.EARTH_RADIUS_KM;
//                double inc = (double)arch.getSpaceSegment().get(0).getSatellites().get(0).getOrbit().getInclination();
//                int nsat = arch.getSpaceSegment().get(0).getSatellites().size();
//                int nplanes = (int)arch.getSpaceSegment().get(0).getNumberPlanes();
//                int f = (int)arch.getSpaceSegment().get(0).getRelativeSpacing();
//
//                String line = "arch-" + i + "," + alt + "," + inc + "," + nsat + "," + nplanes + "," + f + "," +
//                        glb.getRevisitTime().getAvg() + "," + glb.getRevisitTime().getMax() + ","+ costrisk.getLifecycleCost().getEstimate();
//                fw.append(line);
//                fw.append("\n");
//                fw.flush();
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
//        }

        //String mainpath = System.getProperty("user.dir") + File.separator + "problems" + File.separator + "GA_CostOrbitsLaunch" + File.separator + "problems";
        String mainpath = System.getProperty("user.dir") + File.separator + ".." + File.separator + ".." + File.separator + "results"+ File.separator + "results_formulation1_0";
        try (FileWriter fw = new FileWriter(new File(mainpath + File.separator + "metricsArchs.csv"))) {
            //write the header
            fw.append(String.format("arch_id,altitude[km],inclination[deg],nsat,nplanes,f,altitude[km],inclination[deg],nsat,nplanes,f,altitude[km],inclination[deg],nsat,nplanes,f,AgvRevTime[s],MeanResponseTime[s],Cost[$]"));
            fw.append("\n");
            fw.flush();
            int numArchs = 970;
            for (int i=0; i<numArchs; i++){
                String outputFilePath1 = mainpath + File.separator + "arch-" + i + File.separator + "gbl.json";
                String outputFilePath2 = mainpath + File.separator + "arch-" + i + File.separator + "CostRisk_Output.json";
                String outputFilePath3 = mainpath + File.separator + "arch-" + i + File.separator + "arch.json";
                Gbl glb = JSONIO.readJSON( new File(outputFilePath1), Gbl.class);
                CostRisk costrisk = JSONIO.readJSON( new File(outputFilePath2), CostRisk.class);
                Architecture arch = JSONIO.readJSON( new File(outputFilePath3), Architecture.class);

                String line = "arch-" + i + ",";
                for (int j=0; j<3;j++){
                    if (j<arch.getSpaceSegment().size()){
                        double alt = arch.getSpaceSegment().get(j).getSatellites().get(0).getOrbit().getSemimajorAxis()- Utilities.EARTH_RADIUS_KM;
                        double inc = (double)arch.getSpaceSegment().get(j).getSatellites().get(0).getOrbit().getInclination();
                        int nsat = arch.getSpaceSegment().get(j).getSatellites().size();
                        int nplanes = (int)arch.getSpaceSegment().get(j).getNumberPlanes();
                        int f = (int)arch.getSpaceSegment().get(j).getRelativeSpacing();
                        line = line + alt + "," + inc + "," + nsat + "," + nplanes + "," + f + ",";
                    }else{
                        line = line + 0 + "," + 0 + "," + 0 + "," + 0 + "," + 0 + ",";
                    }

                }

                line = line + glb.getRevisitTime().getAvg() + "," + glb.getResponseTime().getAvg() + ","+ costrisk.getLifecycleCost().getEstimate()+ ","+ glb.getCoverage();
                fw.append(line);
                fw.append("\n");
                fw.flush();
            }
        } catch (IOException ex) {
            Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
        }

//        String mainpathRoot = System.getProperty("user.dir") + File.separator + ".." + File.separator + ".." +
//                File.separator + ".." + File.separator + ".." + File.separator + ".." + File.separator + ".." +
//                File.separator + "resultsLab" + File.separator + "results_formulation2_";
//        for (int count =0 ; count<30 ; count++){
//            String mainpath = mainpathRoot+count;
//            try (FileWriter fw = new FileWriter(new File(mainpath + File.separator + "metrics.csv"))) {
//                //write the header
//                fw.append(String.format("AgvRevTime[s],MeanResponseTime[s],Cost[$]"));
//                fw.append("\n");
//                fw.flush();
//                int numArchs = 484;
//                for (int i=0; i<numArchs; i++){
//                    String outputFilePath1 = mainpath + File.separator + "arch-" + i + File.separator + "gbl.json";
//                    String outputFilePath2 = mainpath + File.separator + "arch-" + i + File.separator + "CostRisk_Output.json";
//                    Gbl glb = JSONIO.readJSON( new File(outputFilePath1), Gbl.class);
//                    CostRisk costrisk = JSONIO.readJSON( new File(outputFilePath2), CostRisk.class);
//
//                    String line = glb.getRevisitTime().getAvg() + "," + glb.getResponseTime().getAvg() + ","+ costrisk.getLifecycleCost().getEstimate();
//                    fw.append(line);
//                    fw.append("\n");
//                    fw.flush();
//                }
//            } catch (IOException ex) {
//                Logger.getLogger(ResultIO.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }


    }
}
