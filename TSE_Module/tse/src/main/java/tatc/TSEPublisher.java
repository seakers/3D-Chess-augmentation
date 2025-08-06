package tatc;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * MQTT publisher for the TSE (Tradespace Search Executive) system.
 * This class handles publishing messages to MQTT topics for inter-service communication.
 * 
 * @author TSE Development Team
 */
public class TSEPublisher {
    private final String brokerUrl;
    private final String clientId;
    private MqttClient mqttClient;

    /**
     * Constructs a new TSEPublisher with the specified broker URL and client ID.
     * 
     * @param brokerUrl The MQTT broker URL (e.g., "tcp://localhost:1883")
     * @param clientId The unique client identifier for this publisher
     */
    public TSEPublisher(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }

    /**
     * Connects to the MQTT broker using the configured settings.
     * 
     * @throws MqttException if connection fails
     */
    public void connect() throws MqttException {
        mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        mqttClient.connect(connOpts);
        System.out.println("Connected to MQTT broker: " + brokerUrl);
    }

    /**
     * Publishes a message to the specified MQTT topic.
     * 
     * @param topic The MQTT topic to publish to
     * @param content The message content to publish
     * @param qos The Quality of Service level (0, 1, or 2)
     * @throws MqttException if publishing fails
     */
    public void publish(String topic, String content, int qos) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }
        
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        mqttClient.publish(topic, message);
    }

    /**
     * Disconnects from the MQTT broker and cleans up resources.
     * 
     * @throws MqttException if disconnection fails
     */
    public void disconnect() throws MqttException {
        if (mqttClient != null && mqttClient.isConnected()) {
            mqttClient.disconnect();
            System.out.println("Disconnected from MQTT broker");
        }
    }
}
