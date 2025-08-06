import pandas as pd
import matplotlib.pyplot as plt
import os

# Load the data
#file_path = os.path.join('TSE_Module', 'tse', 'results', 'results_2024-10-30_22-24-31', 'summary.csv')
#file_path = os.path.join('TSE_Module', 'tse', 'results', 'results_2024-12-02_19-11-15', 'summary.csv')
file_path = os.path.join('TSE_Module', 'tse', 'results', 'results_2024-12-03_14-24-16', 'summary.csv')

data = pd.read_csv(file_path)

# Rename columns for clarity
# data.columns = [
#     'ArchId', 'ConstellationType', 'Inclination', 'Altitude', 'NumSats', 'NumPlanes', 'SatIds', 
#     'NumStations', 'GsIds', 'ExecTime', 'ApertureDia', 'BitsPerPixel', 'FocalLength', 
#     'NumDetectorsColsCrossTrack', 'InstrumentScore', 'LifecycleCost', 'HarmonicMeanRevisitTime', 
#     'CoverageFraction'
# ]
# data.columns = [
#     'ArchId', 'inclination', 'altitude', 'payload', 'ScienceScore', 'LifecycleCost', 'HarmonicMeanRevisitTime'
# ]
data.columns = [
'archIndex','inclination','altitude','numberPlanes',
'apertureDia','numberSatellites','bitsPerPixel','numberOfDetectorsRowsAlongTrack',
'dataRate','focalLength','InstrumentScore','LifecycleCost',
'HarmonicMeanRevisitTime','CoverageFraction'
]
# Adjust units
#data['CoverageFraction'] *= 100  # Convert to percentage

# Define plot configurations for each subset of 3 objectives with updated units in labels
# plot_configs = [
#     {
#         'x': 'LifecycleCost',
#         'y': 'InstrumentScore',
#         'color': 'HarmonicMeanRevisitTime',
#         'xlabel': 'Lifecycle Cost ($M)',
#         'ylabel': 'Instrument Performance',
#         'colorlabel': 'Harmonic Mean Revisit Time (Hours)',
#         'filename': 'cost_vs_instrument_performance_color_revisit_time.png'
#     },
#     {
#         'x': 'LifecycleCost',
#         'y': 'InstrumentScore',
#         'color': 'CoverageFraction',
#         'xlabel': 'Lifecycle Cost ($M)',
#         'ylabel': 'Instrument Performance',
#         'colorlabel': 'Coverage Fraction (%)',
#         'filename': 'cost_vs_instrument_performance_color_coverage.png'
#     },
#     {
#         'x': 'LifecycleCost',
#         'y': 'HarmonicMeanRevisitTime',
#         'color': 'InstrumentScore',
#         'xlabel': 'Lifecycle Cost ($M)',
#         'ylabel': 'Harmonic Mean Revisit Time (Hours)',
#         'colorlabel': 'Instrument Performance',
#         'filename': 'cost_vs_revisit_time_color_instrument_performance.png'
#     },
#     {
#         'x': 'LifecycleCost',
#         'y': 'HarmonicMeanRevisitTime',
#         'color': 'CoverageFraction',
#         'xlabel': 'Lifecycle Cost ($M)',
#         'ylabel': 'Harmonic Mean Revisit Time (Hours)',
#         'colorlabel': 'Coverage Fraction (%)',
#         'filename': 'cost_vs_revisit_time_color_coverage.png'
#     },
#     {
#         'x': 'LifecycleCost',
#         'y': 'CoverageFraction',
#         'color': 'InstrumentScore',
#         'xlabel': 'Lifecycle Cost ($M)',
#         'ylabel': 'Coverage Fraction (%)',
#         'colorlabel': 'Instrument Performance',
#         'filename': 'cost_vs_coverage_color_instrument_performance.png'
#     },
#     {
#         'x': 'LifecycleCost',
#         'y': 'CoverageFraction',
#         'color': 'HarmonicMeanRevisitTime',
#         'xlabel': 'Lifecycle Cost ($M)',
#         'ylabel': 'Coverage Fraction (%)',
#         'colorlabel': 'Harmonic Mean Revisit Time (Hours)',
#         'filename': 'cost_vs_coverage_color_revisit_time.png'
#     }
# ]
# plot_configs = [
#     {
#         'x': 'LifecycleCost',
#         'y': 'ScienceScore',
#         'color': 'HarmonicMeanRevisitTime',
#         'xlabel': 'Lifecycle Cost ($M)',
#         'ylabel': 'ScienceBenefit',
#         'colorlabel': 'Harmonic Mean Revisit Time (Hours)',
#         'filename': 'assigning_problem_results.png'
#     },
# ]
plot_configs = [
    {
        'x': 'LifecycleCost',
        'y': 'InstrumentScore',
        'color': 'HarmonicMeanRevisitTime',
        'xlabel': 'Lifecycle Cost ($M)',
        'ylabel': 'ScienceBenefit',
        'colorlabel': 'Harmonic Mean Revisit Time (Hours)',
        'filename': 'firesat_problem_results.png'
    },
]


# Generate and save each plot
for config in plot_configs:
    plt.figure(figsize=(10, 6))
    scatter = plt.scatter(
        data[config['x']], data[config['y']], 
        c=data[config['color']], cmap='viridis', edgecolor='k', alpha=0.7
    )
    plt.colorbar(scatter, label=config['colorlabel'])
    plt.xlabel(config['xlabel'])
    plt.ylabel(config['ylabel'])
    plt.title(f"{config['ylabel']} vs {config['xlabel']} colored by {config['colorlabel']}")
    plt.grid(True)
    
    # Save the figure
    plt.savefig(config['filename'], dpi=300)
    plt.close()
