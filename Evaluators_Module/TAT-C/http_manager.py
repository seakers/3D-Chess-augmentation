# tatc_http_server.py

import paho.mqtt.client as mqtt
import json
import logging
from datetime import datetime
import os
import threading

from flask import Flask, request, jsonify

# Import your TAT-C specific modules
from tat_c_manager import evaluate_coverage
from propagation_interface import perform_orbit_propagation
from access_response import perform_access_response

app = Flask(__name__)

class TATCEvaluator:
    # Define the directory for logs
    LOG_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "logs")

    # MQTT Broker details
    BROKER_ADDRESS = 'localhost'  # Replace with your broker address
    BROKER_PORT = 1883
    CLIENT_ID = 'TAT-C_Evaluator'
    RESULT_TOPIC_PREFIX = 'evaluators/TATC/results'

    # Implemented functions and metrics (hardcoded)
    IMPLEMENTED_FUNCTIONS = {
        'OrbitPropagation': perform_orbit_propagation,
        'RevisitAnalysis': evaluate_coverage,
        'CoverageAnalysis': evaluate_coverage,
        'AccessResponse': perform_access_response
    }

    METRICS = ['CoverageFraction', 'HarmonicMeanRevisitTime']

    def __init__(self):
        # Set up logging
        self.setup_logging()

        # Create MQTT client
        self.client = mqtt.Client(
            client_id=self.CLIENT_ID,
            protocol=mqtt.MQTTv311,
            transport="tcp",
        )

        # Assign MQTT callbacks
        self.client.on_connect = self.on_connect
        self.client.on_disconnect = self.on_disconnect

        # Connect to the broker
        self.client.connect(self.BROKER_ADDRESS, self.BROKER_PORT, keepalive=60)

        # Start the MQTT client loop in a separate thread
        self.client.loop_start()

    def setup_logging(self):
        # Check if the log directory exists, if not, create it
        if not os.path.exists(self.LOG_DIR):
            os.makedirs(self.LOG_DIR)

        # Construct the log filename in the logs directory
        log_filename = os.path.join(self.LOG_DIR, f'debug_{datetime.now().strftime("%Y%m%d_%H%M%S")}.log')

        # Create a logger
        self.logger = logging.getLogger('TAT-C_Evaluator')
        self.logger.setLevel(logging.DEBUG)

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
        self.logger.addHandler(fh)
        self.logger.addHandler(ch)

        # Log a startup message to confirm logging is working
        self.logger.info("Starting TAT-C HTTP server... Log file is initialized.")

    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            self.logger.info("Connected to MQTT Broker!")
        else:
            self.logger.error(f"Failed to connect to MQTT Broker, return code {rc}")

    def on_disconnect(self, client, userdata, rc):
        if rc == 0:
            self.logger.info("Disconnected from MQTT Broker gracefully.")
        else:
            self.logger.warning(f"Unexpected disconnection from MQTT Broker. Return code: {rc}")

    def execute_function(self, function_name, architecture):
        """
        Executes the specified function with the given architecture.

        :param function_name: Name of the function to execute.
        :param architecture: The architecture data.
        :return: Result data from the function execution.
        """
        function = self.IMPLEMENTED_FUNCTIONS.get(function_name)
        if function:
            self.logger.debug(f"Executing function: {function_name}")
            result = function(architecture)
            self.logger.debug(f"Function {function_name} execution completed.")
            return result
        else:
            raise ValueError(f"Function {function_name} is not implemented.")

    def publish_result(self, result_topic, result):
        """
        Publishes the result to the specified MQTT topic.

        :param result_topic: The MQTT topic to publish the result to.
        :param result: The result data to publish.
        """
        self.client.publish(result_topic, json.dumps(result))
        self.logger.debug(f"Published result to topic {result_topic}: {result}")

# Instantiate the evaluator
evaluator = TATCEvaluator()

# Define the HTTP endpoints
@app.route('/tatc/<function_name>', methods=['POST'])
def handle_request(function_name):
    data = request.get_json()
    try:
        # Check if the message contains the necessary fields
        if 'architecture' in data and 'workflow_id' in data:
            architecture = data['architecture']
            workflow_id = data['workflow_id']
            publish_metrics_topic = data.get('result_topic', 'TSE')
            evaluator.logger.debug(f'Processing architecture ID: {workflow_id}')
            evaluator.logger.debug(f'Function to execute: {function_name}')

            # Execute the requested function
            result_data = evaluator.execute_function(function_name, architecture)

            # Prepare the result message
            result = {
                'evaluator': 'TAT-C',
                'workflow_id': workflow_id,
                'function': function_name,
                'results': result_data
            }

            # Determine the result topic
            result_topic = f"{evaluator.RESULT_TOPIC_PREFIX}/{workflow_id}/{function_name}"

            # Publish the result to MQTT
            if function_name =='CoverageAnalysis':
                evaluator.publish_result(publish_metrics_topic, result)
                evaluator.logger.debug(f"Published result to topic {publish_metrics_topic}: {result}")

            # Also return the result in the HTTP response
            return jsonify(result), 200
        else:
            evaluator.logger.error('Missing required fields in the request.')
            return jsonify({'error': 'Missing required fields in the request.'}), 400
    except Exception as e:
        evaluator.logger.error(f"Error processing request: {str(e)}")
        evaluator.logger.exception("Exception details:")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    # Run the Flask app
    app.run(host='0.0.0.0', port=5000)
