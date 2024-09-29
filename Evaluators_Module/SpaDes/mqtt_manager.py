import paho.mqtt.client as mqtt
import json
import logging
import datetime
from ConstellationDesignMain import evaluate_architecture
import os

# Define the directory for logs
log_dir = os.path.join(os.path.dirname(os.path.abspath(__file__)), "logs")

# Check if the log directory exists, if not, create it
if not os.path.exists(log_dir):
    os.makedirs(log_dir)

# Construct the log filename in the logs directory
log_filename = os.path.join(log_dir, f'debug_{datetime.datetime.now().strftime("%Y%m%d_%H%M%S")}.log')

# Create a logger
logger = logging.getLogger('SpaDes_Evaluator')
logger.setLevel(logging.DEBUG)

# Create file handler which logs even debug messages
fh = logging.FileHandler(log_filename)
fh.setLevel(logging.DEBUG)

# Create console handler to output to console (optional)
ch = logging.StreamHandler()
ch.setLevel(logging.DEBUG)

# Create formatter and add it to the handlers
formatter = logging.Formatter('%(asctime)s - %(levelname)s - %(message)s')
fh.setFormatter(formatter)
ch.setFormatter(formatter)

# Add the handlers to the logger
logger.addHandler(fh)
logger.addHandler(ch)

# Log a startup message to confirm logging is working
logger.info("Starting SpaDes MQTT client... Log file is initialized.")

# MQTT Broker details
BROKER_ADDRESS = 'localhost'  # Replace with your broker address
BROKER_PORT = 1883
CLIENT_ID = 'SpaDes_Evaluator'
REQUEST_TOPIC = 'evaluation/requests'
RESULT_TOPIC = 'evaluation/results/SpaDes'

# Create a global MQTT client instance
client = mqtt.Client(
    client_id=CLIENT_ID,
    protocol=mqtt.MQTTv311,
    transport="tcp",
)

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        logger.info("Connected to MQTT Broker!")
        client.subscribe(REQUEST_TOPIC)
        logger.info(f"Subscribed to topic: {REQUEST_TOPIC}")
    else:
        logger.error(f"Failed to connect, return code {rc}")

def on_message(client, userdata, msg):
    logger.debug(f"Message received on topic {msg.topic}: {msg.payload.decode()}")
    try:
        data = json.loads(msg.payload.decode())
        # Check if the message contains the necessary fields
        if 'architecture' in data and 'folderPath' in data and 'workflow_id' in data:
            architecture = data['architecture']
            folder_path = data['folderPath']
            workflow_id = data['workflow_id']

            logger.debug(f'Processing architecture: {architecture}')
            logger.debug(f'Folder path: {folder_path}')
            logger.debug(f'Workflow ID: {workflow_id}')

            # Perform cost evaluation
            logger.debug('Starting cost evaluation...')
            cost = evaluate_architecture(architecture, folder_path)
            logger.debug(f'Cost evaluation completed. Result: {cost}')

            # Prepare the result message
            result = {
                'evaluator': 'SpaDes',
                'workflow_id': workflow_id,
                'results': {
                    'cost': float(cost)
                }
            }

            # Publish the result
            client.publish(RESULT_TOPIC, json.dumps(result))
            logger.debug(f"Published result to topic {RESULT_TOPIC}: {result}")

        else:
            logger.error('Missing required fields in the message.')
    except Exception as e:
        logger.error(f"Error processing message: {str(e)}")

def on_disconnect(client, userdata, rc):
    if rc == 0:
        logger.info("Disconnected from MQTT Broker gracefully.")
    else:
        logger.warning(f"Unexpected disconnection from MQTT Broker. Return code: {rc}")

if __name__ == '__main__':
    logger.info('Starting SpaDes MQTT client...')
    client.on_connect = on_connect
    client.on_message = on_message
    client.on_disconnect = on_disconnect

    # Connect to the broker
    client.connect(BROKER_ADDRESS, BROKER_PORT, keepalive=60)

    # Start the MQTT client loop
    client.loop_forever()
