import json
from neo4j import GraphDatabase
from dotenv import load_dotenv
import os
class TSEWorkflowGenerator:
    
    def __init__(self, neo4j_uri, neo4j_user, neo4j_password):
        self.driver = GraphDatabase.driver(neo4j_uri, auth=(neo4j_user, neo4j_password))
        self.required_functions = set()  # Stores all functions required to calculate the desired metrics
        self.function_tool_map = {}      # Maps functions to the tools that will implement them
        self.dependency_graph = {}       # Stores dependencies between functions (for topological sorting)
        self.metric_function_map = {}    # Maps metrics to the functions that calculate them
        self.function_levels = {}        # Stores the level of each function
    def close(self):
        # Close Neo4j driver
        self.driver.close()

    def execute_query(self, query, parameters=None):
        with self.driver.session() as session:
            result = session.run(query, parameters)
            return [record.data() for record in result]
    def get_tool_http_address(self, tool_name):
        query = """
        MATCH (t:Tool {name: $tool_name})
        RETURN t.httpAddress AS httpAddress
        """
        result = self.execute_query(query, {"tool_name": tool_name})
        
        if result and result[0].get("httpAddress"):
            return result[0]["httpAddress"]
        else:
            raise ValueError(f"No httpAddress found for tool '{tool_name}' or tool does not exist.")
    
    def get_functions_calculating_metric(self, metric_name):
        # Query functions that calculate the given metric
        query = """
        MATCH (f:Function)-[:CALCULATES]->(m:Metric {name: $metric_name})
        RETURN f.name AS function_name
        """
        results = self.execute_query(query, {"metric_name": metric_name})
        functions = [record["function_name"] for record in results]
        # Map the metric to the functions that calculate it
        self.metric_function_map[metric_name] = functions
        return functions

    def resolve_dependencies(self, function_name):
        # Recursively resolve function dependencies
        query = """
        MATCH (f:Function {name: $function_name})-[:REQUIRES]->(dep:Function)
        RETURN dep.name AS dependency_name
        """
        results = self.execute_query(query, {"function_name": function_name})
        dependencies = [record["dependency_name"] for record in results]
        self.dependency_graph[function_name] = dependencies
        for dep_function in dependencies:
            if dep_function not in self.required_functions:
                self.required_functions.add(dep_function)
                self.resolve_dependencies(dep_function)

    def assign_tool_to_function(self, function_name, user_specified_tool=None):
        # Assign a tool to a function, considering user constraints
        if user_specified_tool:
            # Verify that the tool implements the function
            query = """
            MATCH (t:Tool {name: $tool_name})-[:IMPLEMENTS]->(f:Function {name: $function_name})
            RETURN t.name AS tool_name
            """
            results = self.execute_query(query, {"tool_name": user_specified_tool, "function_name": function_name})
            if results:
                self.function_tool_map[function_name] = user_specified_tool
            else:
                raise ValueError(f"Error: Tool '{user_specified_tool}' does not implement function '{function_name}'.")
        else:
            # Find available tools that implement the function
            query = """
            MATCH (t:Tool)-[:IMPLEMENTS]->(f:Function {name: $function_name})
            RETURN t.name AS tool_name
            """
            results = self.execute_query(query, {"function_name": function_name})
            if results:
                # Select the first available tool
                selected_tool = results[0]["tool_name"]
                self.function_tool_map[function_name] = selected_tool
            else:
                raise ValueError(f"Error: No available tools implement function '{function_name}'.")

    def compute_levels(self):
        # Compute levels of functions based on dependencies
        def assign_level(function_name, visited):
            if function_name in visited:
                return self.function_levels[function_name]
            visited.add(function_name)
            deps = self.dependency_graph.get(function_name, [])
            if not deps:
                self.function_levels[function_name] = 1
            else:
                self.function_levels[function_name] = 1 + max(assign_level(dep, visited) for dep in deps)
            return self.function_levels[function_name]

        visited = set()
        for function in self.required_functions:
            assign_level(function, visited)
    def infer_tool_dependencies_from_workflow(self, workflow):
        # Initialize the tool dependency graph as a dictionary
        self.tool_dependency_graph = {}
        
        # Loop through each tool (evaluator) in the workflow
        for evaluator in workflow:
            tool_name = evaluator['evaluator']
            self.tool_dependency_graph[tool_name] = []  # Initialize the tool's dependencies
            
            # Get the functions implemented by this tool
            functions = evaluator['implementedFunctions']
            
            # Loop through each function to determine its dependencies
            for function_name, function_data in functions.items():
                dependencies = function_data['dependencies']  # Function dependencies
                
                # Loop through dependencies of this function
                for dep_function, dep_tool in dependencies.items():
                    if dep_tool != "self":  # If the dependency comes from another tool
                        self.tool_dependency_graph[tool_name].append(dep_tool.split("/")[1])  # Extract the dependent tool name

        return self.tool_dependency_graph

    def compute_tool_levels(self, workflow):
        # Infer tool dependencies from the workflow
        self.tool_dependency_graph = self.infer_tool_dependencies_from_workflow(workflow)

        # Compute levels of tools based on the inferred dependencies
        def assign_tool_level(tool_name, visited):
            if tool_name in visited:
                return self.tool_levels[tool_name]
            visited.add(tool_name)
            
            # Get dependencies from the inferred tool dependency graph
            deps = self.tool_dependency_graph.get(tool_name, [])
            if not deps:
                self.tool_levels[tool_name] = 1  # If no dependencies, assign level 1
            else:
                self.tool_levels[tool_name] = 1 + max(assign_tool_level(dep_tool, visited) for dep_tool in deps)
            
            return self.tool_levels[tool_name]

        # Initialize visited set to avoid recomputation and circular dependencies
        visited = set()
        self.tool_levels = {}

        # Loop through all tools in the workflow to assign levels
        for evaluator in workflow:
            tool_name = evaluator['evaluator']
            assign_tool_level(tool_name, visited)

        return self.tool_levels

    def generate_workflow(self, user_request):
        # Main function to generate the workflow
        desired_metrics = user_request.get("metrics", {})
        tool_constraints = user_request.get("tool_constraints", {})

        for metric in desired_metrics.keys():
            functions = self.get_functions_calculating_metric(metric)
            if not functions:
                raise ValueError(f"Error: No functions calculate the metric '{metric}'.")
            for function in functions:
                self.required_functions.add(function)
                self.resolve_dependencies(function)

        for function in self.required_functions:
            user_tool = None
            if function in tool_constraints:
                user_tool = tool_constraints[function]
            self.assign_tool_to_function(function, user_tool)

        self.compute_levels()

        evaluator_map = {}  # Maps evaluators to their data

        for function in self.required_functions:
            tool = self.function_tool_map[function]
            evaluator = tool
            if evaluator not in evaluator_map:
                evaluator_map[evaluator] = {
                    "evaluator": evaluator,
                    "metrics": {},
                    "implementedFunctions": {},
                    "subscribe": [],
                    "publish_metrics": [],
                }

        # Map metrics to evaluators
        for metric, functions in self.metric_function_map.items():
            for function in functions:
                tool = self.function_tool_map[function]
                evaluator_map[tool]["metrics"][metric] = function

        # Assign required functions and levels for each evaluator
        for function, tool in self.function_tool_map.items():
            evaluator = tool
            if evaluator_map[evaluator]["implementedFunctions"]:
                implemented_functions = evaluator_map[evaluator]["implementedFunctions"]
            else:
                implemented_functions = {}
            implemented_functions[function] = {
                "dependencies": {}
            }
            dependencies = self.dependency_graph[function]

            if len(dependencies)!=0:
                # Iterate over the dependencies and set their values accordingly
                for dep_function in dependencies:
                    dep_tool=self.function_tool_map[dep_function]
                    implemented_functions[function]["dependencies"][dep_function] = (
                        f"{self.get_tool_http_address(dep_tool)}/{dep_function}" if dep_tool != evaluator else "self"
                    )
            implemented_functions[function]["level"] = self.function_levels[function]

            evaluator_map[evaluator]["subscribe"].append(f"evaluators/{tool}")
            if implemented_functions:
                evaluator_map[evaluator]["implementedFunctions"] = implemented_functions

        for evaluator in evaluator_map.values():
            evaluator["subscribe"] = list(set(evaluator["subscribe"]))
            evaluator["publish_metrics"] = "TSE"

        workflow = list(evaluator_map.values())
        tool_levels = self.compute_tool_levels(workflow)
        publish_map = {}
        for metric, function in self.metric_function_map.items():
            tool = self.function_tool_map[function[0]]
            # Add the metric-to-tool mapping in the publish dictionary
            publish_map[metric] = f"evaluators/{tool}/{function.pop()}"
        objectives = []
        for metric in desired_metrics.keys():
            objective_type = desired_metrics[metric]
            objectives.append({
                "objectiveName": metric,
                "objectiveType": objective_type
            })
        output = {
            "evaluation": {
                "TSE": {
                    "objectives": objectives,
                    "subscribe": "TSE",
                    "publish_metric_requests": publish_map,
                    "tool_levels": tool_levels
                },
                "workflow": workflow
            }
        }

        return output

def main():
    with open('user_request.json', 'r') as infile:
        user_request = json.load(infile)
    load_dotenv(dotenv_path=".env")

    print(os.getenv("NEO4J_URI"))
    print(os.getenv("NEO4J_USERNAME"))
    print(os.getenv("NEO4J_PASSWORD"))
    # Initialize the workflow generator
    generator = TSEWorkflowGenerator(
        neo4j_uri=os.getenv("NEO4J_URI"),
        neo4j_user=os.getenv("NEO4J_USERNAME"),
        neo4j_password=os.getenv("NEO4J_PASSWORD")
    )
    try:
        # Generate the workflow
        output = generator.generate_workflow(user_request)

        # Output the workflow to a JSON file
        with open('workflow_output.json', 'w') as outfile:
            json.dump(output, outfile, indent=4)

        print("Workflow successfully generated and saved to 'workflow_output.json'.")

    except ValueError as e:
        # Handle errors (e.g., infeasible workflow)
        error_message = str(e)
        output = {
            "error": error_message
        }
        with open('workflow_output_test.json', 'w') as outfile:
            json.dump(output, outfile, indent=4)

        print(f"An error occurred: {error_message}")

    finally:
        # Close the Neo4j connection
        generator.close()

if __name__ == "__main__":
    main()
