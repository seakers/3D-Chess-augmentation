import requests
import json
import time
from flask import Flask, request, jsonify

# Endpoint for the TSE HTTP server
TSE_ENDPOINT = "http://localhost:7500/tse"

# Create a Flask app to receive callbacks
app = Flask(__name__)

# Store received solutions
received_solutions = []

@app.route('/callback', methods=['POST'])
def receive_solution():
    solution = request.json
    received_solutions.append(solution)
    print("\nReceived new solution:")
    print(f"Solution ID: {solution['solutionId']}")
    print("Design Variables:")
    for var, value in solution['designVariables'].items():
        print(f"  {var}: {value}")
    print("Objectives:")
    for obj, value in solution['objectives'].items():
        print(f"  {obj}: {value}")
    print("-" * 50)
    return jsonify({"status": "received"})

def run_flask():
    app.run(port=7000)

if __name__ == '__main__':
    import threading
    
    # Start Flask server in a separate thread
    flask_thread = threading.Thread(target=run_flask)
    flask_thread.daemon = True
    flask_thread.start()
    
    # Give Flask server time to start
    time.sleep(1)
    
    # Path to the local JSON file
    #json_path = "TSERequests/TSERequestAssigningSmall.json"
    #json_path = "TSERequests/TSERequestClimateCentricDSPAC_test.json"
    #json_path = "TSERequests/TSERequestAssigning.json"
    json_path = "TSERequests/combining.json"
    
    # Read and stringify the TSERequest JSON
    with open(json_path, "r") as f:
        raw_json = f.read()
    
    # Add callback URL to the request
    request_json = json.loads(raw_json)
    request_json["callbackUrl"] = "http://localhost:7000/callback"
    raw_json = json.dumps(request_json)
    
    print("Sending TSERequest to TSE server...")
    response = requests.post(TSE_ENDPOINT, data=raw_json, headers={'Content-Type': 'application/json'})
    
    print("\nInitial Response from TSE server:")
    print("Status Code:", response.status_code)
    print("Response Body:", response.text)
    
    # Keep the script running to receive callbacks
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\nTotal solutions received:", len(received_solutions))
        print("Exiting...")
