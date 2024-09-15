```
Project Structure for TAT-C:
tat-c/
.
├── README.md
│
├── demo  (Paul's architecture demo, ever evolving so please ensure you check for regular updates)
│   ├── bin
│   ├── README.md
│   ├── setup.py
│   ├── tatc
│   └── test
│
├── docs 
│   └── module-output-examples (each module output file examples and documentation, initially uploaded 3/8/19 and ever evolving)
│
├── tse  (From TAMU - what language will be used?  Java?? )
│   ├── (I don't see any directory from TAMU) 
│   ├── bin ( executalbe scripts using python?? including execDriver.py? )
│   ├── include (if using C++ header files>
│   ├── src (if using C++ or Java>
│   ├── lib (when Makefile creating that 'lib' directory>
│   ├── data (default user input files??>
│   └── test 
│
├── ToBeMigrated (Purpose of this folder is to hold the legacy sub-folders and
│   │ files from the old master branch until they are integrated or adapted to
│   │ the new architecture.)
│   ├── interfaces/gui (Still on the old architecture)
│   └── modules (currently has old TSI, CaR-cost and risk, and RMOC/orbits)
│
├── interfaces   (Methods of invoking TAT-C)
│   ├── README.md
│   ├── cli  (Python or unix shell script?)
│   │   ├── bin (?)
│   │   ├── cli
│   │   └── test
│   └── gui
│       ├── bin (? - for script to run GUI)
│       ├── gui  (NOTE:  Some of them need to be renamed and clean-up)
│       │    ├── Re__example_json 
│       │    ├── TAT-C_GUIicons
│       │    │   ├── detailedInputsAlt
│       │    │   ├── globeAlt
│       │    │   ├── nextStepAlt
│       │    │   ├── progressAlt
│       │    │   ├── resultsAlt
│       │    │   ├── runningAlt
│       │    │   ├── simpleBoundsAlt
│       │    │   ├── startRunAlt
│       │    │   └── storageAlt
│       │    └── TATC-Images
│       └── test 
│
├── knowledgebase   (Stevens module, Javascript and python in Docker container)
│   ├── LICENSE
│   ├── README.md
│   ├── api
│   │   ├── Dockerfile
│   │   ├── README.md
│   │   ├── app.js
│   │   ├── bin
│   │   │   └── www
│   │   ├── models
│   │   │   ├── ontology
│   │   │   │   ├── <JavaScript files> 
│   │   │   │   ├── attribute
│   │   │   │   │   └── <JavaScript file>
│   │   │   │   └── jsonld
│   │   │   │       └── <JavaScript files>
│   │   ├── <JavaScript files>
│   │   └── routes
│   │       ├── api
│   │       │   └── ontology
│   │       │       └── <JavaScript files>
│   │       └── <JavaScript file>
│   ├── fuseki
│   │   ├── Dockerfile 
│   │   └── tatc
│   ├── proxy
│   │   └── <Dockerfile and config files>
│   └── web
│       ├── bin
│       ├── hmtl
│       │   ├── <CSS, HTML, and SVG files>
│       │   ├── images
│       │   └── js
│       │       ├── <JavaScript files>
│       │       └── app
│       │           ├── <JavaScript files>
│       │           └── template
│       └── routes
│           ├── fake-api
│           │   └── <JavaScript and JSON files>
│           └── schema
│               ├── 1.0
│               │   └── <JSON files>
│               └── 1.1
│                   └── <JSON files>
│
└── modules
    ├── README.md  (???)
    ├── <Makefile or other configured script?>
    │
    ├── cost-and-risk   (C++)  (NOTE:  it wsa renambed by CaR)
    │   ├── README.md
    │   ├── bin         (create when using Makefile) 
    │   ├── docs
    │   ├── include
    │   ├── lib         (if needed, create when using Makefile)
    │   ├── Makefile 
    │   ├── src
    │   │   ├── Makefile
    │   │   └── <C++ files and its possible sub-directories> 
    │   └── test
    │       └── Makefile
    │
    ├── instrument 
    │   ├── README.md 
    │   ├── docs 
    │   ├── examples (current example_use_case)
    │   ├── instrupy 
    │   │   └── <Python files and its sub-directories>
    │   └── test 
    │
    ├── launch
    │   ├── README..md 
    │   ├── bin 
    │   │   └── <Possible python script or shell script to run the launch>
    │   ├── docs 
    │   ├── launch
    │   │   ├── __init__.py
    │   │   └── <Python files and its sub-directories>
    │   ├── output
    │   │   └── <untracked (?) or tracked JSON files for its output files(?)>
    │   ├── resources 
    │   │   └── <CSV files>
    │   └── test
    │       ├── <Python files for running unit tests>
    │       └── <JSON input files>
    │
    ├── maintenance      (Python)
    │   └── <TBD>
    │
    ├── orbits           (C++)    (NOTE: it was formerly named RMOC)
    │   ├── bin  (executale files from C++ and maybe Python script(s)?)
    │   ├── docs (?????)
    │   ├── Makefile (if needed for building recursively)
    │   ├── README.md
    │   ├── oc
    │   │   ├── Makefile (if needed for building recursively)
    │   │   ├── GMATsrc
    │   │   │   ├── base
    │   │   │   ├── console 
    │   │   │   ├── include 
    │   │   │   └── util
    │   │   │       ├── interpolator
    │   │   │       └── matrixoperations
    │   │   ├── docs
    │   │   │   ├── deliveries
    │   │   │   └── working
    │   │   ├── lib (if needed when usng Makefile for the library file?)
    │   │   ├── src
    │   │   │   └── <C++ files and its sub-directories> 
    │   │   └── tests
    │   │       ├── bin
    │   │       ├── Makefile
    │   │       ├── <other Makefiles ?> 
    │   │       └── output  
    │   ├── rm
    │   │   ├── bin
    │   │   ├── include (C++ header files)
    │   │   ├── lib (if needed when usng Makefile)
    │   │   ├── Makefile
    │   │   ├── src
    │   │   │   ├── Makefile
    │   │   │   └── <C++ files and its sub-directories> 
    │   │   └── test 
    │   └── test
    │
    ├── third-party-tools  (Any suggestions for the better location/name?)
    │   ├── boost   (NOTE:  oc uses that boost library)
    │   │  └── <its files and sub-directories)
    │   └── json	(NOTE:  rm and cost-and-risk use that json)
    │       └── <its files and sub-directories)
    │
    └── value       (Python?)
        ├── README.md 
        ├── docs  (??)
        ├── valpy (??? - any suggestions for its name?) 
        │   └── <Python files and its sub-directories>
        └── test  (current example_use_case)
```
