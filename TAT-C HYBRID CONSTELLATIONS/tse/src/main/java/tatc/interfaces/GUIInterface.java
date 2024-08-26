package tatc.interfaces;

import java.net.*;
import java.io.*;

/**
 * This method handles http requests from the GUIInterface. Sends errors to GUIInterface.
 *
 * @author Prachi
 */

public class GUIInterface {

    public void sendResponses(String targetURL, String urlParameters, String message) {

        String serverName = targetURL;
        int port = Integer.parseInt(urlParameters);
        try {
            System.out.println("Connecting to " + serverName + " on port " + port);
            Socket client = new Socket(serverName, port);

            System.out.println("Connected to " + client.getRemoteSocketAddress());
            OutputStream outToServer = client.getOutputStream();
            DataOutputStream out = new DataOutputStream(outToServer);

            out.writeUTF(message);
            InputStream inFromServer = client.getInputStream();
            DataInputStream in = new DataInputStream(inFromServer);

            String responseFromServer = in.readUTF();

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

