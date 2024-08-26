from tatc import Architecture
from tatc import TradespaceSearch
import argparse
import os
import json

"""
The tradespace search validator (tsv) performs basic validation for a
tradespace search JSON document by removing unused keys and assigning default
values for missing keys. It takes two arguments as inputs:
    in_file     A JSON formatted document containing the TradespaceSearch.
    out_file    The new file name for the validated JSON formatted document.
"""

def execute(in_file, out_file):
    """Executes the tradespace search validator."""
    search = TradespaceSearch.from_json(in_file)
    search.to_json(out_file, indent=2)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Run tradespace search validator'
    )
    parser.add_argument(
        'infile',
        type = argparse.FileType('r'),
        help = "Tradespace search input JSON file"
    )
    parser.add_argument(
        'outfile',
        type = argparse.FileType('w'),
        help = "Validated tradespace search JSON file"
    )
    args = parser.parse_args()
    execute(args.infile, args.outfile)
