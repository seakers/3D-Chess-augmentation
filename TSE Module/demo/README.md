TAT-C Architecture Demo
=======================

This project contains an architectural interface specification for the TAT-C project. It is organized in several pieces:
 1. TAT-C interface library (`tatc/`)
 2. Unit tests (`test/`)
 3. Python executables (`bin/`)
 4. Example outputs for Landsat-8 application case (`landsat8/`)

The interface library contains Python classes to read/write to JSON files and perform basic validation including assigning default values. Unit tests provide low-level coverage of large portions (but not complete) functionality.

The executables include proxy scripts to simulate module-specific behavior and generate output files representative of module outputs. The example outputs have been generated using these scripts.

## Installation

This project was designed for Python 3.7 but should also be compatible with Python 2.7. It requires the following non-standard (to Anaconda) packages (installed automatically if following instructions below):
 - `isodate`
 - `numpy`
 - `enum34` (for Python 2.X)

To make the `tatc` library visible to the Python interpreter and automatically configure dependencies, from the project root directory (containing `setup.py`), run:
```shell
pip install -e .
```
where the trailing period (`.`) indicates to install from the current directory. You may have to re-run this command if there are significant changes to the source code (i.e. a new version is available).

## Testing

This project includes unit tests with substantial (though not complete) coverage of the interface specification. To run using `nosetests` module, run from the command line:

```shell
python -m nose
```

## Usage

Executable scripts are provided in the `bin/` directory.

### Generate Landsat 8

Generates a tradespace search file based on the Landsat 8 validation case:
```shell
python bin/gen_landsat8.py [outfile] [-x]
```
where `outfile` defaults to `landsat8.json`. The optional flag `-x` automatically runs the tradespace search executive using the generated tradespace search file.

Example usage:
```shell
mkdir example
python bin/gen_landsat8.py example/landsat8.json
```
Outputs:
```
|-- bin/
|-- example/
    |-- landsat8.json
|-- tatc/
```

### Tradespace Search Executive (TSE)

Executes a full-factorial tradespace search including enumeration (generating architectures) and evaluation (generating outputs for each architecture):
```shell
python bin/dmo_tse.py infile [outdir]
```
where `infile` specifies the tradespace search input JSON file and `outdir` specifies the output directory to write architectures (defaults to the parent directory of `infile`).

Example usage:
```shell
python bin/demo_tse.py example/landsat8.json
```
Outputs:
```
|-- bin/
|-- example/
    |-- landsat8.json
    |--...(top-level outputs)...
    |-- arch-0/
        |-- arch.json
        |-- ...(architecture-specific outputs)...
    |-- arch-1/
        |-- arch.json
        |-- ...(architecture-specific outputs)...
    |-- arch-2/
        |-- arch.json
        |-- ...(architecture-specific outputs)...
|-- tatc/
```

### Architecture Evaluator

Evaluates an architecture by orchestrating all analysis modules:
```shell
python bin/arch_eval.py infile archdir
```
where `infile` specifies the tradespace search input JSON file, `archdir` specifies the architecture directory to read the architecture input JSON file (`arch.json`) and write analysis outputs.

Example usage:
```shell
python bin/arch_eval.py example/landsat8.json example/arch-1
```
Outputs:
```
|-- bin/
|-- example/
    |-- landsat8.json
    |-- arch-0/
    |-- arch-1/
        |-- arch.json
        |-- ...(outputs)...
    |-- arch-2/
|-- tatc/
```

### Analysis Proxy

Executes a proxy version of an analysis module (orbits, launch, instrument, or cost and risk):
```shell
python bin/orbits_proxy.py infile archdir
python bin/launch_proxy.py infile archdir
python bin/instrument_proxy.py infile archdir
python bin/cost_risk_proxy.py infile archdir
```
where `infile` specifies the tradespace search input JSON file, `archdir` specifies the architecture directory to read the architecture input JSON file (`arch.json`) and any other dependent files and write analysis outputs.

Example usage:
```shell
python bin/orbits_proxy.py example/landsat8.json example/arch-1
```
Outputs:
```
|-- bin/
|-- example/
    |-- landsat8.json
    |-- arch-0/
    |-- arch-1/
        |-- arch.json
        |-- ...(outputs)...
    |-- arch-2/
|-- tatc/
```

### Tradespace Search Validator (TSV)

Performs routine validation of a tradespace search document by reading JSON into Python, assigning any default values and removing unknown keys, and writing JSON back to file:
```shell
python bin/tsv.py infile outfile
```
where `infile` specifies the tradespace search input JSON file and `outdir` specifies the output directory to write architectures (defaults to `.`).

Example usage:
```shell
echo {} > example/blank.json
python bin/tsv.py example/blank.json example/default.json
```
Outputs:
```
|-- bin/
|-- example/
    |-- blank.json
    |-- default.json
|-- tatc/
```
