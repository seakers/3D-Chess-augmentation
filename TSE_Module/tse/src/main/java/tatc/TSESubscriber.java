package tatc;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import java.util.function.BiConsumer;

/**
 * MQTT subscriber for the TSE (Tradespace Search Executive) system.
 * This class handles subscribing to MQTT topics and processing incoming messages
 * through a configurable message handler.
 * 
 * @author TSE Development Team
 */
public class TSESubscriber {
    private final String brokerUrl;
    private final String clientId;
    private MqttClient mqttClient;

    /**
     * Constructs a new TSESubscriber with the specified broker URL and client ID.
     * 
     * @param brokerUrl The MQTT broker URL (e.g., "tcp://localhost:1883")
     * @param clientId The unique client identifier for this subscriber
     */
    public TSESubscriber(String brokerUrl, String clientId) {
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
     * Subscribes to a specific MQTT topic and sets up message handling.
     * 
     * @param topic The MQTT topic to subscribe to
     * @param qos The Quality of Service level (0, 1, or 2)
     * @param messageHandler A BiConsumer that handles incoming messages (topic, payload)
     * @throws MqttException if subscription fails
     */
    public void subscribe(String topic, int qos, BiConsumer<String, String> messageHandler) throws MqttException {
        if (mqttClient == null || !mqttClient.isConnected()) {
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }
        
        mqttClient.subscribe(topic, qos, new IMqttMessageListener() {
            @Override
            public void messageArrived(String receivedTopic, MqttMessage message) throws Exception {
                String payload = new String(message.getPayload());
                // Pass the topic and payload to the provided message handler
                messageHandler.accept(receivedTopic, payload);
            }
        });
        System.out.println("Subscribed to MQTT topic: " + topic);
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
