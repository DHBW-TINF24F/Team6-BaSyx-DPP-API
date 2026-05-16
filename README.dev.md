![GitHub](https://img.shields.io/github/license/eclipse-basyx/basyx-aas-web-ui) 
[![Deploy Swagger Specification](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_swagger.yml/badge.svg)](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_swagger.yml)

# TINF24F_Team6_BaSyx_DPP_API – Developer README

## Quick Start (Recommended)

For a fast startup, run the script in the root directory of the repository:

```bash
./startup.sh
```
>[!NOTE]
If the script is not executable, run chmod +x ./startup.sh first (Linux/macOS).

### Manual Frontend Development

Navigate to the frontend directory:
```bash 
cd SOURCE/frontend
```
Install dependencies and start the development server:

```bash
yarn install
yarn dev
```

>[!WARNING]
The first startup may take a while.

## Backend Development
>[!IMPORTANT]
Before starting the backend locally, stop the corresponding Docker container (if running) to free up port 8080.
Option 1: Maven + java -jar (Recommended for Development)
Bash# 1. Go to the backend directory
cd EXECUTABLE/backend/dpp-backend

### Build backend
```bash
cd ./EXECUTABLE/dpp-backend
mvn clean install
```

### Start the application
```bash
java -jar target/dpp-backend-0.0.1-SNAPSHOT.jar
```
Server runs at: http://localhost:8080

## Run with Docker
```bash
cd ./EXECUTABLE/backend/dpp-backend/
docker build -t dpp-backend .
```

## Run the container
```bash
docker run --rm -p 8080:8080 dpp-backend
```
Server runs at: http://localhost:8080

Detailed Backend Documentation (endpoints, examples, configuration, etc.) can be found in the [Swagger](https://srv01.noah-becker.de/uni/swe/swagger/)