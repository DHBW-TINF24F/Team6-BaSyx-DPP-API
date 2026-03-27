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
- der Definition einer klaren Überführung auf echte Backend- und BaSyx-Integrationen.

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
|TC-BE-03|Updaten eines DPP per ID|1. Sende PUT Request mit Updates<br>2. Prüfe Integrität<br>3. Aktualisiere Submodelle|Aktualisierte Daten gespeichert| |Not Executed|
|TC-BE-04|Löschen eines DPP per ID|1. Sende DELETE Request mit `dppId`<br>2. Entferne aus System|DPP nicht mehr auffindbar| |Not Executed|
|TC-BE-05|Abrufen eines DPP per `productId`|1. Sende GET mit `productId`<br>2. Mappe zu `dppId`<br>3. Erhalte Daten|Zugeordnetes DPP zurück| |Not Executed|
|TC-BE-06|Abrufen eines älteren DPP per Zeitstempel|1. Sende GET mit `productId` und Zeitstempel<br>2. Erhalte historische Version|Historische DPP-Daten| |Not Executed|
|TC-BE-07|Abrufen mehrerer DPPs per Liste|1. Sende GET mit Liste von `productIds`<br>2. Erhalte Liste von `dppIds`|Liste von DPPs| |Not Executed|
|TC-BE-08|Registrierung im AAS Registry|1. Registriere neues DPP<br>2. Prüfe Registry|DPP im Registry auffindbar| |Not Executed|
|TC-BE-09|Abrufen spezifischer Submodel-Daten|1. Sende GET mit `dppId` und `elementId`<br>2. Erhalte Submodel-Element|Gespeicherte Daten zurück| |Not Executed|
|TC-BE-10|Updaten spezifischer Submodel-Daten|1. Sende PUT mit `dppId`, `elementId` und Daten<br>2. Aktualisiere Element|Element aktualisiert| |Not Executed|
|TC-BE-11|Abrufen eines Elements per `elementPath`|1. Sende GET mit `dppId` und `elementPath`<br>2. Erhalte einzelnes Element|Element zurück| |Not Executed|
|TC-BE-12|Updaten eines Elements per `elementPath`|1. Sende PUT mit `dppId`, `elementPath` und Daten<br>2. Aktualisiere Element|Element aktualisiert| |Not Executed|
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

### 4.2 API-Integration

|Test-ID|Beschreibung|Ziel|Automatisierungsgrad|Status|
|---|---|---|---|---|
|IT-API-01|DPP-Erstellung|Erzeugung eines DPP per POST und Prüfung des Response-Vertrags|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-API-02|DPP-Abruf per ID|Abruf eines DPP per `dppId` und Validierung zentraler Felder|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-API-03|Historischer Abruf|Prüfung des Abrufs einer historischen Version über Query-Parameter|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-API-04|Element-Update|Validierung eines gezielten Updates über `elementPath`|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-API-05|Fehlerbehandlung|Prüfung von HTTP-Fehlerfällen bei ungültigen Requests|Automatisiert vorbereitet|Executed with Mocks (Pass)|

### 4.3 Frontend-Backend-Integration

|Test-ID|Beschreibung|Ziel|Automatisierungsgrad|Status|
|---|---|---|---|---|
|IT-FB-01|Viewer lädt DPP|Prüfung des initialen Ladens im Viewer|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-FB-02|Navigation lädt Submodel-Daten|Prüfung der UI-Navigation und Datennachladung|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-FB-03|Fehlende Daten werden behandelt|Prüfung robuster UI-Darstellung bei lückenhaften Daten|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-FB-04|Responsives Layout|Prüfung unterschiedlicher Layouts für Desktop/Mobil|Automatisiert vorbereitet|Executed with Mocks (Pass)|

### 4.4 BaSyx-Integration

|Test-ID|Beschreibung|Ziel|Automatisierungsgrad|Status|
|---|---|---|---|---|
|IT-BX-01|AAS Repository Orchestrierung|Prüfung der Erzeugung einer AAS-Struktur aus DPP-Daten|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-BX-02|Registry und Discovery|Prüfung von Registrierung und Auffindbarkeit|Automatisiert vorbereitet|Executed with Mocks (Pass)|
|IT-BX-03|Last und Konsistenz|Prüfung paralleler Zugriffe und grundlegender Datenkonsistenz|Automatisiert vorbereitet|Executed with Mocks (Pass)|

## 5. Artefakte und Ablage
Die vorbereiteten Integrationstests liegen im Frontend-Projekt unter:
- [SOURCE/frontend/aas-web-ui/tests/integration/DppApiIntegration.test.ts](../../SOURCE/frontend/aas-web-ui/tests/integration/DppApiIntegration.test.ts)
- [SOURCE/frontend/aas-web-ui/tests/integration/FrontendBackendIntegration.test.ts](../../SOURCE/frontend/aas-web-ui/tests/integration/FrontendBackendIntegration.test.ts)
- [SOURCE/frontend/aas-web-ui/tests/integration/BaSyxIntegration.test.ts](../../SOURCE/frontend/aas-web-ui/tests/integration/BaSyxIntegration.test.ts)

Zur reproduzierbaren Ausführung im Repository wurden außerdem hinterlegt:
- [SOURCE/frontend/aas-web-ui/package.json](../../SOURCE/frontend/aas-web-ui/package.json) mit dem Skript `test:integration`
- [SOURCE/frontend/aas-web-ui/package.json](../../SOURCE/frontend/aas-web-ui/package.json) mit `engines.node` zur Vorgabe der unterstützten Node.js-Versionen für WSL und CI
- [.github/workflows/systemtest-issue-tracker.yml](../../.github/workflows/systemtest-issue-tracker.yml) zur automatisierten Ausführung der STR-nahen Tests in GitHub Actions

## 6. Zusammenfassung
Die Systemtests sind inhaltlich aus dem SRS abgeleitet. Zusätzlich wurden vorbereitete, automatisierte Integrationstests erstellt, die aktuell mit Mocking arbeiten. Damit ist eine belastbare Testbasis vorhanden, obwohl das Backend noch nicht vollständig fertiggestellt ist.

Aktueller Ausführungsstand der vorbereiteten Integrationstests (Vitest): **3 Testdateien, 12/12 Tests bestanden**.

Nächste sinnvolle Schritte:
1. Backend-Endpunkte auf die in den Tests beschriebenen Abfragen abgleichen.
2. Mock-Responses schrittweise durch echte Antworten ersetzen.
3. Testergebnisse nach Ausführung in diesem STR dokumentieren.

**Gesamtstatus:** Mock-basierte Integrationstests erstellt und erfolgreich ausgeführt; echte Backend-/BaSyx-Integration noch ausstehend.
