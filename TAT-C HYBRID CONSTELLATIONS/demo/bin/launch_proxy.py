import tatc
import argparse
import os
import csv
import json
import subprocess

"""
The launch analysis proxy performs launch vehicle analysis for a given
architecture. It takes two arguments as inputs:
    in_file     A JSON formatted document containing the TradespaceSearch.
    arch_dir    A readable directory containing a JSON formatted document
                containing the Architecture (arch.json) and the location where
                all analysis outputs shall be written.

Launch vehicle analysis writes the following file to the architecture directory:
    launch.json     JSON-formatted placeholder document.
"""

def execute(in_file, arch_dir):
    """Executes the launch analysis proxy."""
    in_file.seek(0) # reset reading from start of file
    search = tatc.TradespaceSearch.from_json(in_file)
    arch_path = os.path.join(arch_dir, 'arch.json')
    with open(arch_path, 'r') as arch_file:
        arch = tatc.Architecture.from_json(arch_file)
    if hasattr(search.settings, 'proxyLaunch') and search.settings.proxyLaunch:
        write_proxy_outputs(arch, arch_dir)
    else:
        ### option 1: subprocess call to instrument module
        # get path to *this* file
        dir_path = os.path.dirname(os.path.realpath(__file__))
        # get path to current working directory (i.e. where script was called)
        cwd_path = os.getcwd()
        # use subprocess.call with list to (more) safely build the shell command
        return_code = subprocess.call([
            'python',
            os.path.join(dir_path, '..', '..', 'modules', 'launch', 'bin', 'run_launch.py'),
            os.path.join(cwd_path, in_file.name),
            os.path.join(cwd_path, arch_dir)
        ])
        if return_code is 1:
            raise RuntimeError('Error executing launch module')

        ### option 2: direct call to python code in launch module (note: does not work because bin/ is not set up as an importable package)
        #from ...modules.launch.bin import run_launch
        #instrument_module.execute(arch_path, arch_dir)

def write_proxy_outputs(arch, arch_dir):
    """ Writes proxy outputs (placeholder files to establish interface format).

    Args
        arch (Architecture): the architecture to evaluate
        arch_dir (str): writable file directory to write outputs
    """
    with open(os.path.join(arch_dir, 'launch_manifest.json'), 'w') as outfile:
        # assume only one constellation
        constellation = arch.spaceSegment[0] if isinstance(arch.spaceSegment, list) else arch.spaceSegment
        launch_dict = {}
        planes = range(constellation.numberPlanes) if constellation.numberPlanes else [0]
        for plane in planes:
            launch_dict['Plane {:}'.format(plane+1)] = {
                 "Orbit": {
                    "inclination": 0,
                    "semimajorAxis": 0,
                    "eccentricity": 0,
                    "periapsisArgument": 0,
                    "rightAscensionAscendingNode": 0,
                    "trueAnomaly": 0,
                    "polar": False
                 },
                 "Launch 1": {
                    "Rocket Name": "",
                    "Satellites on Rocket": [ ],
                    "Individual Rocket Cost": 0
                 },
                 "Plane {:} Cost".format(plane+1): 0
            }

        json.dump(launch_dict, outfile, indent=2)

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
        description='Run launch analysis (proxy)'
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
