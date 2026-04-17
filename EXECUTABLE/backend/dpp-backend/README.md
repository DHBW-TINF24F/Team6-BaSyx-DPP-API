# DPP Controller

## Docker Container
```bash
# Build the Docker image
docker build -t dpp-backend .

# Run the container
docker run --rm -p 8080:8080 dpp-backend
```
Server runs on [http://localhost:8080](http://localhost:8080).

## Build & Run (local)
```bash
mvn clean install
java -jar target/dpp-backend-0.0.1-SNAPSHOT.jar
```
Server runs on [http://localhost:8080](http://localhost:8080) by default.

---

## API Status Checklist
[X] **POST /dpps** – Create a new DPP (works, creates shell in registry).  
[X] **GET /dpps/{dppId}** – Retrieve a DPP (parses dppId, searches across registries).  
[X] **PATCH /dpps/{dppId}** – Update a DPP (implemented, but issue reported: DPP ID not found).  
[X] **DELETE /dpps/{dppId}** – Delete a DPP (removes shell from registry).  
[X] **GET /dppsByProductId/{productId}** – Retrieve DPP by Product ID (resolves URL via service).  
[] **GET /dppsByProductIdAndDate/{productId}?date=...** – Retrieve DPP by Product ID and date (not yet functional, AAX file upload planned).  
[ ] **POST /dppsByProductIds** – Get DPP IDs for a list of Product IDs (implemented but needs adaption for multiple DPPs per product).  
[X] **POST /registerDPP** – Register new DPP in a registry (uses configured DPP Registry API such as HARTING).  
[ ] **Fine-granular DPP Lifecycle API** – Pending; lifecycle endpoints such as `/dpps/{dppId}/collections/{elementId}` not yet implemented.

## DPP Lifecycle API Examples
All calls use the base URL `http://localhost:8080`.  
DPP IDs are **Base64 encoded** (e.g., `aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvMDIwMTA4MzIxMDE=`).

### POST /dpps (Create)
```bash
curl -X POST "http://localhost:8080/dpps?productId=aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvMDIwMTA4MzIxMDE=" \
  -H "Content-Type: application/json" \
  -d '{
    "id": "https://dpp40.harting.com/shells/02010832101",
    "idShort": "ZSN11",
    "description": [{"language": "en", "text": "Harting ZSN1 DPP v2"}]
  }'
```
Creates a shell in the local or external registry and generates a versioned `dppId`.

### GET /dpps/{dppId} (Read)
```bash
curl -X GET "http://localhost:8080/dpps/aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvMDIwMTA4MzIxMDE=#2026-04-10T08:26:35.306622886Z"
```
Fetches the shell and its payload through the registry service.

### PATCH /dpps/{dppId} (Update)
```bash
curl -X PATCH "http://localhost:8080/dpps/aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvMDIwMTA4MzIxMDE=#2026-04-10T08:26:35.306622886Z" \
  -H "Content-Type: application/json" \
  -d '{"idShort": "ZSN11-updated"}'
```
Updates the shell (**Issue**: ID recognition fails — check `parseDppId` logic).

### DELETE /dpps/{dppId} (Delete)
```bash
curl -X DELETE "http://localhost:8080/dpps/aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMQ=="
```
Deletes the shell from the registry.

### GET /dppsByProductId/{productId}
```bash
curl -X GET "http://localhost:8080/dppsByProductId/aHR0cHM6Ly90ZWNoYnJldy5jb20vc2hlbGxzLzEyMzQ1Njc4OQ=="
```
Retrieves the current DPP using the Product ID service.

### GET /dppsByProductIdAndDate/{productId}
```bash
curl -X GET "http://localhost:8080/dppsByProductIdAndDate/aHR0cHM6Ly90ZWNoYnJldy5jb20vc2hlbGxzLzEyMzQ1Njc4OQ==?date=2026-04-10"
```
Retrieves the version by date (not final yet, **AAX upload** planned).

### POST /dppsByProductIds
```bash
curl -X POST "http://localhost:8080/dppsByProductIds?limit=10" \
  -H "Content-Type: application/json" \
  -d '{"productIds": ["prod1-b64", "prod2-b64"]}'
```
Lists DPP IDs for a set of Product IDs (needs extension for **multiple DPPs per product**).

## DPP Registry API
**POST /registerDPP** registers a new DPP in a central registry (e.g., HARTING, based on configuration).  
It creates a DPP shell and returns its unique identifier.
**Example:**
```bash
curl -X POST "http://localhost:8080/registerDPP" \
  -H "Content-Type: application/json" \
  -d '{"id": "https://example.com/shells/test", "idShort": "TestDPP"}'
```

## Additional Endpoints
- **GET /api/v1/dpp/health** – Returns server health and registry count.  
- **GET /api/v1/dpp/list?limit=5** – Lists DPP IDs from connected registries.  
- **GET /api/v1/dpp?id=URL** – Fetches a full DPP from the given URL.