import tatc
import argparse
import os
import json
import subprocess

"""
The value analysis proxy performs value analysis for all architectures
evaluated in a tradespace analysis. It takes two arguments as inputs:
    in_file     A JSON formatted document containing the TradespaceSearch.
    ts_dir      A readable tradespace directory containing the set of all
                architecture directories (i.e. arch-0/, arch-1/, ...) and where
                all analysis outputs shall be written.

Value analysis writes the following file to the tradespace directory:
    value_output.json    JSON-formatted value analysis.
"""

def execute(in_file, arch_dir):
    """Executes the value analysis proxy."""
    in_file.seek(0) # reset reading from start of file
    search = tatc.TradespaceSearch.from_json(in_file)
    if hasattr(search.settings, 'proxyValue') and search.settings.proxyValue:
        write_proxy_outputs(arch_dir)
    else:
        #TODO add call to value module
        ### option 1: subprocess call to value module
        # get path to *this* file
        dir_path = os.path.dirname(os.path.realpath(__file__))
        # get path to current working directory (i.e. where script was called)
        cwd_path = os.getcwd()

        # use subprocess.call with list to (more) safely build the shell command
        return_code = subprocess.call([
            'python',
            os.path.join(dir_path, '..', '..', 'modules', 'value', 'bin', 'run_value.py'),
            os.path.join(cwd_path, in_file.name),
            os.path.join(cwd_path, arch_dir)
        ])
        if return_code is 1:
            raise RuntimeError('Error executing value module')

        # raise NotImplementedError('Value proxy interface not defined')

def write_proxy_outputs(ts_dir):
    """ Writes proxy outputs (placeholder files to establish interface format).

    Args
        arch_dirs (list): the list of architecture directories
        ts_dir (str): writable file directory to write outputs
    """
    with open(os.path.join(ts_dir, 'value_output.json'), 'w') as outfile:
        arch_dict = {}
        # iterate over the list of sub-directores (index 1 in walk)
        # inside the tradespace directory
        for arch_dir in next(os.walk(ts_dir, topdown=True))[1]:
            arch_dict[arch_dir] = {
                'Total Architecture Value [Mbits]': 0,
                'Total Data Collected [Mbits]': 0
            }
        json.dump(arch_dict, outfile, indent=2)

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
        description='Run value analysis (proxy)'
    )
    parser.add_argument(
        'infile',
        type = argparse.FileType('r'),
        help = "Tradespace search input JSON file"
    )
    parser.add_argument(
        'tsdir',
        nargs = '?',
        action = readable_dir,
        help = "Tradespace directory to read inputs/write outputs"
    )
    args = parser.parse_args()
    execute(args.infile, args.tsdir)
