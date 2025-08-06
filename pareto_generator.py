import numpy as np
import matplotlib.pyplot as plt
import os
import argparse
from pymoo.util.nds.non_dominated_sorting import NonDominatedSorting

# Constants
OBJECTIVE_THRESHOLD = 0.01 # Minimum acceptable value for any objective

def load_objectives_from_csv(filename, nObjectives):
    with open(filename, 'r') as f:
        header_line = f.readline().strip().split(',')
        objective_names = header_line[-nObjectives:]
        objectives_list = []
        for line in f:
            parts = line.strip().split(',')
            obj = [float(parts[-nObjectives + i]) for i in range(nObjectives)]
            objectives_list.append(obj)
    return np.array(objectives_list), objective_names


def apply_objective_directions(objectives, objectives_behavior):
    # Ensure objectives is 2D array
    if len(objectives.shape) == 1:
        objectives = objectives.reshape(1, -1)
    
    for i, behavior in enumerate(objectives_behavior):
        if behavior.lower() == 'max':
            objectives[:, i] = -objectives[:, i]  # Invert for maximization
    return objectives


def filter_objectives(objectives, behaviors, threshold=OBJECTIVE_THRESHOLD):
    # First convert back to original values for threshold checking
    original_values = objectives.copy()
    for i, behavior in enumerate(behaviors):
        if behavior.lower() == 'max':
            original_values[:, i] = -original_values[:, i]  # Convert back to positive for max objectives
    
    # Create mask for valid solutions
    mask = np.ones(len(objectives), dtype=bool)
    for i in range(len(behaviors)):
        if behaviors[i].lower() == 'max':
            # For maximization objectives, we want values above threshold
            mask &= original_values[:, i] >= threshold
        else:
            # For minimization objectives, we want values above threshold
            mask &= original_values[:, i] >= threshold
    
    # Apply the mask to the original objectives (which are in the transformed space)
    return objectives[mask]


def add_units_to_names(names):
    """Add appropriate units to objective names."""
    units = {
        'LifecycleCost': ' ($M)',
        'HarmonicMeanRevisitTime': ' (h)',
        'RevisitTime': ' (h)'
    }
    return [name + units.get(name, '') for name in names]


def get_pareto_fronts(objectives):
    """Calculate Pareto fronts once and return them."""
    nds = NonDominatedSorting()
    return nds.do(objectives, only_non_dominated_front=False)


def print_pareto_points(objectives, behaviors, names, fronts, filtered=False):
    # Add units to names
    names_with_units = add_units_to_names(names)
    
    print(f"\n{'Filtered ' if filtered else ''}Pareto Points:")
    print("-----------------")
    print("DEBUG: Printing points with indices:")
    for rank, front in enumerate(fronts):
        if rank>0:
            break
        print(f"\nRank {rank + 1} Front:")
        print("-----------------")
        for i, idx in enumerate(front):
            objs = objectives[idx].copy()
            # Convert back to original values for display
            for j, behavior in enumerate(behaviors):
                if behavior.lower() == 'max':
                    objs[j] = -objs[j]
            # Skip if any objective is below threshold (only for filtered view)
            if filtered and any(obj < OBJECTIVE_THRESHOLD for obj in objs):
                continue
            print(f"Point {i + 1} (index {idx}):")
            for j, name in enumerate(names_with_units):
                print(f"  {name}: {objs[j]:.4f}")
            print()

    # Find and print minimum revisit time
    min_revisit = float('inf')
    min_revisit_point = None
    for rank, front in enumerate(fronts):
        for idx in front:
            objs = objectives[idx].copy()
            # Convert back to original values
            for j, behavior in enumerate(behaviors):
                if behavior.lower() == 'max':
                    objs[j] = -objs[j]
            # Skip if any objective is below threshold (only for filtered view)
            if filtered and any(obj < OBJECTIVE_THRESHOLD for obj in objs):
                continue
            # Assuming revisit time is the third objective (index 2)
            if objs[2] < min_revisit:
                min_revisit = objs[2]
                min_revisit_point = objs

    if min_revisit_point is not None:
        print(f"\n{'Filtered ' if filtered else ''}Minimum Revisit Time Solution:")
        print("-----------------------------")
        print(f"Revisit Time: {min_revisit:.4f} h")
        for j, name in enumerate(names_with_units):
            print(f"{name}: {min_revisit_point[j]:.4f}")
        print()


def plot_3d_pareto_combinations(objectives, behaviors, names, max_rank_to_plot, output_dir, fronts, file_path, filtered=False):
    # Add units to names
    names_with_units = add_units_to_names(names)

    os.makedirs(output_dir, exist_ok=True)
    markers = ['o', 's', '^', 'D', 'P', '*', 'X']

    # Load chromosomes from CSV
    chromosomes = []
    with open(file_path, 'r') as f:
        header_line = f.readline().strip().split(',')
        total_columns = len(header_line)
        chrom_cols = total_columns - len(names)
        for line in f:
            parts = line.strip().split(',')
            chrom = parts[:-len(names)]
            chromosomes.append(chrom)

    print(f"\n{'Filtered ' if filtered else ''}Pareto Points with Chromosomes:")
    print("--------------------------------")
    for rank, front in enumerate(fronts):
        if rank >= max_rank_to_plot:
            break
        print(f"\nRank {rank + 1} Front:")
        print("-----------------")
        for i, idx in enumerate(front):
            objs = objectives[idx].copy()
            # Convert back to original values for display
            for j, behavior in enumerate(behaviors):
                if behavior.lower() == 'max':
                    objs[j] = -objs[j]
            
            # Filter out points with objectives below threshold (only for filtered view)
            if filtered and any(obj < OBJECTIVE_THRESHOLD for obj in objs):
                continue
                
            print(f"Point {i + 1} (index {idx}):")
            print("  Chromosome:", chromosomes[idx])
            for j, name in enumerate(names_with_units):
                print(f"  {name}: {objs[j]:.4f}")
            print()

    combos = [
        (0, 1, 2),
        (2, 1, 0),
        (0, 2, 1),
    ]

    for x_idx, y_idx, c_idx in combos:
        plt.figure(figsize=(8, 6))
        for rank, front in enumerate(fronts):
            if rank >= max_rank_to_plot:
                break
            objs = objectives[front].copy()  # Make a copy to avoid modifying original
            # Convert maximization objectives back to positive for display
            for i, behavior in enumerate(behaviors):
                if behavior.lower() == 'max':
                    objs[:, i] = -objs[:, i]
            
            # Filter out points with objectives below threshold (only for filtered view)
            if filtered:
                valid_mask = np.all(objs >= OBJECTIVE_THRESHOLD, axis=1)
                if not np.any(valid_mask):
                    continue
                objs = objs[valid_mask]
                front = front[valid_mask]  # Update front indices to match filtered points
            
            plt.scatter(
                objs[:, x_idx],
                objs[:, y_idx],
                c=objs[:, c_idx],
                cmap='viridis',
                marker=markers[rank % len(markers)],
                s=50,
                edgecolors='k'
            )
        plt.colorbar(label=f'{names_with_units[c_idx]}')
        plt.xlabel(f'{names_with_units[x_idx]}')
        plt.ylabel(f'{names_with_units[y_idx]}')
        plt.title(f"{names_with_units[x_idx]} vs {names_with_units[y_idx]} (Color: {names_with_units[c_idx]})")
        plt.legend()
        plt.grid(True)
        plt.tight_layout()
        plt.savefig(os.path.join(output_dir, f"{'filtered_' if filtered else ''}pareto_{names[x_idx]}_vs_{names[y_idx]}_color_{names[c_idx]}.png"))
        plt.close()


def plot_2d_pareto(objectives, behaviors, names, max_rank_to_plot, output_dir, fronts, file_path, filtered=False):
    # Add units to names
    names_with_units = add_units_to_names(names)

    os.makedirs(output_dir, exist_ok=True)
    plt.figure(figsize=(8, 6))
    colors = ['red', 'orange', 'green', 'blue', 'purple', 'brown', 'pink', 'gray']
    
    # Load chromosomes from CSV
    chromosomes = []
    with open(file_path, 'r') as f:
        header_line = f.readline().strip().split(',')
        total_columns = len(header_line)
        chrom_cols = total_columns - len(names)
        for line in f:
            parts = line.strip().split(',')
            chrom = parts[:-len(names)]
            chromosomes.append(chrom)

    print(f"\n{'Filtered ' if filtered else ''}Pareto Points with Chromosomes:")
    print("--------------------------------")
    for rank, front in enumerate(fronts):
        if rank >= max_rank_to_plot:
            break
        print(f"\nRank {rank + 1} Front:")
        print("-----------------")
        for i, idx in enumerate(front):
            objs = objectives[idx].copy()
            # Convert back to original values for display
            for j, behavior in enumerate(behaviors):
                if behavior.lower() == 'max':
                    objs[j] = -objs[j]
            
            # Filter out points with objectives below threshold (only for filtered view)
            if filtered and any(obj < OBJECTIVE_THRESHOLD for obj in objs):
                continue
                
            print(f"Point {i + 1} (index {idx}):")
            print("  Chromosome:", chromosomes[idx])
            for j, name in enumerate(names_with_units):
                print(f"  {name}: {objs[j]:.4f}")
            print()
    
    for rank, front in enumerate(fronts):
        if rank >= max_rank_to_plot:
            break
        objs = objectives[front].copy()  # Make a copy to avoid modifying original
        # Convert maximization objectives back to positive for display
        for i, behavior in enumerate(behaviors):
            if behavior.lower() == 'max':
                objs[:, i] = -objs[:, i]
        
        # Filter out points with objectives below threshold (only for filtered view)
        if filtered:
            valid_mask = np.all(objs >= OBJECTIVE_THRESHOLD, axis=1)
            if not np.any(valid_mask):
                continue
            objs = objs[valid_mask]
            front = front[valid_mask]  # Update front indices to match filtered points
        
        plt.scatter(
            objs[:, 0],
            objs[:, 1],
            label=f'Rank {rank+1}',
            color=colors[rank % len(colors)],
            s=40,
            edgecolors='k'
        )
    plt.xlabel(f'{names_with_units[0]}')
    plt.ylabel(f'{names_with_units[1]}')
    plt.title(f"Pareto Fronts Ranking (up to Rank {max_rank_to_plot})")
    plt.legend()
    plt.grid(True)
    plt.tight_layout()
    plt.savefig(os.path.join(output_dir, f"{'filtered_' if filtered else ''}pareto_{names[0]}_vs_{names[1]}.png"))
    plt.close()


def format_objectives(obj_values, behaviors):
    formatted = []
    for i, val in enumerate(obj_values):
        if behaviors[i].lower() == 'max':
            formatted.append(-val)  # Convert back to positive for display
        else:
            formatted.append(val)
    return formatted


def extract_representative_chromosomes(filename, nObjectives, behaviors):
    objectives_list = []
    chromosomes_list = []
    with open(filename, 'r') as f:
        header_line = f.readline().strip().split(',')
        total_columns = len(header_line)
        chrom_cols = total_columns - nObjectives

        for line in f:
            parts = line.strip().split(',')
            chrom = parts[:-nObjectives]
            obj = [float(parts[-nObjectives + i]) for i in range(nObjectives)]
            chromosomes_list.append(chrom)
            objectives_list.append(obj)

    objectives = np.array(objectives_list).astype(float)

    # Apply objective directions
    for i, behavior in enumerate(behaviors):
        if behavior.lower() == 'max':
            objectives[:, i] = -objectives[:, i]

    # Filter objectives
    objectives = filter_objectives(objectives, behaviors, threshold=OBJECTIVE_THRESHOLD)
    if len(objectives) == 0:
        print("No valid objectives after filtering.")
        return

    # Pareto sorting
    nds = NonDominatedSorting()
    fronts = nds.do(objectives, only_non_dominated_front=False)

    rank1_indices = fronts[0]
    rank1_objs = objectives[rank1_indices]
    rank1_chroms = [chromosomes_list[i] for i in rank1_indices]

    # Find best solutions for each objective
    best_indices = []
    for i in range(nObjectives):
        if behaviors[i].lower() == 'max':
            # For maximization, we want the largest value (which is most negative in our transformed space)
            best_idx = np.argmin(rank1_objs[:, i])
        else:
            # For minimization, we want the smallest value
            best_idx = np.argmin(rank1_objs[:, i])
        best_indices.append(best_idx)

    print("\nRepresentative Chromosomes from Rank 1:")
    for i, idx in enumerate(best_indices):
        print(f"- Best {behaviors[i].upper()} Chromosome (Objective {i}):")
        print("  Chromosome:", rank1_chroms[idx])
        print("  Objectives:", format_objectives(rank1_objs[idx], behaviors))
        print()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--file', type=str, required=True, help='Path to summary CSV')
    parser.add_argument('--nobj', type=int, required=True, help='Number of objectives')
    parser.add_argument('--behaviors', type=str, nargs='+', required=True, help='List of behaviors: max or min per objective')
    parser.add_argument('--rank', type=int, default=3, help='Max rank to plot')
    parser.add_argument('--output_dir', type=str, default='pareto_plots', help='Output directory for plots')
    args = parser.parse_args()

    assert len(args.behaviors) == args.nobj, "Length of behaviors must match number of objectives."

    objectives, objective_names = load_objectives_from_csv(args.file, args.nobj)
    objectives = apply_objective_directions(objectives, args.behaviors)

    # Calculate Pareto fronts once
    fronts = get_pareto_fronts(objectives)
    print("\nDEBUG: All Pareto fronts indices:")
    for i, front in enumerate(fronts):
        print(f"Rank {i + 1}: {front}")

    # First show all Pareto points
    print_pareto_points(objectives, args.behaviors, objective_names, fronts, filtered=False)
    
    # Then show filtered Pareto points
    print_pareto_points(objectives, args.behaviors, objective_names, fronts, filtered=True)

    if args.nobj == 3:
        # Plot all Pareto points
        plot_3d_pareto_combinations(objectives, args.behaviors, objective_names, args.rank, args.output_dir, fronts, args.file, filtered=False)
        # Plot filtered Pareto points
        plot_3d_pareto_combinations(objectives, args.behaviors, objective_names, args.rank, args.output_dir, fronts, args.file, filtered=True)
    elif args.nobj == 2:
        # Plot all Pareto points
        plot_2d_pareto(objectives, args.behaviors, objective_names, args.rank, args.output_dir, fronts, args.file, filtered=False)
        # Plot filtered Pareto points
        plot_2d_pareto(objectives, args.behaviors, objective_names, args.rank, args.output_dir, fronts, args.file, filtered=True)
    else:
        print("Only 2 or 3 objectives are supported.")
    extract_representative_chromosomes(args.file, args.nobj, args.behaviors)


if __name__ == '__main__':
    main()
