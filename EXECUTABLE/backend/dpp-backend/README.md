# DPP Backend Entwicklung

## 1. BaSyx Framework (Docker)
docker compose -f docker-compose.webui.yml up -d
docker ps

Oder: run_basyx-webui.ps1

Ports:
```
8081 - BaSyx AAS Env + Swagger
3000 - BaSyx Web UI
27017 - MongoDB
```
---

## 2. Backend Entwicklung
cd backend/dpp-backend

DppController.java ausführen (VS Code: Java Extension)

Port: 8080 - DPP REST API

---

## 3. API Test
curl http://localhost:8080/api/v1/dpp/health <br>
curl http://localhost:8080/api/v1/dpp/urn:product:123

POST:
curl -X POST http://localhost:8080/api/v1/dpp -H "Content-Type: application/json" -d "{\"productId\":\"urn:battery:456\",\"manufacturer\":\"Team6\"}"

---

## Entwicklung Workflow
mvnw.cmd spring-boot:run    # Hot Reload<br>
Ctrl+C                      # Stop<br>
Code ändern → Enter         # Auto Restart<br>

Endpoints (/api/v1/dpp):

| Methode | Pfad    | Aktion          |
|---------|---------|-----------------|
| GET     | /health | Health Check    |
| GET     | /{id}   | DPP abrufen     |
| POST    | /       | DPP erstellen   |
| GET     | /       | DPPs listen     |

---

## Konfiguration (application.yml)
```
server:
  port: 8080

management:
  endpoints:
    web:
      exposure:
        include: health,info
```
---


## Projektstruktur
```
backend/
├── dpp-backend/
│   ├── src/main/java/…
│   ├── src/main/resources/
│   ├── pom.xml
│   └── mvnw.cmd
└── Dockerfile
```
---

## ALLE PORTS

| Port   | Service                  |
|--------|--------------------------|
| 8080   | DPP Backend REST API     |
| 8081   | BaSyx AAS Env + Swagger  |
| 3000   | BaSyx Web UI             |
| 27017  | MongoDB                  |
