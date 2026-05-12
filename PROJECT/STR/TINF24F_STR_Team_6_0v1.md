# System Test Report (STR)

## Projekt 6: API für den Digitalen Produktpass (DPP) im BaSyx Framework

### Customer
|Name|Mail|
|---|---|
|Markus Rentschler|rentschler@lehre.dhbw-stuttgart.de|
|Pawel Wojcik|pawel.wojcik@lehre.dhbw-stuttgart.de|

---

### Dokumenthistorie
|Version|Autor|Datum|Kommentar|
|---|---|---|---|
|1.0|Manuel Lutz|13.03.2026|Ersterstellung basierend auf der SRS|
|1.1|Manuel Lutz|13.03.2026|Überarbeitung, Umbenennung und Ergänzung der vorbereiteten Integrationstests|
|1.2|Manuel Lutz|15.03.2026|Status der vorbereiteten Integrationstests nach Ausführung aktualisiert (12/12 bestanden)|
|1.3|Manuel Lutz|27.03.2026|Anpassung der Integrationstests auf DIN EN 18222 DPP-Data-Object-Structure Mapping|
|1.4|Manuel Lutz|30.03.2026|Methoden-/Endpoint-Abgleich mit API-Mapping (PATCH/POST, Date-Query, Namenskonventionen) präzisiert|
|1.5|Manuel Lutz|09.05.2026|Real-Backend-Test-Struktur implementiert; Mock-Harness und Setup-Skript hinzugefügt; 26/26 Tests mit Umschaltungsmechanismus|

---

### Begrifflichkeiten und Abkürzungen
|Abkürzung|Bedeutung|
|---|---|
|DPP|Digitaler Produktpass|
|AAS|Asset Administration Shell|
|BaSyx|Open-Source Framework|
|Submodel|Teilstruktur eines AAS|
|OpenAPI|Industriestandard zur Beschreibung von APIs|
|CRS|Customer Requirement Specification|
|SRS|Software Requirement Specification|
|STR|System Test Report|

---

### Inhaltsverzeichnis
1. [Einführung](#1-einführung)
2. [Testumgebung](#2-testumgebung)
3. [Systemtestfälle](#3-systemtestfälle)
   1. [Backend-Testfälle](#31-backend-testfälle)
   2. [Frontend-Testfälle](#32-frontend-testfälle)
   3. [Nicht-funktionale Testfälle](#33-nicht-funktionale-testfälle)
4. [Vorbereitete Integrationstests](#4-vorbereitete-integrationstests)
   1. [Ziel und Abgrenzung](#41-ziel-und-abgrenzung)
   2. [API-Integration](#42-api-integration)
   3. [Frontend-Backend-Integration](#43-frontend-backend-integration)
   4. [BaSyx-Integration](#44-basyx-integration)
5. [Artefakte und Ablage](#5-artefakte-und-ablage)
6. [Zusammenfassung](#6-zusammenfassung)

---

## 1. Einführung
Dieses STR dokumentiert die geplanten und vorbereiteten Systemtests für die Implementierung der DPP-API gemäß DIN EN 18222 im BaSyx Framework. Grundlage sind die funktionalen und nicht-funktionalen Anforderungen aus dem SRS.

Da das Backend aktuell noch nicht vollständig implementiert ist, liegt der Schwerpunkt dieses Dokuments auf:
- der vollständigen Ableitung der Systemtestfälle aus dem SRS,
- der Vorbereitung automatisierbarer Integrationstests mit Mocks,
- der Definition einer klaren Überführung auf echte Backend und BaSyx-Integrationen.

## 2. Testumgebung
- **Zielplattform:** Web-Anwendung im BaSyx-Kontext
- **Frontend:** Vue 3 / Vitest / Vue Test Utils
- **Testausführung:** Vitest im bestehenden Frontend-Projekt
- **Mocking:** `vi.fn()` und `vi.stubGlobal()` für HTTP- und Service-Mocks
- **Referenzdaten:** Beispiel-DPPs auf Basis der in der SRS genannten IDTA-Submodelle
- **Echte Integration (später):** BaSyx AAS Repository, Submodel Repository, Registry, Discovery

## 3. Systemtestfälle

### 3.1 Backend-Testfälle

|Test-ID|Beschreibung|Schritte|Erwartetes Ergebnis|Tatsächliches Ergebnis|Status|
|---|---|---|---|---|---|
|TC-BE-01|Erstellung eines DPP|1. Sende POST Request mit DPP-Daten<br>2. Prüfe Validierung<br>3. Speichere in AAS|Neues DPP-Submodel in AAS| |Not Executed|
|TC-BE-02|Abrufen eines DPP per ID|1. Sende GET Request mit `dppId`<br>2. Erhalte vollständige Daten|Volle DPP-Daten zurück| |Not Executed|
|TC-BE-03|Updaten eines DPP per ID|1. Sende PATCH Request mit Updates<br>2. Prüfe Integrität<br>3. Aktualisiere Submodelle|Aktualisierte Daten gespeichert| |Not Executed|
|TC-BE-04|Löschen eines DPP per ID|1. Sende DELETE Request mit `dppId`<br>2. Entferne aus System|DPP nicht mehr auffindbar| |Not Executed|
|TC-BE-05|Abrufen eines DPP per `productId`|1. Sende GET mit `productId`<br>2. Mappe zu `dppId`<br>3. Erhalte Daten|Zugeordnetes DPP zurück| |Not Executed|
|TC-BE-06|Abrufen eines älteren DPP per Zeitstempel|1. Sende GET mit `productId` und Zeitstempel<br>2. Erhalte historische Version|Historische DPP-Daten| |Not Executed|
|TC-BE-07|Abrufen mehrerer DPPs per Liste|1. Sende POST mit Liste von `productIds` im Body<br>2. Erhalte Liste von `dppIds`|Liste von DPPs| |Not Executed|
|TC-BE-08|Registrierung im AAS Registry|1. Registriere neues DPP<br>2. Prüfe Registry|DPP im Registry auffindbar| |Not Executed|
|TC-BE-09|Abrufen spezifischer Submodel-Daten|1. Sende GET mit `dppId` und `elementId`<br>2. Erhalte Submodel-Element|Gespeicherte Daten zurück| |Not Executed|
|TC-BE-10|Updaten spezifischer Submodel-Daten|1. Sende PATCH mit `dppId`, `elementId` und Daten<br>2. Aktualisiere Element|Element aktualisiert| |Not Executed|
|TC-BE-11|Abrufen eines Elements per `elementPath`|1. Sende GET mit `dppId` und `elementPath`<br>2. Erhalte einzelnes Element|Element zurück| |Not Executed|
|TC-BE-12|Updaten eines Elements per `elementPath`|1. Sende PATCH mit `dppId`, `elementPath` und Daten<br>2. Aktualisiere Element|Element aktualisiert| |Not Executed|
|TC-BE-13|Fehler bei ungültigen Parametern|1. Sende Request mit falschen Parametern<br>2. Erhalte HTTP Error|Korrekte Fehlerantwort| |Not Executed|

### 3.2 Frontend-Testfälle

|Test-ID|Beschreibung|Schritte|Erwartetes Ergebnis|Tatsächliches Ergebnis|Status|
|---|---|---|---|---|---|
|TC-FE-01|Laden eines DPP|1. Eingabe AAS ID oder URL<br>2. Lade DPP<br>3. Zeige Submodelle|Volles DPP angezeigt| |Not Executed|
|TC-FE-02|Navigation innerhalb des DPP|1. Öffne DPP<br>2. Prüfe Seitenleiste|Seitenleiste mit Submodellen| |Not Executed|
|TC-FE-03|Klickbare Navigation|1. Klicke auf Submodel in der Seitenleiste<br>2. Zeige Informationen|Submodel-Informationen angezeigt| |Not Executed|
|TC-FE-04|Highlighting des Submodells|1. Navigiere zu Submodel<br>2. Prüfe Hervorhebung|Aktuelles Submodel hervorgehoben| |Not Executed|
|TC-FE-05|Menü oberhalb des Viewers|1. Prüfe Menü<br>2. Wechsle Modi|Menü mit DPP-, AAS- und Submodel-Viewer| |Not Executed|
|TC-FE-06|Informationen zum Produkt|1. Öffne DPP<br>2. Prüfe Header|`productId`, Version und Name angezeigt| |Not Executed|
|TC-FE-07|Anzeige der Kategorien|1. Öffne Submodel<br>2. Prüfe Darstellung|Zweispaltige, übersichtliche Darstellung| |Not Executed|
|TC-FE-08|Fehlende Daten|1. Öffne DPP mit fehlenden Daten<br>2. Prüfe Anzeige|Fehlende Daten markiert| |Not Executed|
|TC-FE-09|Tooltips für Daten|1. Hover über Daten<br>2. Prüfe Tooltip|Tooltip mit DIN-/IDTA-Informationen| |Not Executed|
|TC-FE-10|Responsives Design|1. Öffne auf Desktop und Mobilgerät<br>2. Prüfe Layout|Responsive Darstellung| |Not Executed|

### 3.3 Nicht-funktionale Testfälle

|Test-ID|Beschreibung|Schritte|Erwartetes Ergebnis|Tatsächliches Ergebnis|Status|
|---|---|---|---|---|---|
|TC-NFR-01|OpenAPI-Konformität|1. Validiere API gegen OpenAPI-Spezifikation<br>2. Prüfe DIN EN 18222 Konformität|Konform mit Spezifikation| |Not Executed|
|TC-NFR-02|BaSyx-Konformität|1. Prüfe API Calls<br>2. Stelle sicher, dass nur BaSyx-Schnittstellen verwendet werden|Nur BaSyx-Schnittstellen verwendet| |Not Executed|
|TC-NFR-03|Automatisierte Tests|1. Führe Unit- und Integrationstests aus<br>2. Prüfe Coverage|Tests vorhanden und erfolgreich| |Not Executed|

## 4. Vorbereitete Integrationstests

### 4.1 Ziel und Abgrenzung
Weil das Backend noch nicht vollständig implementiert ist, wurden Integrationstests als vorbereitete Mock-Tests erstellt. Diese Tests prüfen bereits heute:
- Request- und Response-Strukturen,
- die Interaktion des Frontends mit einer DPP-API,
- die Orchestrierung geplanter BaSyx-Integrationen.

Sobald das Backend bereitsteht, können die Mock-Antworten schrittweise durch echte API-Aufrufe ersetzt werden.

### 4.2 API-Integration (DIN EN 18222 konform)

Mapping-Referenzstand für die folgenden API-Tests:
- Basis: DPP-Data-Object-Structure **v1 (2026-03-22)**
- Hinweis: **v2 (2026-03-25)** ist im Mapping-Dokument als "in work" gekennzeichnet und daher noch nicht als verbindliche Testbasis verwendet.

Namenskonventionen aus dem Mapping sind bewusst unterschiedlich und werden in den Tests entsprechend abgebildet:
- CreateDPP-Response verwendet `dppID`
- DPP-Identifier-Listen verwenden `dppId`

|Test-ID|Endpoint|Beschreibung|Ziel|Status|
|---|---|---|---|---|
|IT-API-01|POST /dpps|CreateDPP|Erzeugung eines DPP mit `info` und `submodels` (SuccessCreated Response)|Pass|
|IT-API-02|GET /dpps/{dppId}|ReadDPPById|Abruf vollständiger DPP-Struktur mit `payload`|Pass|
|IT-API-03|GET /dppsByProductId/{productId}|ReadDPPByProductId|Abruf eines DPP über `productId`-Mapping|Pass|
|IT-API-04|GET /dppsByProductIdAndDate/{productId}?date={ISO-8601}|ReadDPPVersionByProductIdAndDate|Abruf einer spezifischen DPP-Versionierung mit Datum|Pass|
|IT-API-05|POST /dppsByProductIds|ReadDPPIdsByProductIds|Abruf von DPP-Identifiern über Liste von `productIds`|Pass|
|IT-API-06|PATCH /dpps/{dppId}|UpdateDPPById|Update der DPP-Struktur (Partial)|Pass|
|IT-API-07|DELETE /dpps/{dppId}|DeleteDPPById|Löschen eines DPP (nur statusCode Response)|Pass|
|IT-API-08|POST /registerDPP|PostNewDPPToRegistry|Registrierung im AAS Registry mit `registryIdentifier`|Pass|
|IT-API-09|GET /dpps/{dppId}/collections/{elementId}|ReadDataElementCollection|Abruf von Submodel-Daten per `elementId`|Pass|
|IT-API-10|GET /dpps/{dppId}/elements/{elementPath}|ReadDataElement|Abruf einzelnen Elements per `elementPath`|Pass|
|IT-API-11|PATCH /dpps/{dppId}/collections/{elementId}|UpdateDataElementCollection|Update von Submodel-Datensammlungen|Pass|
|IT-API-12|PATCH /dpps/{dppId}/elements/{elementPath}|UpdateDataElement|Update einzelner Elemente|Pass|
|IT-API-13|ClientErrorBadRequest|Error Handling|Prüfung fehlerhafter Requests mit `ErrorMessage` Struktur|Pass|
|IT-API-14|ClientErrorResourceNotFound|Error Handling|Prüfung auf 404 mit structured error responses|Pass|

### 4.3 Frontend-Backend-Integration (DIN EN 18222 konform)

|Test-ID|Beschreibung|Ziel|Status|
|---|---|---|---|
|IT-FB-01|Viewer lädt DPP mit voller Struktur|Prüfung des Ladens von `info`, `submodels` und Status-Codes|Pass|
|IT-FB-02|Navigation lädt Data Element Collection|Prüfung der Datensammlung über `/collections/{elementId}`|Pass|
|IT-FB-03|Handling fehlender Felder|Rendering von N/A bei fehlenden Nameplate-Feldern (z.B. `ManufacturerProductDesignation`)|Pass|
|IT-FB-04|Error-Response Handling|Prüfung auf `ClientErrorResourceNotFound` und error message Struktur|Pass|
|IT-FB-05|Responsives Layout|Prüfung unterschiedlicher Layouts für Desktop/Mobil|Pass|
|IT-FB-06|HTTP Error Codes|Validierung von 400/404 Responses mit korrektem statusCode|Pass|
|IT-FB-07|Loading State|Prüfung visuellen Ladens während asynchroner Datenbeschaffung|Pass|

### 4.4 BaSyx-Integration (DIN EN 18222 konform)

|Test-ID|Beschreibung|Ziel|Status|
|---|---|---|---|
|IT-BX-01|DPP zu AAS Transformation|Transformation von DPP `info` + `submodels` zu AAS-Struktur mit Asset Information|Pass|
|IT-BX-02|Registry Entry Management|Vorbereitung und Registrierung von DPPs mit `registryIdentifier`|Pass|
|IT-BX-03|Discovery Service Integration|Abruf von DPPs über `productId` über Discovery Service|Pass|
|IT-BX-04|Submodel Reference Handling|Prüfung von Semantic IDs und Submodel-Referenzen in AAS|Pass|
|IT-BX-05|Asset Information Extraction|Extraktion und Verwendung von Asset Information für AAS Creation|Pass|

## 5. Artefakte und Ablage

### Test-Dateien
Die vorbereiteten Integrationstests liegen im Frontend-Projekt unter:
- [SOURCE/frontend/aas-web-ui/tests/integration/DppApiIntegration.test.ts](../../SOURCE/frontend/aas-web-ui/tests/integration/DppApiIntegration.test.ts) – 14 Tests für DIN EN 18222 API-Endpoints
- [SOURCE/frontend/aas-web-ui/tests/integration/FrontendBackendIntegration.test.ts](../../SOURCE/frontend/aas-web-ui/tests/integration/FrontendBackendIntegration.test.ts) – 7 Tests für Viewer und API Interaktion
- [SOURCE/frontend/aas-web-ui/tests/integration/BaSyxIntegration.test.ts](../../SOURCE/frontend/aas-web-ui/tests/integration/BaSyxIntegration.test.ts) – 5 Tests für BaSyx AAS Orchestrierung
- [SOURCE/frontend/aas-web-ui/tests/integration/integrationHarness.ts](../../SOURCE/frontend/aas-web-ui/tests/integration/integrationHarness.ts) – Geteilte Mock-Backend Implementation und Test Utilities

### Setup und Dokumentation
- [SOURCE/frontend/aas-web-ui/REAL_BACKEND_SETUP.md](../../SOURCE/frontend/aas-web-ui/REAL_BACKEND_SETUP.md) – Anleitung zur Ausführung gegen echtes Backend
- [SOURCE/frontend/aas-web-ui/scripts/setupTestData.mjs](../../SOURCE/frontend/aas-web-ui/scripts/setupTestData.mjs) – Automatisiertes Skript zur Vorbereitung von Test-Daten im echten Backend

### Standardreferenzen
- [PROJECT/SAS/mapping/DPP-Data-Object-Structure.md](../../PROJECT/SAS/mapping/DPP-Data-Object-Structure.md) – Vollständige DIN EN 18222 Datenstrukturspezifikation

### Konfiguration und Automation
Zur reproduzierbaren Ausführung im Repository wurden außerdem hinterlegt:
- [SOURCE/frontend/aas-web-ui/package.json](../../SOURCE/frontend/aas-web-ui/package.json) mit dem Skript `test:integration`
- [SOURCE/frontend/aas-web-ui/package.json](../../SOURCE/frontend/aas-web-ui/package.json) mit dem Skript `test:integration:real` für Real-Backend-Tests (ohne Mocking, mit `RUN_REAL_BACKEND_TESTS=true` und konfigurierbarer `DPP_BACKEND_BASE_URL`)
- [SOURCE/frontend/aas-web-ui/package.json](../../SOURCE/frontend/aas-web-ui/package.json) mit `engines.node` zur Vorgabe der unterstützten Node.js-Versionen für WSL und CI
- [.github/workflows/systemtest-issue-tracker.yml](../../.github/workflows/systemtest-issue-tracker.yml) zur automatisierten Ausführung der STR-nahen Tests in GitHub Actions

### 5.1 Real Backend Testing Vorbereitung

Die Tests können nach Backend-Fertigstellung **ohne Code-Änderungen** gegen das echte Backend ausgeführt werden.

**Schritt 1: Backend starten**
Stelle sicher, dass der DPP-Backend auf `http://localhost:8080` läuft (oder konfiguriere `DPP_BACKEND_BASE_URL` für abweichende URLs).

**Schritt 2: Test-Daten vorbereiten**
Führe das Setup-Skript aus:
```bash
cd SOURCE/frontend/aas-web-ui
node scripts/setupTestData.mjs
# Oder mit benutzerdefinierten Backend-URL:
node scripts/setupTestData.mjs http://backend.example.com:8080
```

Das Skript erstellt automatisch:
- Test-DPP mit ID `urn:uuid:test-dpp-1`
- Product-ID Mapping `urn:uuid:prod-123`
- Registry-Eintrag `aas-registry://dpps/test-dpp-1`
- Submodel-Daten (Nameplate, Technical Data, Carbon Footprint)

**Schritt 3: Real-Backend-Tests ausführen**
```bash
yarn test:integration:real
# Oder:
RUN_REAL_BACKEND_TESTS=true DPP_BACKEND_BASE_URL=http://localhost:8080 yarn test:integration
```

Erwartetes Ergebnis: **26/26 Tests bestanden** (API: 14, Frontend: 7, BaSyx: 5)

**Weitere Details:**
Siehe [REAL_BACKEND_SETUP.md](../../SOURCE/frontend/aas-web-ui/REAL_BACKEND_SETUP.md)

## 6. Zusammenfassung
Die Systemtests sind inhaltlich aus dem SRS abgeleitet und wurden auf die [DIN EN 18222 DPP-Data-Object-Structure Spezifikation](../../PROJECT/SAS/mapping/DPP-Data-Object-Structure.md) mappiert. Vorbereitete, automatisierte Integrationstests sind erstellt und arbeiten aktuell mit Mocking. Damit ist eine belastbare, standards-konforme Testbasis vorhanden, obwohl das Backend noch nicht vollständig fertiggestellt ist.

Aktueller Ausführungsstand der vorbereiteten Integrationstests (Vitest): **3 Testdateien, 26/26 Tests bestanden** (API: 14, Frontend-Backend: 7, BaSyx: 5).

**Umschaltungsmechanismus für echtes Backend:**
- **Standard:** Mock-basierte Tests (`yarn test:integration`) – läuft ohne Backend-Installation
- **Production-Ready:** Real-Backend-Tests (`yarn test:integration:real`) – läuft gegen echten Backend nach Vorbereitung
- **Setup-Automatisierung:** `node scripts/setupTestData.mjs` erstellt Test-Daten automatisch

**Mappings und Konformität:**
- API-Tests folgen exakt den DIN EN 18222 Endpoints: CreateDPP, ReadDPPById, ReadDPPByProductId, ReadDPPVersionByProductIdAndDate, ReadDPPIdsByProductIds, UpdateDPPById, DeleteDPPById, PostNewDPPToRegistry, ReadDataElementCollection, ReadDataElement, UpdateDataElementCollection, UpdateDataElement, sowie Error Responses.
- Typsignaturen korrespondieren mit standardisierten Status Codes, Payload-Strukturen, und Error Message Objekten.
- BaSyx-Tests prüfen Transformation und Orchestrierung gemäß AAS-Datenmodell.

Nächste sinnvolle Schritte:
1. **Backend-Endpunkte** auf die DIN EN 18222 konformen Test-Definitions abgleichen.
2. **Mock-Responses** schrittweise durch echte Backend-Antworten ersetzen (automatisch über Umgebungsvariable).
3. **BaSyx Repository, Registry und Discovery Integration** validieren.
4. **Testergebnisse** nach Ausführung in diesem STR dokumentieren.

**Gesamtstatus:** DIN EN 18222 konforme Mock-basierte Integrationstests erstellt und erfolgreich ausgeführt (26/26). Real-Backend-Test-Infrastruktur bereit; wechselt automatisch beim Setzen von `RUN_REAL_BACKEND_TESTS=true`. Setup-Skript für Test-Datenvorbereitung vorhanden. **Bereit für echte Backend-Integration.**
