# TSE (Tradespace Search Executive) Module

## Overview

The TSE Module is the core orchestrator of the 3D-Chess Augmentation system, providing a Spring Boot-based REST API for tradespace exploration and satellite constellation optimization. It integrates multiple evaluation tools and manages the entire workflow from request processing to result delivery.

## Architecture

### Core Components

The TSE Module is built as a Spring Boot application with the following key components:

#### 1. **TSEApplication.java** - Main Application Entry Point
- Spring Boot application launcher
- Auto-configuration and component scanning
- Runs on port 7500 by default

#### 2. **DaphneRequestController.java** - REST API Controller
- Handles incoming HTTP requests at `/tse` endpoint
- Processes JSON requests for tradespace searches
- Manages workflow execution and response handling

#### 3. **TSE.java** - Core TSE Logic
- Main tradespace search executive implementation
- Orchestrates the evaluation workflow
- Manages communication between different tools

#### 4. **TSEWorkflowGenerator.java** - Workflow Management
- Generates evaluation workflows based on user requests
- Manages tool dependencies and execution order
- Handles workflow optimization and validation

#### 5. **TSEPublisher.java & TSESubscriber.java** - MQTT Communication
- Publisher: Sends requests to evaluation tools
- Subscriber: Receives results from evaluation tools
- Real-time communication for distributed evaluation

#### 6. **PythonServerManager.java** - Python Integration
- Manages Python tool execution
- Handles communication with Python-based evaluation tools
- Coordinates Python script execution and result collection

### Package Structure

```
src/main/java/tatc/
├── TSEApplication.java              # Spring Boot main class
├── DaphneRequestController.java     # REST API controller
├── TSE.java                        # Core TSE implementation
├── TSEWorkflowGenerator.java       # Workflow generation
├── TSEPublisher.java               # MQTT publisher
├── TSESubscriber.java              # MQTT subscriber
├── PythonServerManager.java        # Python integration
├── ReadOutputs.java                # Output processing
├── ResultIO.java                   # Result I/O operations
├── architecture/                   # Architecture definitions
│   ├── ArchitectureCreator.java
│   ├── ArchitectureMethods.java
│   ├── constellations/             # Constellation types
│   ├── outputspecifications/       # Output specifications
│   ├── specifications/             # Core specifications
│   └── variable/                   # Variable definitions
├── decisions/                      # Decision-making components
│   ├── adg/                       # ADG (Architecture Decision Graph)
│   ├── search/                     # Search algorithms
│   └── [various decision types]
├── interfaces/                     # External interfaces
│   ├── GUIInterface.java
│   └── KnowledgeBaseInterface.java
├── model/                         # Data models
│   └── TSEResponse.java
├── templates/                      # Evaluation templates
├── tradespaceiterator/             # Tradespace iteration
│   ├── search/                     # Search strategies
│   └── [various iterator types]
└── util/                          # Utility classes
```

## Installation

### Prerequisites

- **Java 8** or higher (JDK required)
- **Maven 3.6+**
- **Python 3.7+** with pip

### Quick Installation

```bash
cd TSE_Module/tse

# Install all dependencies and build
make all
```

This command performs:
1. Installs JAR dependencies to local Maven repository
2. Installs Python package in development mode
3. Builds the Spring Boot application

### Manual Installation

If `make all` fails, perform these steps manually:

```bash
cd TSE_Module/tse

# 1. Install JAR dependencies
mvn install:install-file -Dfile=./lib/mopAOS-1.0.jar -DgroupId=seakers -DartifactId=mopAOS -Dversion=1.0 -Dpackaging=jar
mvn install:install-file -Dfile=./lib/conMOP-1.0.jar -DgroupId=seakers -DartifactId=conMOP -Dversion=1.0 -Dpackaging=jar

# 2. Install Python package
pip install -e .

# 3. Build Java application
mvn package
```

### Verification

```bash
# Check if JAR was created
ls -la target/tatc-ml-tse-1.0.jar

# Test Python package installation
python -c "import tatc_tse; print('Success')"
```

## Usage

### Starting the TSE Service

```bash
cd TSE_Module/tse

# Start Spring Boot application
mvn spring-boot:run
```

The service will start on `http://localhost:7500`

### Testing the Service

#### Method 1: REST API (Recommended)

```bash
# Test with sample request
curl -X POST http://localhost:7500/tse \
  -H "Content-Type: application/json" \
  -d @problems/landsat8.json
```

#### Method 2: Python Script

```bash
# Run using Python wrapper
python bin/tse.py problems/landsat8.json problems
```

#### Method 3: Direct Java Execution

```bash
# Run JAR directly
java -jar target/tatc-ml-tse-1.0.jar problems/landsat8.json problems
```

### Using with Python Tools

Once the TSE service is running, you can use the Python tools:

```bash
cd Python

# Install Python dependencies
pip install -r requirements.txt

# Run dummy requester to test the system
python dummyRequester.py
```

This will:
1. Start a callback server on port 7000
2. Send a test request to TSE on port 7500
3. Display received solutions in real-time

## Configuration

### Application Properties

The application uses Spring Boot configuration. Key properties:

```properties
# Server configuration
server.port=7500

# Logging
logging.level.tatc=INFO
logging.level.org.springframework.web=DEBUG

# MQTT configuration
mqtt.broker.url=tcp://localhost:1883
mqtt.client.id=tse-client

# Python integration
python.executable=python
python.scripts.path=../Python/
```

### Test Cases

The `problems/` directory contains various test cases:

- `landsat8.json` - Landsat 8 satellite mission
- `CaseStudy1.json` - TROPICS mission (microsatellites)
- `CaseStudy2.json` - Different constellation configuration
- `CaseStudy3.json` - Advanced mission parameters
- `CaseStudy4.json` - Complex multi-objective optimization
- `CaseStudy5.json` - Large-scale constellation design

## API Documentation

### POST /tse

**Request Format:**
```json
{
  "workflowId": "unique-workflow-id",
  "callbackUrl": "http://localhost:7000/callback",
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

**Response Format:**
```json
{
  "workflowId": "unique-workflow-id",
  "status": "SUCCESS",
  "outputDirectory": "/path/to/results",
  "message": "TSE execution completed successfully"
}
```

## Development

### Building from Source

```bash
# Clean and rebuild
mvn clean package

# Run tests
mvn test

# Install to local repository
mvn install
```

### Adding New Features

1. **New Evaluation Tools**: Add to `templates/` directory
2. **New Constellation Types**: Extend `architecture/constellations/`
3. **New Output Specifications**: Add to `architecture/outputspecifications/`
4. **New Search Strategies**: Implement in `tradespaceiterator/search/`

### Debug Mode

Enable debug logging:

```properties
logging.level.tatc=DEBUG
logging.level.org.springframework.web=DEBUG
```

## Troubleshooting

### Common Issues

1. **Port 7500 already in use**
   ```bash
   # Find process using port
   netstat -ano | findstr :7500
   # Kill process or change port in application.properties
   ```

2. **Missing JAR dependencies**
   ```bash
   # Reinstall JAR dependencies
   mvn install:install-file -Dfile=./lib/mopAOS-1.0.jar -DgroupId=seakers -DartifactId=mopAOS -Dversion=1.0 -Dpackaging=jar
   mvn install:install-file -Dfile=./lib/conMOP-1.0.jar -DgroupId=seakers -DartifactId=conMOP -Dversion=1.0 -Dpackaging=jar
   ```

3. **Python integration issues**
   ```bash
   # Reinstall Python package
   pip uninstall tatc-tse
   pip install -e .
   ```

4. **Maven build failures**
   ```bash
   # Clean and rebuild
   mvn clean package
   ```

### Logs

Check application logs for detailed error information:

```bash
# View Spring Boot logs
tail -f logs/spring-boot.log

# Enable debug logging
export LOGGING_LEVEL_TATC=DEBUG
mvn spring-boot:run
```

## Dependencies

### Java Dependencies (Maven)

- **Spring Boot 2.7.0** - Web framework
- **Orekit 8.0** - Orbital mechanics
- **Hipparchus 1.0** - Mathematical library
- **MOEA Framework 2.12** - Multi-objective optimization
- **Gson 2.8.0** - JSON processing
- **Eclipse Paho 1.2.5** - MQTT client
- **Neo4j Driver 4.4.0** - Graph database
- **JEP 4.0.3** - Python integration

### Python Dependencies

See `../Python/requirements.txt` for complete list of Python dependencies.

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License.
