# from SpacecraftDesignSelectionConstellation import *
# from CostEstimationJSON import loadJSONCostEstimation
# from CallCoverageAnalysis import tatcCovReqTransformer
# import argparse
# import logging

# logging.basicConfig(level=logging.DEBUG, filename='debug.log', filemode='w',
#                     format='%(asctime)s - %(levelname)s - %(message)s')
# def modify_lifecycle_cost(json_file_path, total_mission_costs):
#     # Open the JSON file
#     with open(json_file_path, 'r') as file:
#         data = json.load(file)
    
#     # Update the lifecycleCost estimate with the total mission cost
#     data['lifecycleCost']['estimate'] = total_mission_costs
#     data['lifecycleCost']['fiscalYear'] = 2024
    
#     # Save the updated JSON back to the file
#     with open(json_file_path, 'w') as file:
#         json.dump(data, file, indent=4)
    
#     print("Lifecycle cost updated with total mission costs:", total_mission_costs)
# if __name__ == "__main__":
#     # Initialize argument parser
#     parser = argparse.ArgumentParser(
#         description='Run tradespace search executive'
#     )
    
#     # Add argument for the input JSON file
#     parser.add_argument(
#         'infile',
#         type=argparse.FileType('r'),  # FileType creates a file object
#         help="Tradespace search input JSON file"
#     )
    
#     # Parse the arguments
#     args = parser.parse_args()
    
#     # Extract the file path from the file object
#     archFolderPath = args.infile.name
#     logging.debug(f"Architecture JSON folder path: {archFolderPath}")    

#     archPath = os.path.join(archFolderPath,'arch.json')
#     costRiskPath = os.path.join(archFolderPath,'CostRisk_output.json')
#     logging.debug(f"Architecture JSON path: {archPath}")    
#     # Call the function with the file path (string), not the file object
#     scMasses, subsMasses, constComponents, costEstimationJSONFile = loadJSONConst(archPath)
    
#     totalMissionCosts = loadJSONCostEstimation(costEstimationJSONFile)
#     modify_lifecycle_cost(costRiskPath,sum(totalMissionCosts))
#     print("Total Mission Costs: ", totalMissionCosts)


# ConstellationDesignMain.py

from SpacecraftDesignSelectionConstellation import *
from CostEstimationJSON import loadJSONCostEstimation
from CallCoverageAnalysis import tatcCovReqTransformer
import argparse
import logging
import json
import os

logging.basicConfig(level=logging.DEBUG, filename='debug.log', filemode='w',
                    format='%(asctime)s - %(levelname)s - %(message)s')

def evaluate_architecture(arch_json, arch_folder_path):
    arch_path = os.path.join(arch_folder_path, 'arch.json')
    #cost_risk_path = os.path.join(arch_folder_path, 'CostRisk_output.json')
    #logging.debug(f"Architecture JSON path: {arch_path}")    
    
    # Call the function with the file path (string), not the file object
    sc_masses, subs_masses, const_components, cost_estimation_json_file = loadJSONConst(arch_json)
    
    total_mission_costs = loadJSONCostEstimation(cost_estimation_json_file)
    print("Total Mission Costs: ", total_mission_costs)
    # Return any results if needed
    return sum(total_mission_costs)

# Keep the original main block for standalone execution
if __name__ == "__main__":
    # Initialize argument parser
    # parser = argparse.ArgumentParser(
    #     description='Run tradespace search executive'
    # )
    
    # # Add argument for the input JSON file
    # parser.add_argument(
    #     'infile',
    #     type=argparse.FileType('r'),  # FileType creates a file object
    #     help="Tradespace search input JSON file"
    # )
    
    # # Parse the arguments
    # args = parser.parse_args()
    
    # # Extract the file path from the file object
    # arch_folder_path = args.infile.name
    current_dir = os.path.abspath(os.path.join('..','..'))
    print(current_dir)
    arch_folder_path = os.path.join(current_dir,"TSE_Module","tse","problems","arch-0")
    jsonPath = os.path.join(arch_folder_path,"arch.json")
    jsonFile = open(jsonPath)
    jsonDict = json.load(jsonFile)
    logging.debug(f"Architecture JSON folder path: {arch_folder_path}")
    # Call the evaluate_architecture function
    cost = evaluate_architecture(jsonDict,arch_folder_path)
    print(f"Total cost: {cost}")

