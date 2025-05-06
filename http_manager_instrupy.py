# mqtt_manager.py

import paho.mqtt.client as mqtt
import json
import logging
from datetime import datetime, timedelta, timezone
import os
import threading
import requests  # Added for HTTP communication

# Import the InstruPy specific modules
from instrupy_manager import data_metrics_instrupy, calculate_media_metrics
from eose.datametrics import DataMetricsRequest, DataMetricsResponse
from pydantic import parse_obj_as

class InstruPyEvaluator:
    # Define the directory for logs
    LOG_DIR = os.path.join(os.path.dirname(os.path.abspath(__file__)), "logs")

    # MQTT Broker details
    BROKER_ADDRESS = 'localhost'  # Replace with your broker address
    BROKER_PORT = 1883
    CLIENT_ID = 'InstruPy_Evaluator'
    RESULT_TOPIC_PREFIX = 'evaluators/InstruPy/results'

    # Implemented functions and metrics (hardcoded)
    IMPLEMENTED_FUNCTIONS = {
        'InstrumentModel': data_metrics_instrupy,
    }

    METRICS = ['IncidenceAngle', 'LookAngle', 'ObservationRange', 'SolarZenith', 'InstrumentScore']

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
        self.client.on_message = self.on_message
        self.client.on_disconnect = self.on_disconnect

    def setup_logging(self):
        # Check if the log directory exists, if not, create it
        if not os.path.exists(self.LOG_DIR):
            os.makedirs(self.LOG_DIR)

        # Construct the log filename in the logs directory
        log_filename = os.path.join(self.LOG_DIR, f'debug_{datetime.now().strftime("%Y%m%d_%H%M%S")}.log')

        # Create a logger
        self.logger = logging.getLogger('InstruPy_Evaluator')
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
        self.logger.info("Starting InstruPy MQTT client... Log file is initialized.")

    def start(self):
        # Connect to the broker
        self.client.connect(self.BROKER_ADDRESS, self.BROKER_PORT, keepalive=60)

        # Start the MQTT client loop
        self.client.loop_forever()

    def on_connect(self, client, userdata, flags, rc):
        if rc == 0:
            self.logger.info("Connected to MQTT Broker!")
            # Subscribe to all implemented function topics
            for function_name in self.IMPLEMENTED_FUNCTIONS.keys():
                topic = f'evaluators/InstruPy/{function_name}'
                client.subscribe(topic)
                self.logger.info(f"Subscribed to topic: {topic}")
        else:
            self.logger.error(f"Failed to connect, return code {rc}")

    def on_message(self, client, userdata, msg):
        self.logger.debug(f"Message received on topic {msg.topic}: {msg.payload.decode()}")
        try:
            data = json.loads(msg.payload.decode())
            # Extract the function name from the topic
            topic_parts = msg.topic.split('/')
            if len(topic_parts) >= 3:
                function_name = topic_parts[2]
                if function_name in self.IMPLEMENTED_FUNCTIONS:
                    # Start a new thread to process each message
                    threading.Thread(target=self.process_request, args=(function_name, data)).start()
                else:
                    self.logger.error(f"Received request for unknown function: {function_name}")
            else:
                self.logger.error(f"Invalid topic format: {msg.topic}")
        except Exception as e:
            self.logger.error(f"Error processing message: {str(e)}")
            self.logger.exception("Exception details:")

    def on_disconnect(self, client, userdata, rc):
        if rc == 0:
            self.logger.info("Disconnected from MQTT Broker gracefully.")
        else:
            self.logger.warning(f"Unexpected disconnection from MQTT Broker. Return code: {rc}")

    def process_request(self, function_name, data):
        try:
            # Check if the message contains the necessary fields
            if 'workflow_id' in data and 'result_topic' in data:
                workflow_id = data['workflow_id']
                publish_metrics_topic = data['result_topic']
                self.logger.debug(f'Processing request for workflow ID: {workflow_id}')
                self.logger.debug(f'Function to execute: {function_name}')
                architecture = data['architecture']
                # Extract the dependencies
                dependencies = data.get('dependencies')
                # Execute the requested function
                result_data = self.execute_function(function_name, dependencies, architecture)

                # Prepare the result message
                result = {
                    'evaluator': 'InstruPy',
                    'workflow_id': workflow_id,
                    'function': function_name,
                    'results': result_data,
                }

                # Determine the result topic
                result_topic = f"{self.RESULT_TOPIC_PREFIX}/{function_name}"

                # Publish the result
                self.client.publish(result_topic, json.dumps(result))
                self.logger.debug(f"Published result to topic {result_topic}: {result}")
                self.client.publish(publish_metrics_topic, json.dumps(result))
                self.logger.debug(f"Published result to topic {publish_metrics_topic}: {result}")
            else:
                self.logger.error('Missing required fields in the message.')
        except Exception as e:
            self.logger.error(f"Error processing request: {str(e)}")
            self.logger.exception("Exception details:")

    def execute_function(self, function_name, dependencies, architecture):
        """
        Executes the specified function with the given request, handling dependencies via HTTP.

        :param function_name: Name of the function to execute.
        :param dependencies: A dictionary of dependencies required for the function.
        :param architecture: The architecture data.
        :return: Result data from the function execution.
        """
        function = self.IMPLEMENTED_FUNCTIONS.get(function_name)
        if not function:
            raise ValueError(f"Function {function_name} is not implemented.")

        self.logger.debug(f"Executing function: {function_name}")

        dependency_results = {}

        # For each dependency, send HTTP request and get the result
        if dependencies and function_name in dependencies:
            for dependency_name in dependencies[function_name]['dependencies']:
                # Get the URL for the dependency
                dependency_url = dependencies[function_name]['dependencies'][dependency_name]
                self.logger.debug(f"Requesting dependency {dependency_name} from {dependency_url}")

                # Build the request payload
                request_payload = {
                    'architecture': architecture,
                    'workflow_id': architecture.get('workflow_id', 0),
                    'function': dependency_name,
                }

                try:
                    # Send the request to the dependency via HTTP POST
                    response = requests.post(dependency_url, json=request_payload)
                    response.raise_for_status()  # Raise an exception for HTTP errors

                    # Parse the response
                    result_data = response.json()
                    dependency_results[dependency_name] = json.loads(result_data['results'][0])

                    self.logger.debug(f"Received result for dependency {dependency_name}: {result_data}")

                except requests.exceptions.RequestException as e:
                    self.logger.error(f"Error requesting dependency {dependency_name}: {str(e)}")
                    self.logger.exception("Exception details:")
                    raise
        date_str = architecture.get("mission").get("start")
        year = int(date_str[0:4])
        month = int(date_str[5:7])
        day = int(date_str[8:10])
        mission_start=datetime(year, month, day, tzinfo=timezone.utc)
            # Extract number of days (assumes format always includes 'D')
        days_part = architecture.get("mission").get("duration").split('D')[0]  # 'P0Y0M01'
        days_str = days_part.split('M')[-1]     # '01'
        days = int(days_str)
        hours = days * 24
        mission_duration=timedelta(hours=hours)
        request = DataMetricsRequest(
            start=mission_start,
            duration=mission_duration,
            **dependency_results["AccessResponse"],
            **dependency_results["OrbitPropagation"]
        )
        # All dependencies have been resolved, execute the main function
        result = function(request)
        self.logger.debug(f"Function {function_name} execution completed.")
        return result

if __name__ == '__main__':
    evaluator = InstruPyEvaluator()
    evaluator.start()
