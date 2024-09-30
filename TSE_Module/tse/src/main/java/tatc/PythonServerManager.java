package tatc;

import java.io.*;
import java.net.URL;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
public class PythonServerManager {
    private static Process pythonServerProcess = null;
    //private static int serverPort = 5000;
    private static final String SERVER_HOST = "localhost";
    private static boolean isServerStartedByJava = false;
    private int server_port;

    public synchronized void startServer(int server_port,String serverScriptPath) throws IOException {
        this.server_port = server_port;
        //killProcessesOnPort(server_port);
        if (isServerRunning()) {
            System.out.println("Python server is already running.");
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
            System.out.println("Python server started.");
        } catch (IOException e) {
            System.err.println("Failed to start Python server.");
            throw e;
        }
    }

    private boolean isServerRunning() {
        try {
            URL url = new URL("http://" + SERVER_HOST + ":" + this.server_port + "/health");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(1000);
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                // Optionally check the response content to ensure it's your server
                System.out.println("Server has responded");
                return true;
            }
        } catch (IOException e) {
            // Server is not running
        }
        return false;
    }

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

    public static synchronized void stopServer() {
        if (isServerStartedByJava && pythonServerProcess != null && pythonServerProcess.isAlive()) {
            pythonServerProcess.destroy();
            System.out.println("Python server stopped.");
            pythonServerProcess = null;
            isServerStartedByJava = false;
        }
    }
    private static void killProcessesOnPort(int port) {
        try {
            // Run the netstat command to find processes listening on port 5000
            ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", "netstat -aon | findstr :"+port);
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
            System.err.println("Failed to kill processes on port 5000: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // StreamGobbler class to capture output
    private static class StreamGobbler extends Thread {
        private InputStream inputStream;
        private String type;

        public StreamGobbler(InputStream inputStream, String type) {
            this.inputStream = inputStream;
            this.type = type;
        }

        public void run() {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
                String line = null;
                while ((line = br.readLine()) != null) {
                    System.out.println("[" + type + "] " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }
}
