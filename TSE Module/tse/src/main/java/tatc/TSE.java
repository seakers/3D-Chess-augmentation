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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of the Tradespace Search Executive
 */

public class TSE {

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

        long startTime = System.nanoTime();
        TradespaceSearchExecutive tse = new TradespaceSearchExecutive(args[0],args[1]);
        tse.run();
        long endTime = System.nanoTime();
        Logger.getGlobal().finest(String.format("Took %.4f sec", (endTime - startTime) / Math.pow(10, 9)));
    }
}
