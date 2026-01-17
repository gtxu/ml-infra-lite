# ml-infra-lite

A lightweight, high-performance ML model serving infrastructure focusing on **Zero-Downtime Hot-swapping** and **Thread-safe Model Management**.

## Key Features
- **Atomic Hot-swapping**: Switch ML models in memory without interrupting ongoing inference requests.
- **Concurrency Control**: Implements `ReentrantReadWriteLock` to balance high-concurrency reads with exclusive writes.
- **Multi-Module Architecture**: Decoupled layers (API, Core, Server) for better maintainability and testability.
- **S3-Compatible Storage (Ready)**: Built-in support for MinIO and AWS S3 for model asset management.

## Architecture
The project follows a clean architecture pattern:
1. **ml-infra-api**: The contract layer defining the `InferenceEngine` interface.
2. **ml-infra-core**: The business logic layer containing the `ModelManager` and storage abstractions.
3. **ml-infra-server**: The hosting layer powered by Dropwizard, providing RESTful endpoints and Swagger documentation.

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+
- Docker (optional, for MinIO)

### Build & Run
```bash
# Clone and build
mvn clean install

# Run the server locally
cd ml-infra-server
java -jar target/ml-infra-server-1.0-SNAPSHOT.jar server ../config.yml

```

### Quick Start with Docker
```bash
# Build and start both the ML Server and MinIO
docker-compose up --build