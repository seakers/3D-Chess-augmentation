import tatc
import argparse
import os
import json
import subprocess

"""
The cost and risk analysis proxy performs cost and risk analysis for a given
architecture. It takes two arguments as inputs:
    in_file     A JSON formatted document containing the TradespaceSearch.
    arch_dir    A readable directory containing a JSON formatted document
                containing the Architecture (arch.json) and the location where
                all analysis outputs shall be written.

Cost and risk analysis writes the following file to the architecture directory:
    CostRisk_output.json    JSON-formatted cost and risk analysis.
"""

def execute(in_file, arch_dir):
    """Executes the cost and risk analysis proxy."""
    in_file.seek(0) # reset reading from start of file
    search = tatc.TradespaceSearch.from_json(in_file)
    arch_path = os.path.join(arch_dir, 'arch.json')
    with open(arch_path, 'r') as arch_file:
        arch = tatc.Architecture.from_json(arch_file)
    if hasattr(search.settings, 'proxyCostRisk') and search.settings.proxyCostRisk:
        write_proxy_outputs(arch, arch_dir)
    else:
        # get path to *this* file
        dir_path = os.path.dirname(os.path.realpath(__file__))
        # get path to current working directory (i.e. where script was called)
        cwd_path = os.getcwd()
        # use subprocess.call with list to (more) safely build the shell command
        return_code = subprocess.call([
            os.path.join(dir_path, '..', '..', 'modules', 'costrisk', 'bin', 'TATc_CostRisk'),
            os.path.join(cwd_path, in_file.name),
            os.path.join(cwd_path, arch_dir)
        ])
        if return_code is 1:
            raise RuntimeError('Error executing cost and risk module')

def write_proxy_outputs(arch, arch_dir):
    """ Writes proxy outputs (placeholder files to establish interface format).

    Args
        arch (Architecture): the architecture to evaluate
        arch_dir (str): writable file directory to write outputs
    """

    with open(os.path.join(arch_dir, 'CostRisk_output.json'), 'w') as outfile:
        json.dump({
        	"groundCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
        	"hardwareCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
        	"iatCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
        	"launchCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
        	"lifecycleCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
            "nonRecurringCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
            "operationsCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
            "programCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
            "recurringCost" : { "estimate" : 0, "fiscalYear" : 0, "standardError" : 0 },
        	# "spacecraftRank" : [ { "fiscalYear" : 2019, "rank" : 0, "spacecraftIndex" : 0, "totalCost" : 0 } ],
        	"systemRisk" : [
                 { "category" : "Configuration Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of unforseen risk arising due to lack of flight heritage" },
                 { "category" : "Configuration Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of 'infant mortality' significantly reducing science return (due to extended on orbit checkouts, assuming a catastrophic failure does not occur)" },
                 { "category" : "Configuration Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk that constellation does not observe intended coverage area following loss of one or more spacecraft" },
                 { "category" : "Configuration Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of collision with orbital debris" },
                 { "category" : "Configuration Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of interrupted installation" },
                 { "category" : "Configuration Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of constellation formation change due to atmospheric drag" },
                 { "category" : "Configuration Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of improper spacing between adjacent spacecraft due to atmospheric drag" },
                 { "category" : "Satellite Performance Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of instrument design flaw" },
                 { "category" : "Satellite Performance Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of instrument deployment failure" },
                 { "category" : "Satellite Performance Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of premature instrument failure" },
                 { "category" : "Spacecraft Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of radiation damage to spacecraft instrumentation" },
                 { "category" : "Power Subsystem Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of excessive power subsystem degradation over lifetime" },
                 { "category" : "Attitude Determination and Control Subsystem Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of improper attitude alignment" },
                 { "category" : "Propulsion Subsystem Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of fuel related accident (e.g. propellant tank rupture, propulsion subsystem failure)" },
                 { "category" : "Thermal Subsystem Risks", "consequence" : 0, "likelihood" : 0, "risk" : "Risk of thermal subsystem failure" }
             ]
         }, outfile, indent=2)

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
        description='Run cost and risk analysis (proxy)'
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
