from flask import Flask, request, jsonify
import os
from ConstellationDesignMain import evaluate_architecture
import logging
logging.basicConfig(level=logging.DEBUG, filename='debug.log', filemode='w',
                    format='%(asctime)s - %(levelname)s - %(message)s')
app = Flask(__name__)
@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'running'}), 200
@app.route('/evaluate', methods=['POST'])
def evaluate():
    data = request.get_json()
    
    if not data:
        return jsonify({'error': 'Invalid input data'}), 400

    # Extract architecture data and folder path
    architecture = data.get('architecture')
    folder_path = data.get('folderPath')

    if not architecture or not folder_path:
        return jsonify({'error': 'Missing architecture data or folder path'}), 400

    # Perform cost evaluation (replace with your actual logic)
    cost = evaluate_architecture(architecture, folder_path)

    # Return the result
    return jsonify({'cost': float(cost)}), 200

if __name__ == '__main__':
    print("Server running...")
    app.run(host='localhost', port=5000)
