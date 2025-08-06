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

- **Java 8** or higher
- **Maven** 3.6+
- **Python** 3.7+
- **Git**

### 1. TSE Module Setup

The TSE Module is the core orchestrator of the system.

#### Installation

```bash
cd TSE_Module/tse

# Install dependencies and build
make all
```

This will:
- Install required JAR dependencies (mopAOS and conMOP)
- Install Python dependencies
- Build the Java application with Maven

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

#### Available Test Cases

The module includes several pre-configured test cases:
- `landsat8.json` - Landsat 8 satellite mission
- `CaseStudy1.json` - TROPICS mission (microsatellites)
- `CaseStudy2.json` - Different constellation configuration
- `CaseStudy3.json` - Advanced mission parameters
- `CaseStudy4.json` - Complex multi-objective optimization
- `CaseStudy5.json` - Large-scale constellation design

### 2. TATC (Tradespace Analysis Toolkit for Constellations)

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

### 3. VASSAR Setup

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

### 4. instrupy and orbitpy Setup

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