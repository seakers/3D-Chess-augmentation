import tatc
import argparse
import os, errno
import csv
import time

import arch_eval
import value_proxy

"""
The tradespace search exeutive (tse) coordinates the end-to-end execution of a
tradespace search. Following a specified search strategy, it generates and
evaluates a series of architectures conforming to the tradespace search. It
takes two arguments as inputs:
    in_file     A JSON formatted document containing the TradespaceSearch.
    out_dir     A readable directory where all outputs shall be written.

Each architecture is stored in a unique directory labeled with a sequential
integer (arch-1/, arch-2/, etc.), containing the architecture specification
(arch.json) and any outputs generated by the architecture evaluator (arch_eval).
"""

def execute(in_file, out_dir=None):
    """Executes the example tradespace search executive."""
    if out_dir is None:
        out_dir = os.path.dirname(in_file.name)
    search = tatc.TradespaceSearch.from_json(in_file)
    with open(os.path.join(out_dir, 'summary.csv'), 'w') as out_file:
        writer = csv.writer(out_file)
        writer.writerow([
            'arch_id', 'constellation_type', 'num_satellites', 'num_planes', 'sat_ids', 'num_stations', 'gs_ids', 'exec_time [s]'
        ])
    for architecture in search.designSpace.generate_architectures(search.mission.start):
        start_time = time.time()
        arch_dir = os.path.join(out_dir, architecture._id)
        try:
            # try to create directory (checking in advance exposes race condition)
            os.makedirs(arch_dir)
        except OSError as e:
            # ignore error if directory already exists
            if e.errno != errno.EEXIST:
                raise
        with open(os.path.join(arch_dir, 'arch.json'), 'w') as out_file:
            architecture.to_json(out_file, indent=2)
        with open(os.path.join(arch_dir, 'arch.json'), 'r') as arch_file:
            arch_eval.execute(in_file, arch_dir)
        with open(os.path.join(out_dir, 'summary.csv'), 'a') as out_file:
            writer = csv.writer(out_file)
            existing_constellations = [c for c in search.designSpace.spaceSegment
                if c.constellationType == tatc.ConstellationType.EXISTING]
            generated_constellations = [c for c in architecture.spaceSegment]

            for constellation in existing_constellations + generated_constellations:
                writer.writerow([
                    architecture._id,
                    constellation.constellationType,
                    len(constellation.satellites),
                    constellation.numberPlanes,
                    ' '.join(satellite._id if satellite._id else 'sat-x{}'.format(i)
                            for i, satellite in enumerate(constellation.satellites)),
                    architecture.groundSegment[0].numberStations,
                    ' '.join(station._id if station._id else 'gs-x{}'.format(i)
                            for i, station in enumerate(architecture.groundSegment[0].groundStations)),
                    time.time() - start_time
                ])
    if hasattr(search.settings, 'proxyValue') and search.settings.proxyValue:
        pass
    else:
        # call the value module across all architectures
        value_proxy.execute(in_file, out_dir)

class readable_dir(argparse.Action):
    """Defines a custom argparse Action to identify a readable directory."""
    def __call__(self, parser, namespace, values, option_string=None):
        if values is None:
            setattr(namespace,self.dest,None)
        else:
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
        description='Run tradespace search executive'
    )
    parser.add_argument(
        'infile',
        type = argparse.FileType('r'),
        help = "Tradespace search input JSON file"
    )
    parser.add_argument(
        'outdir',
        nargs = '?',
        action = readable_dir,
        help = "Architecture output directory (defaults to directory containing infile)"
    )
    args = parser.parse_args()
    execute(args.infile, args.outdir)