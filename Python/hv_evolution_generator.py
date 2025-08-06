import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from pymoo.util.nds.non_dominated_sorting import NonDominatedSorting
from pymoo.indicators.hv import HV
import os
import argparse

def load_objectives_with_row_nfe(filename, nObjectives):
    with open(filename, 'r') as f:
        header_line = f.readline().strip().split(',')
        objective_names = header_line[-nObjectives:]
        objectives_list = []
        nfe_list = []
        for i, line in enumerate(f):
            parts = line.strip().split(',')
            obj = [float(parts[-nObjectives + j]) for j in range(nObjectives)]
            objectives_list.append(obj)
            nfe_list.append(i + 1)  # Row index as NFE, starting from 1
    return np.array(nfe_list), np.array(objectives_list), objective_names

def apply_objective_directions(objectives, objectives_behavior):
    for i, behavior in enumerate(objectives_behavior):
        if behavior.lower() == 'max':
            objectives[:, i] = -objectives[:, i]
    return objectives

def normalize_objectives(objectives):
    min_vals = np.min(objectives, axis=0)
    max_vals = np.max(objectives, axis=0)
    normalized = (objectives - min_vals) / (max_vals - min_vals + 1e-12)
    ref_point = np.ones(objectives.shape[1]) * 1.1  # Slightly outside normalized box
    return normalized, ref_point

def compute_hv_evolution(nfe_list, objectives, ref_point):
    nds = NonDominatedSorting()
    hv_evolution = []

    for nfe in nfe_list:
        current_objs = objectives[:nfe]  # Take all rows up to this NFE
        fronts = nds.do(current_objs, only_non_dominated_front=True)
        pareto_front = current_objs[fronts[0]]
        hv = HV(ref_point=ref_point).do(pareto_front)
        hv_evolution.append((nfe, hv))

    return np.array(hv_evolution)

def plot_hv_evolution(hv_evolution, output_dir, objective_names):
    os.makedirs(output_dir, exist_ok=True)
    plt.figure(figsize=(8, 6))
    plt.plot(hv_evolution[:, 0], hv_evolution[:, 1], marker='o', color='blue', label='Hypervolume')
    plt.xlabel('Number of Function Evaluations (NFE)')
    plt.ylabel('Normalized Hypervolume')
    plt.title(f'Hypervolume Evolution ({", ".join(objective_names)})')
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, 'hypervolume_evolution_normalized.png'))
    plt.close()

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--file', type=str, required=True, help='Path to summary CSV')
    parser.add_argument('--nobj', type=int, required=True, help='Number of objectives (at end of file)')
    parser.add_argument('--behaviors', type=str, nargs='+', required=True, help='List of behaviors: max or min per objective')
    parser.add_argument('--output_dir', type=str, default='hv_plots', help='Output directory for plots')
    args = parser.parse_args()

    assert len(args.behaviors) == args.nobj, "Length of behaviors must match number of objectives."

    nfe_list, objectives, objective_names = load_objectives_with_row_nfe(args.file, args.nobj)
    objectives = apply_objective_directions(objectives, args.behaviors)

    normalized_objs, ref_point = normalize_objectives(objectives)

    hv_evolution = compute_hv_evolution(nfe_list, normalized_objs, ref_point)

    plot_hv_evolution(hv_evolution, args.output_dir, objective_names)

if __name__ == '__main__':
    main()
