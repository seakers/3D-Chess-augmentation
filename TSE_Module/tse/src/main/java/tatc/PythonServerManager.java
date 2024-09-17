package tatc;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.InetSocketAddress;

public class PythonServerManager {
    private static Process pythonServerProcess = null;
    private static final int SERVER_PORT = 5000;
    private static final String SERVER_HOST = "localhost";
    private static boolean isServerStartedByJava = false;

    public static synchronized void startServer() throws IOException {
        if (isServerRunning()) {
            System.out.println("Python server is already running.");
            isServerStartedByJava = false; // Server was not started by this Java process
            stopServer();
            //return;
        }

        String tatcRoot = System.getProperty("tatc.root");
        String evaluatorModulePath = tatcRoot + File.separator + "Evaluator_Module" + File.separator + "SpaDes";
        String serverScriptPath = evaluatorModulePath + File.separator + "server.py";

        ProcessBuilder builder = new ProcessBuilder("python", serverScriptPath);
        builder.directory(new File(evaluatorModulePath));
        builder.redirectErrorStream(true); // Combine stdout and stderr

        try {
            pythonServerProcess = builder.start();
            isServerStartedByJava = true; // Mark that we started the server
            waitForServerToStart();
            System.out.println("Python server started.");
        } catch (IOException e) {
            System.err.println("Failed to start Python server.");
            throw e;
        }
    }

    private static boolean isServerRunning() {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), 1000);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void waitForServerToStart() throws IOException {
        int maxRetries = 10;
        int waitTime = 1000; // milliseconds
        for (int i = 0; i < maxRetries; i++) {
            if (isServerRunning()) {
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
}
