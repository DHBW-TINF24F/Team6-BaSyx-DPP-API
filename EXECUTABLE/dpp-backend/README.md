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
| Status | ID       | Name                                   | Beschreibung                                      | Akzeptanzkriterium                                   | Curl Befehl | Call |
|--------|----------|----------------------------------------|--------------------------------------------------|-----------------------------------------------------|-------------|------|
| ✅ | FR-BE-01 | Erstellung eines DPP                   | Neues DPP validieren und speichern               | DPP-Submodel ist in AAS vorhanden                   | `curl -X POST "http://api.com/dpps" -H "Content-Type: application/json" -d '{ "shell": { "id": "urn:uuid:7e51f712-429a-419a-9e8c-8f43c393850b", "dpps": [ { "productId": "PID", "version": "1.0.0", "submodels": [ { "name": "CarbonFootPrint", "version": "1.2.0", "reference": "urn:submodel:cfp:001" }, { "name": "DigitalNamePlate", "version": "1.0.1", "reference": "urn:submodel:dnp:002" } ] } ] } }'` | `POST /dpps` |
| ✅ | FR-BE-02 | Abrufen eines DPP per ID               | DPP anhand dppId abrufen                         | Vollständige Daten liegen vor                       | `curl -X GET "http://api.com/dpps/{dppId}"` | `GET /dpps/{dppId}` |
| ✅ | FR-BE-03 | Updaten eines DPP per ID               | DPP aktualisieren                                | Daten sind aktualisiert gespeichert                 | `curl -X PUT "http://api.com/dpps/{dppId}" -H "Content-Type: application/json" -d '{ "version": "{input}", "submodels": [ {"name": "{input}", "version": "{input}", "reference": "{input}"}, {"name": "{input}", "version": "{input}", "reference": "{input}"} ] }'` | `PUT /dpps/{dppId}` |
| ✅ | FR-BE-04 | Löschen eines DPP per ID               | DPP löschen                                      | DPP ist nicht mehr auffindbar                       | `curl -X DELETE http://api.com/dpps/{dppId}` | `DELETE /dpps/{dppId}` |
| ✅ | FR-BE-05 | Abrufen eines DPP per productID        | DPP über productId abrufen                       | Zugeordneter DPP liegt vollständig vor              | `curl -X GET "http://api.com/dppsByProductId/{productId}"` | `GET /dppsByProductId/{productId}` |
| ✅ | FR-BE-06 | Historisches DPP abrufen               | DPP per productId + Zeit                         | Historische Daten liegen vollständig vor            | `curl -X GET "http://api.com/dppsByProductIdAndDate/{productId}?timeStamp={timestamp}"` | `GET /dppsByProductIdAndDate/{productId}?timeStamp={timestamp}` |
| ✅ | FR-BE-07 | Mehrere DPPs per productID Liste       | Liste von productIds → dppIds                    | Vollständige Liste vorhanden                        | `curl -X POST "http://api.com/dppIdsByProductIds" -H "Content-Type: application/json" -d '["{productId}","{productId}"]'` | `POST /dppIdsByProductIds` |
| ❌ | FR-BE-08 | Registrierung im AAS Registry          | DPP registrieren                                 | DPP ist im Registry auffindbar                      | `curl -X POST http://api.com/registry -H "Content-Type: application/json" -d '{...}'` | `POST /registry` |
| ❌ | FR-BE-09 | Submodel-Daten abrufen                 | Submodel per elementId abrufen                   | Daten des Elements liegen vor                       | `curl http://api.com/dpp/{dppId}/submodels/{elementId}` | `GET /dpp/{dppId}/submodels/{elementId}` |
| ❌ | FR-BE-10 | Submodel-Daten updaten                 | Submodel aktualisieren                           | Daten wurden angepasst                              | `curl -X PATCH http://api.com/dpp/{dppId}/submodels/{elementId} -H "Content-Type: application/json" -d '{...}'` | `PATCH /dpp/{dppId}/submodels/{elementId}` |
| ❌ | FR-BE-11 | Element per elementPath abrufen        | Einzelnes Element abrufen                        | Element liegt korrekt vor                           | `curl http://api.com/dpp/{dppId}/elements?path={elementPath}` | `GET /dpp/{dppId}/elements?path={elementPath}` |
| ❌ | FR-BE-12 | Element per elementPath updaten        | Einzelnes Element aktualisieren                  | Element wurde korrekt aktualisiert                  | `curl -X PATCH "http://api.com/dpp/{dppId}/elements?path={elementPath}" -H "Content-Type: application/json" -d '{...}'` | `PATCH /dpp/{dppId}/elements?path={elementPath}` |
| ❌ | FR-BE-13 | Fehler bei ungültigen Parametern       | HTTP Error bei falscher Eingabe                  | HTTP Error wird zurückgegeben                       | `curl -X GET http://api.com/dpp/invalid` | `GET /dpp/invalid` (400/422) |
