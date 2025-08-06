package tatc;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * Manages Python server processes for the TSE (Tradespace Search Executive) system.
 * This class handles starting, monitoring, and stopping Python-based evaluation servers
 * that provide external evaluation capabilities to the TSE system.
 * 
 * @author TSE Development Team
 */
public class PythonServerManager {
    private static Process pythonServerProcess = null;
    //private static int serverPort = 5000;
    private static final String SERVER_HOST = "localhost";
    private static boolean isServerStartedByJava = false;
    private int serverPort;

    /**
     * Starts a Python server at the specified port using the given script path.
     * 
     * @param serverPort The port number for the server to listen on
     * @param serverScriptPath The path to the Python script to execute
     * @throws IOException if server startup fails
     */
    public synchronized void startServer(int serverPort, String serverScriptPath) throws IOException {
        this.serverPort = serverPort;
        //killProcessesOnPort(serverPort);
        if (isServerRunning()) {
            System.out.println("Python server is already running on port " + serverPort);
            isServerStartedByJava = false; // Server was not started by this Java process
            return;
        }
        String pythonExecutable = "python";
        // Specify the full path to the Python interpreter
        // Convert the string path to a Path object
        Path path = Paths.get(serverScriptPath);
        // Get the parent directory of the path
        Path parentDirectory = path.getParent();
        // Convert the parent directory to a string
        String evaluatorModulePath = parentDirectory.toString();
        ProcessBuilder builder = new ProcessBuilder(pythonExecutable, serverScriptPath);
        builder.directory(new File(evaluatorModulePath));
        builder.redirectErrorStream(true); // Combine stdout and stderr

        try {
            pythonServerProcess = builder.start();
            isServerStartedByJava = true; // Mark that we started the server

            // Capture and log the process's output
            StreamGobbler outputGobbler = new StreamGobbler(pythonServerProcess.getInputStream(), "OUTPUT");
            outputGobbler.start();

            waitForServerToStart();
            System.out.println("Python server started successfully on port " + serverPort);
        } catch (IOException e) {
            System.err.println("Failed to start Python server: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Checks if the Python server is currently running by attempting to connect to its health endpoint.
     * 
     * @return true if the server is running and responding, false otherwise
     */
    private boolean isServerRunning() {
        try {
            URL url = new URL("http://" + SERVER_HOST + ":" + this.serverPort + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // Optionally check the response content to ensure it's your server
                System.out.println("Server health check successful");
                return true;
            }
        } catch (IOException e) {
            // Server is not running or not responding
        }
        return false;
    }

    /**
     * Waits for the Python server to start by polling the health endpoint.
     * 
     * @throws IOException if the server fails to start within the timeout period
     */
    private void waitForServerToStart() throws IOException {
        int maxRetries = 10;
        int waitTime = 1000; // milliseconds
        for (int i = 0; i < maxRetries; i++) {
            if (this.isServerRunning()) {
                return;
            }
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IOException("Interrupted while waiting for server to start", e);
            }
        }
        throw new IOException("Python server failed to start after " + (maxRetries * waitTime / 1000) + " seconds.");
    }

    /**
     * Stops the Python server if it was started by this Java process.
     */
    public static synchronized void stopServer() {
        if (isServerStartedByJava && pythonServerProcess != null && pythonServerProcess.isAlive()) {
            pythonServerProcess.destroy();
            System.out.println("Python server stopped.");
            pythonServerProcess = null;
            isServerStartedByJava = false;
        }
    }

    /**
     * Kills all processes listening on the specified port (Windows-specific implementation).
     * 
     * @param port The port number to check for processes
     */
    private static void killProcessesOnPort(int port) {
        try {
            // Run the netstat command to find processes listening on the specified port
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "netstat -aon | findstr :" + port);
            Process process = processBuilder.start();
            
            // Capture the output of the command
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Split the line to get the PID (Process ID)
                String[] parts = line.trim().split("\\s+");
                if (parts.length > 4) {
                    String pid = parts[parts.length - 1];  // The PID is the last column
                    System.out.println("Killing process with PID: " + pid);
                    
                    // Kill the process
                    ProcessBuilder killProcess = new ProcessBuilder("cmd.exe", "/c", "taskkill /PID " + pid + " /F");
                    killProcess.start();
                }
            }
        } catch (IOException e) {
            System.err.println("Failed to kill processes on port " + port + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * StreamGobbler class to capture and log process output streams.
     * This inner class runs as a separate thread to handle process output.
     */
    private static class StreamGobbler extends Thread {
        private final InputStream inputStream;
        private final String type;

        /**
         * Constructs a new StreamGobbler for the specified input stream.
         * 
         * @param inputStream The input stream to monitor
         * @param type The type identifier for logging (e.g., "OUTPUT", "ERROR")
         */
        public StreamGobbler(InputStream inputStream, String type) {
            this.inputStream = inputStream;
            this.type = type;
        }

        @Override
        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println("[" + type + "] " + line);
                }
            } catch (IOException e) {
                System.err.println("Error reading process output: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
