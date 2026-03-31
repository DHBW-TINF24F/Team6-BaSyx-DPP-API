# DPP Controller README

## Overview
DPP Controller is a Spring Boot REST API for managing Digital Product Passports (DPPs) based on Asset Administration Shell (AAS) standards. It provides discovery from registries (HARTING and local), CRUD operations, and direct DPP fetching with submodels.


### Build & Run
```bash
mvn clean install
java -jar target/dpp-backend-0.0.1-SNAPSHOT.jar
```


## Endpoints

### Health Check
GET /api/v1/dpp/health

text
Returns service status and configured registry count.

**Response:**
```json
{
  "status": "UP",
  "registries": 2
}
```

### Discovery
GET /api/v1/dpp/list?limit=5

text
Lists unique DPP shell IDs from registries (local/external), with optional limit (default 10).

**Response structure:**
```json
{
  "statusCode": 200,
  "payload": {
    "local": ["id1", "id2"],
    "external": [{"name": "harting", "registry": "https://dpp40.harting.com:8081", "dppIds": ["id3"]}],
    "total": 3
  }
}
```

### DPP by URL
GET /api/v1/dpp?id=https://dpp40.harting.com/shells/02095002010200

text
Fetches complete DPP (shell + submodels) from URL. Validates URL and JSON, handles redirects, extracts submodels.

**Response:**
```json
{
  "statusCode": 200,
  "source": "https://dpp40.harting.com/shells/02095002010200",
  "payload": {
    "id": "...",
    "idShort": "...",
    "submodels": [{"id": "...", "data": {...}}]
  }
}
```

### CRUD Operations

#### Create DPP
POST /dpps

text
Creates DPP. Body: JSON with `dppId`.

**Example:**
```bash
curl -X POST http://localhost:8080/dpps \
  -H "Content-Type: application/json" \
  -d '{"dppId": "test-dpp-123"}'
```

#### Read/Update/Delete
GET /dpps/{dppId}
PATCH /dpps/{dppId}
DELETE /dpps/{dppId}

text

#### Product-based Queries
GET /dppsByProductId/{productId}
GET /dppsByProductIdAndDate/{productId}?date=2026-03-31
POST /dppsByProductIds?limit=10&cursor=abc

text

#### Registry
POST /registerDPP

text

## Testing with curl
Replace `http://localhost:8080` with your server URL.

```bash
# Health
curl http://localhost:8080/api/v1/dpp/health

# List DPPs (limit 3)
curl "http://localhost:8080/api/v1/dpp/list?limit=3"

# Fetch HARTING DPP example
curl "http://localhost:8080/api/v1/dpp?id=https://dpp40.harting.com/shells/02095002010200"

# Create DPP
curl -X POST http://localhost:8080/dpps \
  -H "Content-Type: application/json" \
  -d '{"dppId": "test-123", "productId": "prod-456"}'

# Read DPP
curl http://localhost:8080/dpps/test-123

# Product-based
curl http://localhost:8080/dppsByProductId/prod-456
```
