# 3D-Chess-augmentation
## Overview
The 3D Chess Augmentation Project is a modular tool designed for space system design and satellite constellation evaluation. It provides engineers and scientists with a powerful platform to define, evaluate, and optimize satellite constellation architectures, leveraging Tradespace Exploration (TSE). The project integrates various evaluation tools such as VASSAR, TAT-C, and SpaDes, and is capable of multi-objective optimization across metrics like science performance, lifecycle cost, revisit time, and coverage.

The project is developed using a hybrid architecture:

Java: For the TSE engine and data handling.
Python: For evaluation modules such as SpaDes and TAT-C.
Features
Tradespace Exploration (TSE): A multi-objective optimization engine that explores and evaluates satellite constellation architectures based on mission objectives and constraints.
Modular Evaluator: Includes tools like VASSAR (for science performance), SpaDes (for lifecycle cost), and OREKIT (for orbital mechanics and propagation).
Customizable Workflows: Easily define workflows using JSON requests to call different evaluation tools.
Integration of Python and Java: Uses Python-based evaluation modules from within the Java-based TSE engine.