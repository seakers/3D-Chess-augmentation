import json
from neo4j import GraphDatabase

class TSEWorkflowGenerator:
    def __init__(self, neo4j_uri, neo4j_user, neo4j_password):
        # Initialize Neo4j driver
        self.driver = GraphDatabase.driver(neo4j_uri, auth=(neo4j_user, neo4j_password))
        # Initialize data structures
        self.required_functions = set()  # Stores all functions required to calculate the desired metrics
        self.function_tool_map = {}      # Maps functions to the tools that will implement them
        self.dependency_graph = {}       # Stores dependencies between functions (for topological sorting)
        self.metric_function_map = {}    # Maps metrics to the functions that calculate them

    def close(self):
        # Close Neo4j driver
        self.driver.close()

    def execute_query(self, query, parameters=None):
        # Execute Cypher query and return results
        with self.driver.session() as session:
            result = session.run(query, parameters)
            return [record.data() for record in result]

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
                # Select the first available tool (or implement a selection strategy)
                selected_tool = results[0]["tool_name"]
                self.function_tool_map[function_name] = selected_tool
            else:
                raise ValueError(f"Error: No available tools implement function '{function_name}'.")

    def topological_sort_util(self, function_name, visited, stack):
        # Utility function for topological sorting
        visited.add(function_name)
        for dep in self.dependency_graph.get(function_name, []):
            if dep not in visited:
                self.topological_sort_util(dep, visited, stack)
        stack.append(function_name)

    def generate_workflow(self, user_request):
        # Main function to generate the workflow
        desired_metrics = user_request.get("metrics", [])
        tool_constraints = user_request.get("tool_constraints", {})

        # Step 1: Identify required functions
        for metric in desired_metrics:
            functions = self.get_functions_calculating_metric(metric)
            if not functions:
                raise ValueError(f"Error: No functions calculate the metric '{metric}'.")
            for function in functions:
                self.required_functions.add(function)
                self.resolve_dependencies(function)

        # Step 2: Assign tools to functions
        for function in self.required_functions:
            user_tool = None
            if function in tool_constraints:
                user_tool = tool_constraints[function]
            self.assign_tool_to_function(function, user_tool)

        # Step 3: Verify feasibility (additional compatibility checks can be added here)

        # Step 4: Construct workflow sequence (topological sort)
        visited = set()
        stack = []
        for function in self.required_functions:
            if function not in visited:
                self.topological_sort_util(function, visited, stack)
        # Reverse the stack to get the correct order
        workflow_order = stack[::-1]

        # Step 5: Build the workflow in the desired format
        workflow = []
        evaluator_map = {}  # Maps evaluators to their metrics and subscriptions

        # Map functions to evaluators (tools)
        for function in workflow_order:
            tool = self.function_tool_map[function]
            evaluator = tool
            if evaluator not in evaluator_map:
                evaluator_map[evaluator] = {
                    "evaluator": evaluator,
                    "metrics": {},
                    "subscribe": f"evaluators/{evaluator}",
                    "publish": []
                }

        # Map metrics to evaluators
        for metric, functions in self.metric_function_map.items():
            for function in functions:
                tool = self.function_tool_map.get(function)
                if tool:
                    evaluator_map[tool]["metrics"][metric] = tool

        # Determine publish and subscribe relationships
        for evaluator in evaluator_map.values():
            # Determine what this evaluator publishes to
            if evaluator["evaluator"] == "TSE":
                evaluator["publish"] = [f"evaluators/{ev}" for ev in evaluator_map if ev != "TSE"]
            else:
                evaluator["publish"] = "TSE"
                # Determine what this evaluator subscribes to
                dependencies = set()
                for function in self.dependency_graph:
                    if self.function_tool_map[function] == evaluator["evaluator"]:
                        deps = self.dependency_graph.get(function, [])
                        for dep in deps:
                            dep_tool = self.function_tool_map.get(dep)
                            if dep_tool and dep_tool != evaluator["evaluator"]:
                                dependencies.add(f"evaluators/{dep_tool}")
                if dependencies:
                    evaluator["subscribe"] = list(dependencies)
                else:
                    evaluator["subscribe"] = f"evaluators/{evaluator['evaluator']}"

        # Build the workflow list
        workflow = list(evaluator_map.values())

        # Build the evaluation objectives
        objectives = []
        for metric in desired_metrics:
            objective_type = "MAX" if "Benefit" in metric or "Score" in metric else "MIN"
            objectives.append({
                "objectiveName": metric,
                "objectiveType": objective_type
            })

        # Final output
        output = {
            "evaluation": {
                "TSE": {
                    "objectives": objectives,
                    "subscribe": "TSE",
                    "publish": [f"evaluators/{evaluator}" for evaluator in evaluator_map if evaluator != "TSE"]
                },
                "workflow": workflow
            }
        }

        return output

def main():
    # Read user request from input JSON file
    with open('user_request.json', 'r') as infile:
        user_request = json.load(infile)

    # Initialize the workflow generator
    generator = TSEWorkflowGenerator(
        neo4j_uri="neo4j+s://00785431.databases.neo4j.io",
        neo4j_user="neo4j",
        neo4j_password="Akkxk1jF-figNYw_6Ca9bRuOadjZmxXHVKAFIvcqCLM"
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
        with open('workflow_output.json', 'w') as outfile:
            json.dump(output, outfile, indent=4)

        print(f"An error occurred: {error_message}")

    finally:
        # Close the Neo4j connection
        generator.close()

if __name__ == "__main__":
    main()
