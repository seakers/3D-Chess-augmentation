from flask import Flask, request, jsonify
import os
from ConstellationDesignMain import evaluate_architecture
import logging
import datetime
# Configure logging
logging.basicConfig(level=logging.DEBUG, filename=f'Evaluators_Module/SpaDes/logs/debug_{datetime.datetime.now().strftime("%Y%m%d_%H%M%S")}.log', filemode='w',
                    format='%(asctime)s - %(levelname)s - %(message)s')

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health_check():
    logging.debug('Health check endpoint called.')
    return jsonify({'status': 'running'}), 200

@app.route('/evaluate', methods=['POST'])
def evaluate():
    logging.debug('Received request at /evaluate endpoint.')
    data = request.get_json()

    # Log the received data for debugging purposes
    logging.debug(f'Request data: {data}')
    
    if not data:
        logging.error('Invalid input data: No JSON body received.')
        return jsonify({'error': 'Invalid input data'}), 400

    # Extract architecture data and folder path
    architecture = data.get('architecture')
    folder_path = data.get('folderPath')

    logging.debug(f'Extracted architecture: {architecture}')
    logging.debug(f'Extracted folder path: {folder_path}')

    if not architecture or not folder_path:
        logging.error('Missing architecture data or folder path.')
        return jsonify({'error': 'Missing architecture data or folder path'}), 400

    try:
        # Perform cost evaluation
        logging.debug('Starting cost evaluation...')
        cost = evaluate_architecture(architecture, folder_path)
        logging.debug(f'Cost evaluation completed. Result: {cost}')

        # Return the result
        return jsonify({'cost': float(cost)}), 200

    except Exception as e:
        logging.error(f'Error during evaluation: {str(e)}')
        return jsonify({'error': 'Evaluation failed', 'details': str(e)}), 500

if __name__ == '__main__':
    logging.debug('Starting SpaDes server...')
    print("Server running...")
    app.run(host='localhost', port=5000)
