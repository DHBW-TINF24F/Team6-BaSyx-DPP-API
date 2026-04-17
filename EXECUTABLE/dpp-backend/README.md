# DPP Backend


## Quick Start

### Local Development
```bash
mvn clean package
java -jar target/dpp-backend-0.0.1-SNAPSHOT.jar
```

### Docker
```bash
# Build
docker build -t dpp-backend .

# Run (maps host:8081 → container:8080)
docker run --rm -p 8081:8080 dpp-backend
```