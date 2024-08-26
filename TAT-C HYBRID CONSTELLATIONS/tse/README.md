TSE for TAT-C ML
=======================

This project contains the algorithm to enumerate architectures using either full factorial or intelligent search.


## Installation

To install necessary dependencies for the project, follow the steps below.

```shell
mvn install:install-file -Dfile=./lib/mopAOS-1.0.jar -DgroupId=seakers -DartifactId=mopAOS -Dversion=1.0 -Dpackaging=jar

mvn package
```
where the trailing period (`.`) indicates to install from the current directory.


## Usage

Executable script is provided in the `bin/` directory.

### Run the TSE

Executes a full-factorial tradespace search including enumeration (generating architectures) and evaluation (generating outputs for each architecture):
```shell
python tse/bin/tse.py infile [outdir]
```
where `infile` specifies the tradespace search input JSON file and `outdir` specifies the output directory to write architectures (defaults to the parent directory of `infile`).

Example usage:
```shell
python tse/bin/tse.py tse/problems/landsat8.json tse/problems
```
Outputs:
```
|-- problems/
    |-- landsat8.json
    |-- arch-0/
        |-- arch.json
        |-- ...(outputs)...
    |-- arch-1/
        |-- arch.json
        |-- ...(outputs)...
    |-- arch-2/
        |-- arch.json
        |-- ...(outputs)...
```