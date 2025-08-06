package tatc.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for JSON file input/output operations.
 * Provides methods for reading and writing JSON files with proper error handling
 * and type-safe deserialization.
 * 
 * @author TSE Development Team
 */
public class JSONIO {
    
    /**
     * Reads a JSON file and deserializes it into a Java object.
     * 
     * @param <T> The generic type of the object stored in the JSON file
     * @param file The file to read
     * @param c The class of the object stored in the JSON file
     * @return The object stored in the JSON file, or null if reading fails
     */
    public static <T> T readJSON(File file, Class<T> c) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            Gson gson = new Gson();
            return gson.fromJson(br, c);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, "File not found: " + file.getAbsolutePath(), ex);
        } catch (IOException ex) {
            Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, "Error reading JSON file: " + file.getAbsolutePath(), ex);
        }
        return null;
    }
    
    /**
     * Writes an object to a JSON file with pretty printing.
     * 
     * @param file The file to save the JSON to
     * @param obj The object to write to the JSON file
     * @return true if the JSON was successfully saved to the file, false otherwise
     */
    public static boolean writeJSON(File file, Object obj) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String str = gson.toJson(obj);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file))) {
            bw.append(str);
            bw.flush();
            return true;
        } catch (IOException ex) {
            Logger.getLogger(JSONIO.class.getName()).log(Level.SEVERE, "Error writing JSON file: " + file.getAbsolutePath(), ex);
            return false;
        }
    }

    /**
     * Replaces "_type" with "@type" in a given file.
     * This method is used to fix JSON type field formatting issues.
     * 
     * @param file The file to process
     * @throws IOException if an I/O error occurs during file processing
     */
    public static void replaceTypeFieldUnderscore(File file) throws IOException {
        File tempFile = File.createTempFile("buffer", ".tmp");
        
        try (FileWriter fw = new FileWriter(tempFile);
             Reader fr = new FileReader(file);
             BufferedReader br = new BufferedReader(fr)) {
            
            String line;
            while ((line = br.readLine()) != null) {
                fw.write(line.replaceAll("_type", "@type") + "\n");
            }
        }

        // Delete the original file and rename the temp file
        if (!file.delete()) {
            throw new IOException("Failed to delete original file: " + file.getAbsolutePath());
        }
        
        if (!tempFile.renameTo(file)) {
            throw new IOException("Failed to rename temporary file to: " + file.getAbsolutePath());
        }
    }
}