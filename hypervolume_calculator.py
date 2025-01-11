import os
import pandas as pd
import numpy as np
import itertools
import matplotlib.pyplot as plt
from matplotlib import cm
from pymoo.indicators.hv import HV

# USER INPUT SECTION
M = 11  # Expected number of decision variables
L = 3  # Expected number of objectives
objectives_info = {
    'ScienceScore': 'max',
    'LifecycleCost': 'min',
    'HarmonicMeanRevisitTime': 'min',
}
folder = "Mission Portofolio"
#folder = "Standard Form"

# Define base directory as the location of this script
base_dir = os.path.dirname(os.path.abspath(__file__))
results_dir = os.path.join(base_dir, folder)
plots_dir = os.path.join(results_dir, "plots")
if not os.path.exists(plots_dir):
    os.makedirs(plots_dir)

# Collect all result folders
result_folders = [os.path.join(results_dir, f) for f in os.listdir(results_dir) if f.startswith("results")]

# Initialize variables for hypervolume and Pareto front aggregation
all_hypervolumes = []
all_pareto_fronts = []

# Process each results folder
for folder in result_folders:
    csv_file = os.path.join(folder, "summary.csv")
    if not os.path.exists(csv_file):
        print(f"Skipping {folder}, no summary.csv found.")
        continue

    # READ THE CSV WITH VARIABLE-LENGTH HANDLING
    with open(csv_file, 'r') as f:
        lines = f.readlines()

    rows = []
    for line in lines:
        line = line.strip()
        if not line or line.startswith("#"):  # Skip empty lines and comments
            continue
        tokens = line.split(",")  # Assume CSV is comma-separated
        try:
            # Convert tokens to floats
            row = list(map(float, tokens))
            rows.append(row)
        except ValueError:
            print(f"Skipping invalid row: {line}")
            continue

    # Convert to a DataFrame with NaN for padding
    max_cols = max(len(row) for row in rows)
    df = pd.DataFrame([row + [None] * (max_cols - len(row)) for row in rows])

    # Extract the last 3 non-NaN columns (objectives) for each row
    objectives = df.apply(lambda row: row.dropna().iloc[-L:].values, axis=1)
    objectives = np.array([obj for obj in objectives if len(obj) == L])  # Filter out incomplete rows

    # Normalize each objective to [0,1] depending on max or min
    objectives_norm = np.zeros_like(objectives, dtype=float)

    for j in range(L):  # Iterate over objectives
        val = objectives[:, j]
        min_val = np.min(val)
        max_val = np.max(val)
        denom = max_val - min_val
        if denom == 0:
            norm = np.full_like(val, 0.5, dtype=float)  # All values are the same
        else:
            norm = (val - min_val) / denom

        if list(objectives_info.values())[j] == 'max':
            norm = 1 - norm  # Flip for maximizing objectives

        objectives_norm[:, j] = norm

    # For hypervolume (assumes minimization), invert so that 0 is best
    objectives_for_hv = objectives_norm

    # Define a reference point slightly beyond [1,1,...,1]
    ref_point = np.ones(L) + 0.05

    # Compute hypervolume evolution
    hv = HV(ref_point=ref_point)
    hv_evolution = []
    for i in range(1, len(objectives_for_hv) + 1):
        current_front = objectives_for_hv[:i]
        hv_value = hv.do(current_front)
        hv_evolution.append(hv_value)

    all_hypervolumes.append(hv_evolution)

    # Extract non-dominated solutions (Pareto front)
    num_solutions = objectives_for_hv.shape[0]

    def is_dominated_by_any(point, others):
        """
        Check if a point is dominated by any other points in a set.
        A point is dominated if there exists another point better or equal in all objectives
        and strictly better in at least one objective.
        """
        return np.any(
            np.all(others <= point, axis=1) &
            np.any(others < point, axis=1)
        )

    # Compute domination for all solutions
    is_dominated = np.array([
        is_dominated_by_any(objectives_for_hv[i], objectives_for_hv) for i in range(num_solutions)
    ])

    # Extract Pareto front (non-dominated solutions)
    pareto_front = objectives[~is_dominated]
    all_pareto_fronts.append(pareto_front)

# Compute average hypervolume evolution
max_length = max(len(hv) for hv in all_hypervolumes)
hv_matrix = np.zeros((len(all_hypervolumes), max_length))

for i, hv in enumerate(all_hypervolumes):
    hv_matrix[i, :len(hv)] = hv

avg_hypervolume = np.mean(hv_matrix, axis=0)
std_hypervolume = np.std(hv_matrix, axis=0)
avg_hypervolume[490:] = [avg_hypervolume[490]]
std_hypervolume[490:] = [std_hypervolume[490]]
# Define number of sigmas for shading
n_sigmas = 2

# Plot average hypervolume evolution with shaded area
plt.figure(figsize=(10, 6))

x_range = range(1, len(avg_hypervolume) + 1)
plt.plot(x_range, avg_hypervolume, label="Average Hypervolume", color="blue", linewidth=3)
plt.fill_between(
    x_range,
    avg_hypervolume - n_sigmas * std_hypervolume,
    avg_hypervolume + n_sigmas * std_hypervolume,
    color="blue",
    alpha=0.2,
    label=f"±{n_sigmas} Sigma"
)

plt.xlabel('NFE')
plt.ylabel('Hypervolume')
plt.title('Average Hypervolume Evolution Across Results')
plt.grid(True)
plt.legend()
plt.savefig(os.path.join(plots_dir, 'average_hypervolume_evolution.png'), dpi=300)
plt.close()
# Combine Pareto fronts
# Combine Pareto fronts
combined_pareto_front = np.vstack(all_pareto_fronts)

# Compute average hypervolume evolution
max_length = max(len(hv) for hv in all_hypervolumes)
hv_matrix = np.zeros((len(all_hypervolumes), max_length))

for i, hv in enumerate(all_hypervolumes):
    hv_matrix[i, :len(hv)] = hv

avg_hypervolume = np.mean(hv_matrix, axis=0)
std_hypervolume = np.std(hv_matrix, axis=0)

# Define number of sigmas for shading
n_sigmas = 2

# Plot average hypervolume evolution with shaded area and scatter plot
plt.figure(figsize=(12, 8))

# Plot the average hypervolume
x_range = range(1, len(avg_hypervolume) + 1)
plt.plot(x_range, avg_hypervolume, label="Average Hypervolume", color="blue")
plt.fill_between(
    x_range,
    avg_hypervolume - n_sigmas * std_hypervolume,
    avg_hypervolume + n_sigmas * std_hypervolume,
    color="blue",
    alpha=0.2,
    label=f"±{n_sigmas} Sigma"
)

# Add scatter plots of Pareto objectives
objectives_list = list(objectives_info.keys())
for combo in itertools.combinations(objectives_list, 3):
    o1, o2, o3 = combo

    # Combined Pareto front values
    data_o1 = combined_pareto_front[:, objectives_list.index(o1)]
    data_o2 = combined_pareto_front[:, objectives_list.index(o2)]
    data_o3 = combined_pareto_front[:, objectives_list.index(o3)]

    plt.figure(figsize=(8, 6))
    sc = plt.scatter(data_o1, data_o2, c=data_o3, cmap=cm.viridis, s=50)
    plt.colorbar(sc, label=o3)
    plt.xlabel(o1)
    plt.ylabel(o2)
    plt.title(f'{o1} vs {o2} colored by {o3}')
    plt.grid(True)
    filename = f'combined_pareto_{o1}_{o2}_{o3}.png'
    plt.savefig(os.path.join(plots_dir, filename), dpi=300)
    plt.close()

# Add labels and legend
plt.xlabel('NFE')
plt.ylabel('Hypervolume')
plt.title('Average Hypervolume Evolution with Pareto Objective Scatter')
plt.grid(True)
plt.legend()
plt.savefig(os.path.join(plots_dir, 'average_hypervolume_with_objectives.png'), dpi=300)
plt.close()

print(f"All plots and average hypervolume saved in the '{plots_dir}' directory.")
