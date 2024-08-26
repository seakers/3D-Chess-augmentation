#!C:\Users\dfornos\AppData\Local\Microsoft\WindowsApps\PythonSoftwareFoundation.Python.3.11_qbz5n2kfra8p0\python.exe


import os
import errno
import sys
import subprocess
import argparse


if __name__ == "__main__":
    parser = argparse.ArgumentParser(
        description='Run architecture evaluator'
    )
    parser.add_argument(
        'infile',
        help="Tradespace search input JSON file"
    )
    parser.add_argument(
        'outdir',
        help="Architecture directory to read inputs/write outputs"
    )
    args = parser.parse_args()

    temp_cwd = os.getcwd()

    tse_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    tatc_dir = os.path.dirname(tse_dir)

    jarPath = os.path.join(tatc_dir, 'tse', 'target', 'tatc-ml-tse-1.0.jar')
    inputPath = os.path.abspath(args.infile)
    outputPath = os.path.abspath(args.outdir)

    os.chdir(tatc_dir)
    subprocess.call(['java', '-jar', jarPath, inputPath, outputPath])

    # Change the previous current working directory back
    os.chdir(temp_cwd)
