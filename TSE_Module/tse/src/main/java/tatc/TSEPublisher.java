package tatc;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class TSEPublisher {
    private String brokerUrl;
    private String clientId;
    private MqttClient mqttClient;

    public TSEPublisher(String brokerUrl, String clientId) {
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

    // Publish a message to a topic
    public void publish(String topic, String content, int qos) throws MqttException {
        MqttMessage message = new MqttMessage(content.getBytes());
        message.setQos(qos);
        mqttClient.publish(topic, message);
        System.out.println("Message published to topic \"" + topic + "\": " + content);
    }

    // Disconnect from the MQTT broker
    public void disconnect() throws MqttException {
        mqttClient.disconnect();
        System.out.println("Disconnected from broker");
    }
}
