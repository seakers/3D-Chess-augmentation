import tatc
import argparse
import os
import csv
import json
import subprocess

"""
The orbits analysis proxy performs orbital analysis for a given architecture.
It takes two arguments as inputs:
    in_file     A JSON formatted document containing the TradespaceSearch.
    arch_dir    A readable directory containing a JSON formatted document
                containing the Architecture (arch.json) and the location where
                all analysis outputs shall be written.

Orbital analysis writes the following files to the architecture directory:
    access.csv  CSV-formatted list of all access periods for constellation
                member satellites.
    gbl.json    JSON-formatted document of global performance measures for the
                constellation.
    lcl.csv     CSV-formatted list of all performance measures local to a point
                of interest for the constellation.
    obs-#.csv   CSV-formatted list of time-steamped satellite Keplerian state
                variables (1 file per satellite, sequential integer ids).
    satellite_states-#.csv  CSV-formatted list of time-stamped satellite
                Cartesian state variables (1 file per satellite, sequential
                integer ids).
"""

def execute(in_file, arch_dir):
    """Executes the orbital analysis proxy."""
    in_file.seek(0) # reset reading from start of file
    search = tatc.TradespaceSearch.from_json(in_file)
    arch_path = os.path.join(arch_dir, 'arch.json')
    print(arch_path)
    with open(arch_path, 'r') as arch_file:
        arch = tatc.Architecture.from_json(arch_file)
    if hasattr(search.settings, 'proxyOrbits') and search.settings.proxyOrbits:
        keepLowLevelData = False
        if hasattr(search.settings, 'outputs') and hasattr(search.settings.outputs, 'keepLowLevelData'):
            keepLowLevelData = search.settings.outputs.keepLowLevelData
        write_proxy_outputs(arch, arch_dir, keepLowLevelData)
    else:
        # get path to *this* file
        dir_path = os.path.dirname(os.path.realpath(__file__))
        # get path to current working directory (i.e. where script was called)
        cwd_path = os.getcwd()
        # use subprocess.call with list to (more) safely build the shell command
        return_code = subprocess.call([
            os.path.join(dir_path, '..', '..', 'modules', 'orbits', 'rm', 'bin', 'reductionMetrics'),
            os.path.join(cwd_path, in_file.name),
            os.path.join(cwd_path, arch_dir)
        ])
        if return_code is 1:
            raise RuntimeError('Error executing orbits module')

def write_proxy_outputs(arch, arch_dir, keepLowLevelData=False):
    """ Writes proxy outputs (placeholder files to establish interface format).

    Args
        arch (Architecture): the architecture to evaluate
        arch_dir (str): writable file directory to write outputs
        keepLowLevelData (bool): preserve low-level data outputs
    """

    with open(os.path.join(arch_dir, 'gbl.json'), 'w') as outfile:
        json.dump({
            "Time" : {"min" : 0, "max" : 0},
            "TimeToCoverage" : {"min": 0, "max": 0, "avg": 0},
            "AccessTime" : {"min": 0, "max": 0, "avg": 0},
            "RevisitTime" : {"min": 0, "max": 0, "avg": 0},
            "ResponseTime" : {"min": 0, "max": 0, "avg": 0},
            "Coverage" : 0,
            "NumOfPOIpasses" : {"min": 0, "max": 0, "avg": 0},
            "DataLatency" : {"min": 0, "max": 0, "avg": 0},
            "NumGSpassesPD" : 0,
            "TotalDownlinkTimePD" : 0,
            "DownlinkTimePerPass" : {"min": 0, "max": 0, "avg": 0}
        }, outfile, indent=2)
    with open(os.path.join(arch_dir, 'lcl.csv'), 'w') as outfile:
        writer = csv.writer(outfile)
        writer.writerow([
            "Time [s]", "", "POI", "[deg]", "[deg]", "[km]",
            "AccessTime [s]", "", "", "RevisitTime [s]", "", "", "ResponseTime [s]", "", "",
            "TimeToCoverage [s]", "Number of Passes"
        ])
        writer.writerow([
            "t0", "t1", "POI", "lat", "lon", "alt", "ATavg", "ATmin", "ATmax",
            "RvTavg", "RvTmin", "RvTmax", "RpTavg", "RpTmin", "RpTmax", "TCcov", "numPass"
        ])
    with open(os.path.join(arch_dir, 'poi.csv'), 'w') as outfile:
        writer = csv.writer(outfile)
        writer.writerow([
            "POI", "lat[deg]", "lon[deg]"
        ])
        writer.writerow([
            0, 0, 0
        ])
    if keepLowLevelData:
        for satellite in arch.spaceSegment[0].satellites:
            with open(os.path.join(arch_dir, '{:}_accessInfo.csv'.format(satellite._id)), 'w') as outfile:
                writer = csv.writer(outfile)
                writer.writerow([
                    'EventNum', 'POI', 'AccessFrom[Days]', 'Duration[s]', 'Time[Days]',
                    'X[km]', 'Y[km]', 'Z[km]', 'VX[km/s]', 'VY[km/s]', 'VZ[km/s]'
                ])
            with open(os.path.join(arch_dir, 'obs_{:s}.csv'.format(satellite._id)), 'w') as outfile:
                writer = csv.writer(outfile)
                writer.writerow([
                    "Time[s]", "Ecc[deg]", "Inc[deg]", "SMA[km]", "AOP[deg]",
                    "RAAN[deg]", "MA[deg]", "Lat[deg]", "Lon[deg]", "Alt[km]"
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
        description='Run orbital analysis (proxy)'
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
