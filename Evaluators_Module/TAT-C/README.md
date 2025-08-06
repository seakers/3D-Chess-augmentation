# TAT-C Evaluator Module

This module is part of the 3D Chess Augmentation Project, specifically designed to provide an interface between the Tradespace Analysis Tool for Constellations (TAT-C) and the Trade Space Explorer (TSE) and other tools. It serves as a critical component for evaluating satellite constellation designs and their performance metrics.

## Overview

The TAT-C Evaluator Module provides a set of interfaces and services that enable:
- Orbit propagation analysis
- Coverage analysis
- Revisit time calculations
- Access time calculations
- Integration with the TSE and other tools through both MQTT and HTTP protocols

## Features

- **Multiple Communication Protocols**:
  - MQTT interface for TSE interaction
  - HTTP REST API for requests from other tools
  - Support for both synchronous and asynchronous operations

- **Core Analysis Capabilities**:
  - Satellite orbit propagation using SGP4
  - Coverage analysis for constellation designs
  - Revisit time calculations
  - Access time analysis
  - Support for various sensor types and configurations

- **Integration Features**:
  - Seamless integration with TAT-C v3.2.1
  - Support for various coordinate reference systems
  - Flexible target point definitions
  - Comprehensive logging system

## Implemented Functions

The module implements the following TAT-C functions:

### Orbit Propagation
- SGP4-based orbit propagation
- Support for multiple coordinate frames (ECI, ECEF)
- Position and velocity calculations

### Coverage Analysis
- Coverage fraction calculations
- Harmonic mean revisit time analysis
- Support for custom target points and regions

### Access Time Analysis
- Access interval calculations
- Support for multiple payload configurations
- Integration with propagation records

### Metrics
- CoverageFraction
- HarmonicMeanRevisitTime

## Installation

1. Clone the repository
2. Install the required dependencies:
   ```bash
   pip install -r requirements.txt
   ```

## Usage

The module can be used in two ways:

### MQTT Interface (TSE Integration)
```python
# Example MQTT usage for TSE integration
from mqtt_manager import TATCEvaluator

evaluator = TATCEvaluator()
# The evaluator will automatically connect to the MQTT broker
# and start listening for analysis requests from TSE
```

### HTTP Interface (Other Tools)
```python
# Example HTTP usage for other tools
from http_manager import TATCEvaluator

evaluator = TATCEvaluator()
# The evaluator will start an HTTP server
# and handle analysis requests via REST API from other tools
```

## Dependencies

- TAT-C v3.2.1
- paho-mqtt
- flask
- numpy
- pandas
- geopandas
- scipy
- joblib
- pydantic
- astropy
- shapely
- skyfield
- sgp4
- geojson-pydantic

## Project Structure

- `mqtt_manager.py`: MQTT interface implementation for TSE communication
- `http_manager.py`: HTTP REST API implementation for other tools
- `tat_c_manager.py`: Core TAT-C integration logic
- `access_response.py`: Access time analysis implementation
- `tatc_propagation.py`: Orbit propagation implementation
- `eose/`: Extended Orbit Simulation Engine module

## Contributing

This project is part of the 3D Chess Augmentation Project. For contribution guidelines, please refer to the main project documentation.

## License

This project is licensed under the terms of the main 3D Chess Augmentation Project.
