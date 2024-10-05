import networkx as nx
import matplotlib.pyplot as plt

# Create a directed graph for MQTT message flow with colored edges and updated topics
G = nx.DiGraph()

# Define nodes representing tools
tools = [
    "TSE", "VASSAR", "instruPy", "orbitPy", "OREKIT", "TAT-C", "SpaDes"
]
G.add_nodes_from(tools)

# Define edges representing requests and responses with updated topics, including color (red for subscribe, blue for publish)
edges = [
    ("TSE", "VASSAR", "evaluation/request/science", "blue"),
    ("VASSAR", "TSE", "evaluation/response/science", "red"),
    ("TSE", "SpaDes", "evaluation/request/cost", "blue"),
    ("SpaDes", "TSE", "evaluation/response/cost", "red"),
    ("TSE", "TAT-C", "evaluation/request/cov", "blue"),
    ("TAT-C", "TSE", "evaluation/response/cov", "red"),
    ("VASSAR", "instruPy", "evaluation/request/instruments", "blue"),
    ("instruPy", "VASSAR", "evaluation/response/instruments", "red"),
    ("VASSAR", "orbitPy", "evaluation/request/cov", "blue"),
    ("orbitPy", "VASSAR", "evaluation/response/cov", "red"),
    ("orbitPy", "OREKIT", "evaluation/request/prop", "blue"),
    ("OREKIT", "orbitPy", "evaluation/response/prop", "red"),
    ("VASSAR", "TAT-C", "evaluation/request/science", "blue"),
    ("TAT-C", "VASSAR", "evaluation/response/science", "red"),
]

# Add edges to the graph
for edge in edges:
    G.add_edge(edge[0], edge[1], label=edge[2], color=edge[3])

# Set layout for graph visualization with more spread out nodes
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

plt.title("MQTT Message Flow Diagram with Topics (Requests and Responses, Red for Subscribe, Blue for Publish)", fontsize=15)
plt.show()
