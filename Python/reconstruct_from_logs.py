import os
import numpy as np
import matplotlib.pyplot as plt
from pymoo.util.nds.non_dominated_sorting import NonDominatedSorting
from pymoo.indicators.hv import HV

# --- Settings ---
log_folder = r'C:\Users\dfornos\OneDrive - Texas A&M University\Desktop\3D-CHESS-aumentation-MQTT\3D-Chess-augmentation\TSE_Module\tse\results\results_20250512_000254'
objective_count = 2  # Number of objectives
max_rank_to_plot = 3

# --- Read all log files ---
all_objectives = []
file_order = []

for fname in sorted(os.listdir(log_folder)):
    if fname.startswith("results"):
        path = os.path.join(log_folder, fname)
        with open(path, 'r') as f:
            for line in f:
                if line.strip():
                    parts = line.strip().replace('[','').replace(']','').split(';')
                    if len(parts) >= objective_count:
                        obj = [float(p) for p in parts[:objective_count]]
                        all_objectives.append(obj)
                        file_order.append(fname)

objectives = np.array(all_objectives)

# --- Flip signs if needed ---
# Assume first objective is maximize (flip), second minimize (keep)
objectives[:, 0] = -objectives[:, 0]

# --- Normalize using extrema ---
mins = objectives.min(axis=0)
maxs = objectives.max(axis=0)
norm_objs = (objectives - mins) / (maxs - mins)

# --- Pareto Ranking ---
nds = NonDominatedSorting()
fronts = nds.do(norm_objs, only_non_dominated_front=False)

# --- Hypervolume Evolution ---
hv = HV(ref_point=np.ones(objective_count) * 1.1)  # Reference point slightly worse than worst (normalized)
hv_values = []
evaluations = np.arange(1, len(norm_objs)+1)
current_population = []

for i, obj in enumerate(norm_objs):
    current_population.append(obj)
    front = nds.do(np.array(current_population), only_non_dominated_front=True)[0]
    hv_values.append(hv(np.array(current_population)[front]))

# --- Plot Pareto ---
plt.figure(figsize=(12, 5))

plt.subplot(1, 2, 1)
shapes = ['o', 's', '^', 'P', '*']
colors = ['red', 'orange', 'green', 'blue', 'purple']
for rank, front in enumerate(fronts):
    if rank >= max_rank_to_plot:
        break
    objs = norm_objs[front]
    plt.scatter(-objs[:, 0], objs[:, 1], label=f'Rank {rank+1}', marker=shapes[rank % len(shapes)], color=colors[rank % len(colors)], edgecolors='k', s=50)

plt.xlabel('Science Score (normalized, max)')
plt.ylabel('Lifecycle Cost (normalized, min)')
plt.title(f'Pareto Fronts up to Rank {max_rank_to_plot}')
plt.legend()
plt.grid(True)

# --- Plot Hypervolume ---
plt.subplot(1, 2, 2)
plt.plot(evaluations, hv_values, label='Hypervolume')
plt.xlabel('NFE (from file line order)')
plt.ylabel('Normalized Hypervolume')
plt.title('Hypervolume Evolution')
plt.grid(True)
plt.legend()

plt.tight_layout()
plt.show()
