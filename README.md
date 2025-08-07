# 3D-Chess Augmentation: Tradespace Search Executive (TSE) Module

## Overview

The 3D-Chess Augmentation project is a comprehensive satellite constellation design and evaluation framework that integrates multiple specialized tools for tradespace exploration and architecture optimization. This project enables automated enumeration, evaluation, and optimization of satellite constellation architectures using advanced multi-objective optimization techniques.

### Key Components

- **TSE Module**: Core tradespace search executive that orchestrates the entire evaluation process
- **TATC (Tradespace Analysis Toolkit for Constellations)**: Primary evaluation engine for constellation performance analysis
- **VASSAR**: Science and cost evaluation framework for satellite missions
- **instrupy**: Instrument modeling and analysis toolkit
- **orbitpy**: Orbital mechanics and propagation analysis

### Architecture

The system follows a modular architecture where:
1. **TSE Module** receives mission requirements and design space definitions
2. **TATC** performs constellation-level analysis and optimization
3. **VASSAR** evaluates science value and cost metrics
4. **instrupy** and **orbitpy** provide specialized instrument and orbital analysis
5. Results are aggregated and presented through REST APIs and MQTT messaging

## Installation and Setup

### Prerequisites

- **Java 8** or higher (JDK, not just JRE)
- **Maven 3.6+**
- **Python 3.7+** with pip
- **Git**

### 1. TSE Module Setup

The TSE Module is the core orchestrator of the system, built as a Spring Boot application with Maven.

#### Installation

```bash
cd TSE_Module/tse

# Install dependencies and build
make all
```

This command will:
1. **Install JAR Dependencies**: Install required JAR files (mopAOS-1.0.jar and conMOP-1.0.jar) into your local Maven repository
2. **Install Python Package**: Install the TSE Python package in development mode
3. **Build Java Application**: Compile and package the Spring Boot application using Maven

#### Manual Installation Steps (if make fails)

If the `make all` command fails, you can perform the installation manually:

```bash
cd TSE_Module/tse

# 1. Install JAR dependencies to local Maven repository
mvn install:install-file -Dfile=./lib/mopAOS-1.0.jar -DgroupId=seakers -DartifactId=mopAOS -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=./lib/conMOP-1.0.jar -DgroupId=seakers -DartifactId=conMOP -Dversion=1.0 -Dpackaging=jar

# 2. Install Python package
pip install -e .

# 3. Build the Java application
mvn package
```

#### Verification

After installation, verify the build was successful:

```bash
# Check if the JAR file was created
ls -la target/tatc-ml-tse-1.0.jar

# Check if Python package is installed
python -c "import tatc_tse; print('TSE Python package installed successfully')"
```

#### Testing the TSE Module

**Method 1: REST API (Recommended)**

```bash
# Start the application
mvn spring-boot:run

# Test with a sample request
curl -X POST http://localhost:7500/tse \
  -H "Content-Type: application/json" \
  -d @problems/landsat8.json
```

**Method 2: Python Script**

```bash
# Run using the Python script
python bin/tse.py problems/landsat8.json problems
```

**Method 3: Direct Java Execution**

```bash
# Run the JAR directly
java -jar target/tatc-ml-tse-1.0.jar problems/landsat8.json problems
```

#### Available TSERequests

The system includes several pre-configured TSERequest JSON files for testing different scenarios:

**Core Decision-Making Requests:**
- `TSERequestAssigning.json` - **Assigning Decision Type**: Assigns payloads to orbits for TROPICS mission with 7-day duration, optimizing ScienceScore (MAX) and LifecycleCost (MIN)
- `TSERequestAssigningSmall.json` - **Simplified Assigning**: Reduced version with 1-day duration and fewer orbit options for quick testing
- `assigning.json` - **Alternative Assigning**: Similar to Assigning but with RevisitAnalysis tool constraint

**Complex Decision Requests:**
- `combining.json` - **Combining Decision Type**: Combines multiple design variables (satellites, planes, altitude, inclination, etc.) for comprehensive constellation optimization with 3 objectives (InstrumentScore, LifecycleCost, HarmonicMeanRevisitTime)
- `TSERequestClimateCentricDSPAC_test.json` - **Multi-Stage Decision Process**: Complex workflow with DownSelecting, Partitioning, Combining, and Construction decisions for climate-centric analysis

**Specialized Test Requests:**
- `TSERequestFFFireSat2_test.json` - **FireSat Mission**: Fire detection mission with OrbitPy tool constraints, optimizing InstrumentScore, LifecycleCost, and HarmonicMeanRevisitTime

**Note**: The `modified_tseRequest.json` file is reserved for development and should not be used for testing.

### 2. Python Tools Setup

The Python folder contains various analysis and visualization tools for the 3D-Chess Augmentation project.

#### Installation

```bash
cd Python

# Install Python dependencies
pip install -r requirements.txt
```

#### Using the Python Tools

Once the TSE service is running with `mvn spring-boot:run` and all other tools are running on their respective ports, you can use the Python tools:

**Testing with dummyRequester.py**

```bash
cd Python

# Run the dummy requester to send test requests to TSE
python dummyRequester.py
```

This script will:
1. Start a Flask server on port 7000 to receive callbacks
2. Send a TSERequest to the TSE server running on port 7500
3. Display received solutions in real-time
4. Continue running until interrupted (Ctrl+C)

**Available Python Tools**

- `dummyRequester.py` - Test client for sending requests to TSE
- `workflow_generator.py` - Generate evaluation workflows
- `plot_generator.py` - Generate visualization plots from results
- `pareto_generator.py` - Generate Pareto front analysis
- `hypervolume_calculator.py` - Calculate hypervolume metrics
- `hv_evolution_generator.py` - Track hypervolume evolution
- `graph_generator.py` - Generate workflow dependency graphs
- `reconstruct_from_logs.py` - Reconstruct results from log files
- `genetic_operators.py` - Genetic algorithm operators

### 3. TATC (Tradespace Analysis Toolkit for Constellations)

TATC is the primary evaluation engine for constellation performance analysis.

#### Installation

```bash
# Clone the TATC repository
git clone https://github.com/seakers/TATC-Integration.git
cd TATC-Integration

# Follow the instructions in the TATC repository README
# This typically involves:
# - Installing Python dependencies
# - Setting up configuration files
# - Running initial tests
```

**Note**: Refer to the [TATC-Integration repository](https://github.com/seakers/TATC-Integration.git) for detailed setup instructions specific to that component.

### 4. SpaDes (Spacecraft Design Tool)

SpaDes is a comprehensive spacecraft design tool that takes payloads and orbit specifications as input and produces spacecraft designs with cost estimates.

#### Installation

```bash
# Clone the SpaDes repository (use IntegrationDev branch for latest features)
git clone https://github.com/seakers/SpaDes.git
cd SpaDes
git checkout IntegrationDev

# Install Python dependencies
pip install -r requirements.txt

# Note: SpaDes requires Python 3.9 or 3.10 for compatibility with tat-c
```

#### Testing SpaDes

**Method 1: Basic Spacecraft Design**

```bash
# Run the main spacecraft design script
python SpacecraftDesignMain.py

# This will:
# - Pick a random payload and orbit
# - Generate a spacecraft design
# - Output design specifications and cost estimates
```

**Method 2: Constellation Design**

```bash
# Run constellation design analysis
python ConstellationDesignMain.py

# This provides constellation-level analysis and optimization
```

**Method 3: Cost Estimation Testing**

```bash
# Test cost estimation functionality
python testCostEstimation.py

# This validates the cost estimation algorithms
```

#### SpaDes Features

- **Spacecraft Design**: Automated generation of spacecraft configurations
- **Cost Estimation**: Comprehensive cost analysis and budgeting
- **Constellation Analysis**: Multi-satellite constellation optimization
- **Performance Scoring**: Science and performance evaluation metrics
- **MQTT Integration**: Real-time communication capabilities
- **Configuration Optimization**: Automated component selection and sizing

#### Available Modules

- `SpacecraftDesignMain.py` - Main spacecraft design script
- `ConstellationDesignMain.py` - Constellation design and analysis
- `CostEstimation.py` - Cost analysis and budgeting
- `PerformanceScore.py` - Performance evaluation and scoring
- `ConfigurationOptimization.py` - Automated configuration optimization
- `ADCSDesign.py` - Attitude determination and control system design
- `CommsDesign.py` - Communications system design
- `EPSDesign.py` - Electrical power system design
- `PropulsionDesign.py` - Propulsion system design

### 5. VASSAR Setup

VASSAR provides science and cost evaluation capabilities for satellite missions.

#### Installation

```bash
# Clone VASSAR repositories (use Alex_Dev_TATC branches)
git clone -b Alex_Dev_TATC https://github.com/seakers/VASSAR_exec.git
git clone -b Alex_Dev_TATC https://github.com/seakers/VASSAR_lib.git

# Clone VASSAR resources (use main branch)
git clone https://github.com/seakers/VASSAR_resources.git

# Set up VASSAR environment
cd VASSAR_exec
# Follow VASSAR-specific setup instructions
```

**Repository References:**
- [VASSAR_exec](https://github.com/seakers/VASSAR_exec) - Execution scripts
- [VASSAR_lib](https://github.com/seakers/VASSAR_lib.git) - Core VASSAR functions
- [VASSAR_resources](https://github.com/seakers/VASSAR_resources.git) - Common resources

### 6. instrupy and orbitpy Setup

These tools provide instrument modeling and orbital mechanics analysis.

#### Installation

```bash
# Navigate to the instrupy branch in this repository
git checkout instrupy

# Install instrupy dependencies
pip install -r requirements.txt

# Install orbitpy dependencies
pip install orbitpy
```

## Usage Examples

### Basic TSE Execution

```bash
# Start the TSE application
cd TSE_Module/tse
mvn spring-boot:run

# Send a tradespace search request
curl -X POST http://localhost:7500/tse \
  -H "Content-Type: application/json" \
  -d '{
    "workflowId": "test-workflow-001",
    "mission": {
      "name": "Test Mission",
      "duration": "P0Y0M90D"
    },
    "designSpace": {
      "spaceSegment": [{
        "constellationType": "DELTA_HOMOGENEOUS",
        "numberSatellites": [1, 2],
        "numberPlanes": [1, 2]
      }]
    }
  }'
```

### Testing with Python Tools

Once all services are running:

```bash
# Navigate to Python tools directory
cd Python

# Install dependencies
pip install -r requirements.txt

# Run the dummy requester to test the system
python dummyRequester.py
```

This will:
1. Start a callback server on port 7000
2. Send a test request to TSE on port 7500
3. Display received solutions in real-time
4. Continue running until interrupted

### Advanced Configuration

The system supports complex mission configurations including:
- Multi-objective optimization
- Advanced constellation patterns
- Instrument-specific analysis
- Cost-risk evaluation

## API Documentation

### TSE REST API

**Endpoint**: `POST /tse`

**Request Format**:
```json
{
  "workflowId": "unique-workflow-id",
  "callbackUrl": "http://localhost:8080/callback",
  "mission": {
    "name": "Mission Name",
    "duration": "P0Y0M90D",
    "target": {
      "latitude": {"minValue": -90, "maxValue": 90},
      "longitude": {"minValue": -180, "maxValue": 180}
    }
  },
  "designSpace": {
    "spaceSegment": [...],
    "satellites": [...]
  }
}
```

**Response Format**:
```json
{
  "workflowId": "unique-workflow-id",
  "status": "SUCCESS",
  "outputDirectory": "/path/to/results",
  "message": "TSE execution completed successfully"
}
```

## Troubleshooting

### Common Issues

1. **Compilation Errors**: Ensure all dependencies are installed with `make all`
2. **Port Conflicts**: The TSE application runs on port 7500 by default
3. **Missing Dependencies**: Check that all required JAR files are in the `lib/` directory
4. **Python Path Issues**: Ensure Python dependencies are installed correctly

### Debug Mode

Enable debug logging by modifying `application.properties`:
```properties
logging.level.tatc=DEBUG
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For issues and questions:
- Check the troubleshooting section above
- Review the individual component repositories
- Create an issue in the appropriate repository