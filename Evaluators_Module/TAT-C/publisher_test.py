import paho.mqtt.client as mqtt
import json

# MQTT Broker details
BROKER_ADDRESS = 'localhost'  # Replace with your broker address if it's different
BROKER_PORT = 1883
REQUEST_TOPIC = 'evaluation/requests/coverage'

# Sample message payload
test_payload = {
    "architecture": {
        # Include a simplified architecture JSON that TAT-C expects
        "@type": "Architecture",
        "spaceSegment": [
            {
                "@type": "Constellation",
                "satellites": [
                    {
                        "@type": "Satellite",
                        "name": "TestSat",
                        "orbit": {
                            "semimajorAxis": 7178.14,
                            "eccentricity": 0.0,
                            "inclination": 10.0,
                            "rightAscensionAscendingNode": 0.0,
                            "periapsisArgument": 0.0,
                            "trueAnomaly": 0.0,
                            "epoch": "2019-08-01T00:00:00Z"
                        },
                        "payload": [
                            {
                                "fieldOfView": {
                                    "sensorGeometry": "RECTANGULAR",
                                    "fullConeAngle": 30.0,
                                    "alongTrackFieldOfView": 40.0,
                                    "crossTrackFieldOfView": 114.0
                                }
                            }
                        ]
                    }
                ]
            }
        ]
    },
    "folderPath": "test/folder/path",  # Adjust as needed
    "workflow_id": "test_workflow_001"
}

# Initialize the MQTT client
client = mqtt.Client()

# Connect to the broker
client.connect(BROKER_ADDRESS, BROKER_PORT, 60)

# Publish the test message
client.publish(REQUEST_TOPIC, json.dumps(test_payload))

print(f"Published test payload to topic {REQUEST_TOPIC}")

# Disconnect from the broker
client.disconnect()
