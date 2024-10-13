import networkx as nx
import matplotlib.pyplot as plt

# Create a directed graph (DiGraph)
G = nx.DiGraph()

# Define nodes representing tools
tools = [
    "TSE", "VASSAR", "instruPy", "orbitPy", "OREKIT", "TAT-C", "SpaDes"
]
G.add_nodes_from(tools)

# Define edges representing requests and responses with updated topics, including color (red for subscribe, blue for publish)
edges = [
    ("TSE", "VASSAR", "request science", "blue"),
    ("VASSAR", "TSE", "response science", "red"),
    ("TSE", "SpaDes", "request cost", "blue"),
    ("SpaDes", "TSE", "response lifecycle cost", "red"),
    ("TSE", "TAT-C", "request coverage", "blue"),
    ("TAT-C", "TSE", "response coverage", "red"),
    ("VASSAR", "instruPy", "request instruments", "blue"),
    ("instruPy", "VASSAR", "response instruments", "red"),
    ("VASSAR", "orbitPy", "request coverage", "blue"),
    ("orbitPy", "VASSAR", "response coverage", "red"),
    ("orbitPy", "OREKIT", "request propagation", "blue"),
    ("OREKIT", "orbitPy", "response propagation", "red"),
    ("VASSAR", "TAT-C", "request science", "blue"),
    ("TAT-C", "VASSAR", "response science", "red"),
]

# Add edges to the graph
for edge in edges:
    G.add_edge(edge[0], edge[1], label=edge[2], color=edge[3])

# Set layout for graph visualization with more spread-out nodes
pos = nx.spring_layout(G, seed=42, k=2.5)

# Draw the nodes
plt.figure(figsize=(18, 12))
nx.draw_networkx_nodes(G, pos, node_color="lightblue", node_size=3000)

# Draw the edges with specified colors
edge_colors = [G[u][v]['color'] for u, v in G.edges()]
nx.draw_networkx_edges(G, pos, edgelist=G.edges(), edge_color=edge_colors, arrows=True, arrowstyle='-|>', arrowsize=20)

# Draw labels for nodes and edges
nx.draw_networkx_labels(G, pos, font_size=12, font_weight="bold")
edge_labels = {(edge[0], edge[1]): edge[2] for edge in edges}
nx.draw_networkx_edge_labels(G, pos, edge_labels=edge_labels, font_size=9, label_pos=0.5, bbox=dict(facecolor='white', edgecolor='none'))

plt.title("TSE Evaluation Workflow Graph with Directed Dependencies", fontsize=15)
plt.show()




# Evaluators and their capabilities defined by the user/system
# evaluators = {
#     "SpaDes": {"outputs": ["lifecycleCost"]},
#     "TAT-C": {"outputs": ["coverageFraction"], "inputs": ["orbitalData"]},
#     "VASSAR": {"outputs": ["scienceBenefit"], "inputs": ["coverageFraction", "instrumentPerformance"]},
#     "instruPy": {"outputs": ["instrumentPerformance"]}
# }

# # Define the directed graph
# G = nx.DiGraph()

# # Add nodes and edges based on evaluator inputs and outputs
# for evaluator, capabilities in evaluators.items():
#     G.add_node(evaluator, metrics=capabilities["outputs"])

# # Add dependencies between evaluators based on required inputs
# for evaluator, capabilities in evaluators.items():
#     for input_metric in capabilities.get("inputs", []):
#         for other_eval, other_capabilities in evaluators.items():
#             if input_metric in other_capabilities["outputs"]:
#                 G.add_edge(other_eval, evaluator)

# # Visualize or use the directed graph for workflow execution
# print(list(nx.topological_sort(G)))
import pygraphviz as pgv
import matplotlib.pyplot as plt
import matplotlib.image as mpimg

# Create a directed graph using PyGraphviz
G = pgv.AGraph(directed=True)

# Define nodes representing tools
tools = [
    "TSE", "VASSAR", "instruPy", "orbitPy", "OREKIT", "TAT-C", "SpaDes"
]
for tool in tools:
    G.add_node(tool, shape="ellipse", style="filled", color="lightblue")

# Define edges representing requests and responses with updated topics, including color and direction
edges = [
    ("TSE", "VASSAR", "request science"),
    ("VASSAR", "TSE", "response science"),
    ("TSE", "SpaDes", "request cost"),
    ("SpaDes", "TSE", "response cost"),
    ("VASSAR", "instruPy", "request instruments"),
    ("instruPy", "VASSAR", "response instruments"),
    ("VASSAR", "orbitPy", "request coverage"),
    ("orbitPy", "VASSAR", "response coverage"),
    ("VASSAR", "TAT-C", "request coverage"),
    ("TAT-C", "VASSAR", "response coverage"),
    ("orbitPy", "OREKIT", "request propagation"),
    ("OREKIT", "orbitPy", "response propagation"),
    # Self-dependencies
    ("VASSAR", "VASSAR", "science"),
    ("VASSAR", "VASSAR", "instrumentPerformance"),
    ("orbitPy", "orbitPy", "coverage"),
    ("TAT-C", "TAT-C", "coverage"),
    ("TAT-C", "TAT-C", "propagation"),
    ("OREKIT", "OREKIT", "propagation"),
    ("instruPy", "instruPy", "instrumentPerformance"),
    ("SpaDes", "SpaDes", "cost")
]

# Add edges to the graph
for edge in edges:
    G.add_edge(edge[0], edge[1], label=edge[2])

# Render the graph
G.graph_attr['label'] = "Directed Workflow Graph for Evaluation with Self-Dependencies"
G.graph_attr['labelloc'] = "t"
G.layout(prog="dot")
G.draw("/mnt/data/directed_workflow_graph.png")

# Show the generated graph image
img = mpimg.imread("/mnt/data/directed_workflow_graph.png")
plt.figure(figsize=(10, 10))
plt.imshow(img)
plt.axis('off')
plt.show()