import os
import pandas as pd
import numpy as np
import itertools
import matplotlib.pyplot as plt
from matplotlib import cm
from pymoo.indicators.hv import HV

# USER INPUT SECTION
M = 9  # number of decision variables
L = 4  # number of objectives
objectives_info = {
    'InstrumentScore': 'max',
    'LifecycleCost': 'min',
    'HarmonicMeanRevisitTime': 'min',
    'CoverageFraction': 'max'
}

# Name of the CSV file
csv_file = r"C:\Users\dfornos\Desktop\3D-CHESS-aumentation-MQTT\3D-Chess-augmentation\TSE_Module\tse\results\results_2024-12-12_09-54-22\summary.csv"

# CREATE OUTPUT DIRECTORY
plots_dir = "plots"
if not os.path.exists(plots_dir):
    os.makedirs(plots_dir)

# READ THE CSV  
df = pd.read_csv(csv_file)

# Extract decision variables and objectives
dv_columns = df.columns[:M]
obj_columns = df.columns[M:M+L]

# Check if the objectives in the CSV match the provided mapping
if set(obj_columns) != set(objectives_info.keys()):
    raise ValueError("The objectives in the CSV do not match the provided objective mapping keys.")

# Reorder objectives_info to match obj_columns order
objectives_info_ordered = {col: objectives_info[col] for col in obj_columns}

# Get objectives array
objectives = df[obj_columns].values

# Normalize each objective to [0,1] depending on max or min
objectives_norm = np.zeros_like(objectives, dtype=float)

for j, col in enumerate(obj_columns):
    val = objectives[:, j]
    min_val = np.min(val)
    max_val = np.max(val)
    denom = max_val - min_val
    if denom == 0:
        # All values are the same, set them to a neutral value (e.g., 0.5)
        norm = np.full_like(val, 0.5, dtype=float)
    else:
        # Initial normalization (for max)
        norm = (val - min_val) / denom

    if objectives_info_ordered[col] == 'min':
        # If it's a minimizing objective, flip it so 1 = best (lowest value)
        norm = 1 - norm
    
    objectives_norm[:, j] = norm

# Now, objectives_norm has all objectives in [0,1], where 1 is best and 0 is worst.

# For hypervolume (which assumes minimization), invert them so that 0 is best:
# This means (1 - objectives_norm) so that now smaller is better.
#objectives_for_hv = 1 - objectives_norm
objectives_for_hv =objectives_norm

# Define a reference point slightly beyond [1,1,...,1] after this inversion
ref_point = np.ones(L) * 1.1

# Compute hypervolume evolution
hv = HV(ref_point=ref_point)
hv_evolution = []
for i in range(1, len(objectives_for_hv)+1):
    current_front = objectives_for_hv[:i]
    hv_value = hv.do(current_front)
    hv_evolution.append(hv_value)

# Plot Hypervolume Evolution
plt.figure(figsize=(10,6))
plt.plot(range(1, len(hv_evolution)+1), hv_evolution, marker='o')
plt.xlabel('NFE')
plt.ylabel('Hypervolume')
plt.title('Hypervolume Evolution')
plt.grid(True)
plt.savefig(os.path.join(plots_dir, 'hypervolume_evolution.png'), dpi=300)
plt.close()

# Create scatter plots for each combination of 3 objectives (original scale)
objectives_list = list(obj_columns)

for combo in itertools.combinations(objectives_list, 3):
    o1, o2, o3 = combo

    # Extract the original data for plotting
    data_o1 = df[o1].values
    data_o2 = df[o2].values
    data_o3 = df[o3].values

    plt.figure(figsize=(8,6))
    sc = plt.scatter(data_o1, data_o2, c=data_o3, cmap=cm.viridis, s=50)
    plt.colorbar(sc, label=o3)
    plt.xlabel(o1)
    plt.ylabel(o2)
    plt.title(f'Scatter Plot of {o1} vs {o2} colored by {o3}')
    plt.grid(True)
    filename = f'scatter_{o1}_{o2}_{o3}.png'
    plt.savefig(os.path.join(plots_dir, filename), dpi=300)
    plt.close()

print("All plots saved in the 'plots' directory.")
