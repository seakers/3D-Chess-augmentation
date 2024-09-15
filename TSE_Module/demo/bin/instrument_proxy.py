import tatc
import argparse
import os
import csv
import json
import subprocess

"""
The instrument analysis proxy analyzes instrument performance for a given
architecture. It takes two arguments as inputs:
    in_file     A JSON formatted document containing the TradespaceSearch.
    arch_dir    A readable directory containing a JSON formatted document
                containing the Architecture (arch.json) and the location where
                all analysis outputs shall be written.

Instrument analysis writes the following files to the architecture directory:
    coverage_basic_sensor-#.csv  CSV-formatted list of coverage periods
                for a basic sensor-type instrument (1 file per satellite,
                sequential integer ids).
    gbl_basic_sensor.json JSON-formatted document of global performance measures
                for the constellation assuming basic sensor-type instruments.
    lcl_basic_sensor.csv CSV-formatted list of all performance measures local
                to a point of interest for the constellation assuming basic
                sensor-type instruments.
    coverage_optical_scanner-#.csv CSV-formatted list of coverage periods
                for an optical scanner-type instrument (1 file per satellite,
                sequential integer ids).
    gbl_optical_scanner.json JSON-formatted document of global performance
                measures for the constellation assuming optical scanner-type
                instruments.
    lcl_optical_scanner.csv CSV-formatted list of all performance measures local
                to a point of interest for the constellation assuming optical
                scanner-type instruments.
    coverage_synthetic_aperture_radar-#.csv CSV-formatted list of coverage
                periods for a synthetic aperture radar-type instrument (1 file
                per satellite, sequential integer ids).
    gbl_synthetic_aperture_radar.json JSON-formatted document of global
                performance measures for the constellation assuming synthetic
                aperture radar-type instruments.
    lcl_synthetic_aperture_radar.csv CSV-formatted list of all performance
                measures local to a point of interest for the constellation
                assuming synthetic aperture radar-type instruments.
"""

def execute(in_file, arch_dir):
    """Executes the instrument analysis proxy."""
    in_file.seek(0) # reset reading from start of file
    search = tatc.TradespaceSearch.from_json(in_file)
    arch_path = os.path.join(arch_dir, 'arch.json')
    with open(arch_path, 'r') as arch_file:
        arch = tatc.Architecture.from_json(arch_file)
    if hasattr(search.settings, 'proxyInstrument') and search.settings.proxyInstrument:
        keepLowLevelData = False
        if hasattr(search.settings, 'outputs') and hasattr(search.settings.outputs, 'keepLowLevelData'):
            keepLowLevelData = search.settings.outputs.keepLowLevelData
        write_proxy_outputs(arch, arch_dir, keepLowLevelData)
    else:
        ### option 1: subprocess call to instrument module
        # get path to *this* file
        dir_path = os.path.dirname(os.path.realpath(__file__))
        # get path to current working directory (i.e. where script was called)
        cwd_path = os.getcwd()
        # use subprocess.call with list to (more) safely build the shell command
        return_code = subprocess.call([
            'python',
            os.path.join(dir_path, '..', '..', 'modules', 'instrument', 'bin', 'instrument_module.py'),
            os.path.join(cwd_path, arch_dir)
        ])
        if return_code is 1:
            raise RuntimeError('Error executing instrument module')
        ### option 2: direct call to python code in instrument module (note: does not work because bin/ is not set up as an importable package)
        #from ...modules.instrument.bin import instrument_module
        #instrument_module.main(arch_dir)

def write_proxy_outputs(arch, arch_dir, keepLowLevelData):
    """ Writes proxy outputs (placeholder files to establish interface format).

    Args
        arch (Architecture): the architecture to evaluate
        arch_dir (str): writable file directory to write outputs
        keepLowLevelData (bool): preserve low-level data outputs
    """
    # assume only one constellation
    constellation = arch.spaceSegment[0] if isinstance(arch.spaceSegment, list) else arch.spaceSegment
    # assume only one satellite (or all are homogeneous)
    satellite = constellation.satellites[0] if isinstance(constellation.satellites, list) else constellation.satellites
    # assume only one instrument (or all are homogeneous)
    payload = satellite.payload[0] if isinstance(satellite.payload, list) else satellite.payload
    if isinstance(satellite.payload, tatc.OpticalScanner):
        if keepLowLevelData:
            with open(os.path.join(arch_dir, 'level_0_typical_observed_data_metrics.csv'), 'w') as outfile:
                writer = csv.writer(outfile)
                writer.writerow([
                    'eventIdx',
                    'POI index',
                    'Lat[deg]',
                    'Lon[deg]',
                    'Access From [s]',
                    'Access To [s]',
                    'Ground Pixel Along-Track Resolution [m]',
                    'Ground Pixel Cross-Track Resolution [m]',
                    'SNR',
                    'DR',
                    'Noise-Equivalent Delta T [K]',
                    'Coverage [T/F]'
                ])
        with open(os.path.join(arch_dir, 'level_1_typical_observed_data_metrics.csv'), 'w') as outfile:
            writer = csv.writer(outfile)
            writer.writerow([
                'POI index',
                'Lat[deg]',
                'Lon[deg]',
                'Mean of Ground Pixel Along-Track Resolution [m]',
                'Mean of Ground Pixel Cross-Track Resolution [m]',
                'Mean of SNR,Mean of DR,Mean of Noise-Equivalent Delta T [K]',
                'SD of Ground Pixel Along-Track Resolution [m]',
                'SD of Ground Pixel Cross-Track Resolution [m]',
                'SD of SNR',
                'SD of DR',
                'SD of Noise-Equivalent Delta T [K]'
            ])
        with open(os.path.join(arch_dir, 'level_2_typical_observed_data_metrics.csv'), 'w') as outfile:
            writer = csv.writer(outfile)
            writer.writerow([
                'Mean of Mean of Ground Pixel Along-Track Resolution [m]',
                'Mean of Mean of Ground Pixel Cross-Track Resolution [m]',
                'Mean of Mean of SNR',
                'Mean of Mean of DR',
                'Mean of Mean of Noise-Equivalent Delta T [K]',
                'SD of Mean of Ground Pixel Along-Track Resolution [m]',
                'SD of Mean of Ground Pixel Cross-Track Resolution [m]',
                'SD of Mean of SNR',
                'SD of Mean of DR',
                'SD of Mean of Noise-Equivalent Delta T [K]',
                'Mean of SD of Ground Pixel Along-Track Resolution [m]',
                'Mean of SD of Ground Pixel Cross-Track Resolution [m]',
                'Mean of SD of SNR',
                'Mean of SD of DR',
                'Mean of SD of Noise-Equivalent Delta T [K]'
            ])
    elif isinstance(satellite.payload, tatc.SyntheticApertureRadar):
        if keepLowLevelData:
            with open(os.path.join(arch_dir, 'level_0_typical_observed_data_metrics.csv'), 'w') as outfile:
                writer = csv.writer(outfile)
                writer.writerow([
                    'eventIdx',
                    'POI index',
                    'Lat[deg]',
                    'Lon[deg]',
                    'Access From [s]',
                    'Access To [s]',
                    'Ground Pixel Along-Track Resolution [m]',
                    'Ground Pixel Cross-Track Resolution [m]',
                    'Sigma NEZ Nought [dB]',
                    'Incidence angle [deg]',
                    'Swath-width [km]',
                    'Coverage [T/F]'
                ])
        with open(os.path.join(arch_dir, 'level_1_typical_observed_data_metrics.csv'), 'w') as outfile:
            writer = csv.writer(outfile)
            writer.writerow([
                'POI index',
                'Lat[deg]',
                'Lon[deg]',
                'Mean of Ground Pixel Along-Track Resolution [m]',
                'Mean of Ground Pixel Cross-Track Resolution [m]',
                'Mean of Sigma NEZ Nought [dB]',
                'Mean of Incidence angle [deg]',
                'Mean of Swath-width [km]',
                'SD of Ground Pixel Along-Track Resolution [m]',
                'SD of Ground Pixel Cross-Track Resolution [m]',
                'SD of Sigma NEZ Nought [dB]',
                'SD of Incidence angle [deg]',
                'SD of Swath-width [km]'
            ])
        with open(os.path.join(arch_dir, 'level_2_typical_observed_data_metrics.csv'), 'w') as outfile:
            writer = csv.writer(outfile)
            writer.writerow([
                'Mean of Mean of Ground Pixel Along-Track Resolution [m]',
                'Mean of Mean of Ground Pixel Cross-Track Resolution [m]',
                'Mean of Mean of Sigma NEZ Nought [dB]',
                'Mean of Mean of Incidence angle [deg]',
                'Mean of Mean of Swath-width [km]',
                'SD of Mean of Ground Pixel Along-Track Resolution [m]',
                'SD of Mean of Ground Pixel Cross-Track Resolution [m]',
                'SD of Mean of Sigma NEZ Nought [dB]',
                'SD of Mean of Incidence angle [deg]',
                'SD of Mean of Swath-width [km]',
                'Mean of SD of Ground Pixel Along-Track Resolution [m]',
                'Mean of SD of Ground Pixel Cross-Track Resolution [m]',
                'Mean of SD of Sigma NEZ Nought [dB]',
                'Mean of SD of Incidence angle [deg]',
                'Mean of SD of Swath-width [km]'
            ])
    else:
        if keepLowLevelData:
            with open(os.path.join(arch_dir, 'level_0_typical_observed_data_metrics.csv'), 'w') as outfile:
                writer = csv.writer(outfile)
                writer.writerow([
                    'eventIdx',
                    'POI index',
                    'Lat[deg]',
                    'Lon[deg]',
                    'Access From [s]',
                    'Access To [s]',
                    'Observation Range [km]',
                    'Look angle [deg]',
                    'Incidence angle [deg]',
                    'Coverage [T/F]'
                ])
        with open(os.path.join(arch_dir, 'level_1_typical_observed_data_metrics.csv'), 'w') as outfile:
            writer = csv.writer(outfile)
            writer.writerow([
                'POI index',
                'Lat[deg]',
                'Lon[deg]',
                'Mean of Observation Range [km]',
                'Mean of Look angle [deg]',
                'Mean of Incidence angle [deg]',
                'SD of Observation Range [km]',
                'SD of Look angle [deg]',
                'SD of Incidence angle [deg]'
            ])
        with open(os.path.join(arch_dir, 'level_2_typical_observed_data_metrics.csv'), 'w') as outfile:
            writer = csv.writer(outfile)
            writer.writerow([
                'Mean of Mean of Observation Range [km]',
                'Mean of Mean of Look angle [deg]',
                'Mean of Mean of Incidence angle [deg]',
                'SD of Mean of Observation Range [km]',
                'SD of Mean of Look angle [deg]',
                'SD of Mean of Incidence angle [deg]',
                'Mean of SD of Observation Range [km]',
                'Mean of SD of Look angle [deg]',
                'Mean of SD of Incidence angle [deg]'
            ])

class readable_dir(argparse.Action):
    """Defines a custom argparse Action to identify a readable directory."""
    def __call__(self, parser, namespace, values, option_string=None):
        prospective_dir = values
        if not os.path.isdir(prospective_dir):
            raise argparse.ArgumentTypeError(
                '{0} is not a valid path'.format(prospective_dir)
            )
        if os.access(prospective_dir, os.R_OK):
            setattr(namespace,self.dest,prospective_dir)
        else:
            raise argparse.ArgumentTypeError(
                '{0} is not a readable dir'.format(prospective_dir)
            )

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Run instrument analysis (proxy)'
    )
    parser.add_argument(
        'infile',
        type = argparse.FileType('r'),
        help = "Tradespace search input JSON file"
    )
    parser.add_argument(
        'archdir',
        action = readable_dir,
        help = "Architecture directory to read inputs/write outputs"
    )
    args = parser.parse_args()
    execute(args.infile, args.archdir)
