/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc;

/*
 * A pre-Phase A constellation mission analysis tool
 */

import tatc.tradespaceiterator.TradespaceSearchExecutive;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
/**
 * Main class of the Tradespace Search Executive
 */

public class TSE {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //setup logger
        if (args == null || args.length == 0) {
            args = new String[2]; 
            // args[0] = "tse\\problems\\CaseStudy1.json";
            // args[1] = "tse\\results";
            // args[0] = "C:\Users\dfornos\Desktop\tat-c\tse\problems\CaseStudy1.json";
            // args[1] = "C:\Users\dfornos\Desktop\tat-c\tse\results";
            //args[0] = "TSE_Module\\tse\\problems\\CaseStudy1.json";
            args[0] = "TSERequestExample2.json";
            args[1] = "TSE_Module\\tse\\results";
            
        }
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        args[0] = "workflow_outputtest.json";
        args[1] = "TSE_Module\\tse\\results\\results_"+timestamp;
        Path path = Paths.get(args[1]);
        try{
            Files.createDirectories(path);
        }catch(IOException e){
            System.out.println("Directory doesn't exist");
        }
        String fullPathArg0 = Paths.get(args[0]).toAbsolutePath().toString();      
        String fullPathArg1 = Paths.get(args[1]).toAbsolutePath().toString();
        Level level = Level.FINEST;
        Logger.getGlobal().setLevel(level);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(level);
        Logger.getGlobal().addHandler(handler);
        long startTime = System.nanoTime();
        TradespaceSearchExecutive tse = new TradespaceSearchExecutive(fullPathArg0,fullPathArg1);
        tse.run();
        long endTime = System.nanoTime();
        Logger.getGlobal().finest(String.format("Took %.4f sec", (endTime - startTime) / Math.pow(10, 9)));
    }
}
