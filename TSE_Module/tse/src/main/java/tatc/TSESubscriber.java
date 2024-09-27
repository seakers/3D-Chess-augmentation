package tatc;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import java.util.function.BiConsumer;

import org.eclipse.paho.client.mqttv3.IMqttMessageListener;



public class TSESubscriber {
    private String brokerUrl;
    private String clientId;
    private MqttClient mqttClient;

    public TSESubscriber(String brokerUrl, String clientId) {
        this.brokerUrl = brokerUrl;
        this.clientId = clientId;
    }

    // Connect to the MQTT broker
    public void connect() throws MqttException {
        mqttClient = new MqttClient(brokerUrl, clientId, new MemoryPersistence());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        mqttClient.connect(connOpts);
        System.out.println("Connected to broker: " + brokerUrl);
    }

    // Subscribe to a topic
public void subscribe(String topic, int qos, BiConsumer<String, String> messageHandler) throws MqttException {
    mqttClient.subscribe(topic, qos, new IMqttMessageListener() {
        @Override
        public void messageArrived(String receivedTopic, MqttMessage message) throws Exception {
            String payload = new String(message.getPayload());
            // Pass the topic and payload to the provided message handler
            messageHandler.accept(receivedTopic, payload);
        }
    });
    System.out.println("Subscribed to topic: " + topic);
}

    // Handle incoming messages
    private void handleMessage(String topic, String payload) {
        // Implement your logic to process the message
        // For example, parse JSON and update the architecture
        System.out.println("Processing message from topic \"" + topic + "\": " + payload);
    }

    // Disconnect from the MQTT broker
    public void disconnect() throws MqttException {
        mqttClient.disconnect();
        System.out.println("Disconnected from broker");
    }
}
