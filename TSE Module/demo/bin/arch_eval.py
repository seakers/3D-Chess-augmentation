import argparse
import os, errno, sys
sys.path.append(os.getcwd())
import orbits_proxy
import cost_risk_proxy
import instrument_proxy
import launch_proxy
import value_proxy

"""
The architecture evalutator (arch_eval) coordinates the execution of analysis
modules. It takes two arguments as inputs:
    in_file     A JSON formatted document containing the TradespaceSearch.
    arch_dir    A readable directory containing a JSON formatted document
                containing the Architecture (arch.json) and the location where
                all analysis outputs shall be written.

The execution worksflow processes the following analysis modules using
proxy interfaces:
 1. Orbits (orbits_proxy)
 2. Instrument (instrument_proxy)
 3. Launch (launch_proxy)
 4. Cost and Risk (cost_risk_proxy)
 5. Value (value_proxy)
"""

def execute(in_file, arch_dir):
    """Executes the architecture evaluator."""
    orbits_proxy.execute(in_file, arch_dir)
    instrument_proxy.execute(in_file, arch_dir)
    launch_proxy.execute(in_file, arch_dir)
    cost_risk_proxy.execute(in_file, arch_dir)
    value_proxy.execute(in_file,arch_dir)

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
        description='Run architecture evaluator'
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
