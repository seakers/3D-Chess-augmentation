/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tatc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class utilized to read JSON files
 */

public class JSONIO {
    
    /**
     * Reads a json file and deserialize into java object
     * @param <T> the generic of the object stored in the json file
     * @param file the file to read
     * @param c the class of the object stored in the json file
     * @return the object stored in the json file
     */
    public static <T> T readJSON(File file, Class<T> c){
        try(BufferedReader br = new BufferedReader(new FileReader(file))){
            Gson gson = new Gson();
            return gson.fromJson(br, c);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Writes the distributed spacecraft mission specification to a JSON file
     * @param file the file to save the json
     * @param obj object to write to json file
     * @return true if the json was successfully save to the file
     */
    public static boolean writeJSON(File file, Object obj){
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String str = gson.toJson(obj);
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(file))){
            bw.append(str);
            bw.flush();
        } catch (IOException ex) {
            Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
        return true;
    }

    /**
     * Replaces "_type" to "@type" in a given file.
     * @param file the file
     * @throws IOException IO
     */
    public static void replaceTypeFieldUnderscore(File file) throws IOException {

        File tempFile = File.createTempFile("buffer", ".tmp");
        FileWriter fw = new FileWriter(tempFile);

        Reader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        while(br.ready()) {
            fw.write(br.readLine().replaceAll("_type", "@type") + "\n");
        }

        fw.close();
        br.close();
        fr.close();
        file.delete();

        // Finally replace the original file.
        boolean success = tempFile.renameTo(file);
    }
}