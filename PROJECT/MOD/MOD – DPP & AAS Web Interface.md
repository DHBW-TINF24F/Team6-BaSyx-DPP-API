# Moduldokumentation – DPP & AAS Web Interface

## 1. Übersicht

Das Modul **DPP & AAS Web Interface** stellt eine webbasierte Benutzeroberfläche zur Verfügung, über die digitale Produktpässe (DPP) und Asset Administration Shells (AAS) angezeigt, durchsucht und im Detail betrachtet werden können. Die Implementierung basiert auf **Vue 3** mit der Composition API (`<script setup>`), **TypeScript** und **Vue Router**.

---

## 2. Architektur

### 2.1 Komponentenstruktur

```
src/
├── pages/
│   ├── AASList.vue            # Listenansicht aller AAS-Einträge
│   └── DPPDetailPage.vue      # Detailansicht eines digitalen Produktpasses
├── router/
│   └── index.ts               # Zentrale Router-Konfiguration
├── stores/
│   └── aasStore.ts            # Pinia-Store für AAS-Zustandsverwaltung
└── components/
    └── ValueTree.vue          # Rekursive Render-Komponente für Submodel-Daten
```

### 2.2 Technologie-Stack

- **Framework:** Vue 3 (Composition API, `<script setup>`)
- **Sprache:** TypeScript
- **Routing:** Vue Router 4
- **State Management:** Pinia (`useAASStore`)
- **HTTP-Client:** Fetch API (native)
- **Styling:** Scoped CSS mit Dark-Theme-Variablen
- **Encoding:** URL-sichere Base64-Kodierung über `EncodeDecodeUtils`

---

## 3. Seitenkomponenten

### 3.1 DPPListView.vue – AAS-Listenansicht

**Zweck:** Anzeige aller verfügbaren AAS-Einträge mit Suchfunktion und Navigation zur Detailansicht.

**Funktionalität:**

- Abruf aller AAS-Einträge vom Backend-Endpunkt
- Volltextsuche über AAS-Bezeichner und Beschreibungen
- Navigation zur DPP-Detailseite bei Auswahl eines Eintrags
- Responsive Darstellung (Desktop und Mobil)

**API-Anbindung:**

```
 GET /shells
```

**Navigationslogik:**

```typescript
function navigateToDetail(aasId: string) {
  const encodedEndpoint = base64Encode(aasId)
  router.push({
    name: 'DPPDetailPage',
    query: { aas: encodedEndpoint }
  })
}
```

**Darstellung:**

- Listenelemente mit AAS-ID, Beschreibung und Asset-Typ
- Suchfeld im oberen Bereich
- Dark-Theme-konforme Farbgebung

---

### 3.2 DPPDetailPage.vue – DPP-Detailansicht

**Zweck:** Vollständige Darstellung eines digitalen Produktpasses inklusive aller Submodel-Elemente.

**Funktionalität:**

- Dekodierung des referenzierten AAS- oder Produkt-Identifiers aus der Navigation
- Abruf der DPP-Versionen über den DPP-Backend-Endpunkt
- Laden und Rendern aller zugehörigen Submodel-Daten
- Rekursive Darstellung verschachtelter Wertstrukturen (ValueTree)
- Fehlerbehandlung bei nicht erreichbaren Endpunkten

**API-Anbindung:**

```
POST /dppsByProductIds
GET /dpps/{dppId}
GET /dppsByProductId/{productId}
```

**Zustandsverwaltung:**

```typescript
interface DPPState {
  loading: boolean
  error: string | null
  aasEndpoint: string
  submodels: Submodel[]
  selectedSubmodel: Submodel | null
}
```

**Typdefinitionen:**

```typescript
interface Submodel {
  id: string
  idShort: string
  semanticId?: SemanticId
  submodelElements: SubmodelElement[]
}

interface SubmodelElement {
  idShort: string
  modelType: string
  value?: string | number | boolean
  submodelElements?: SubmodelElement[]
}

interface SemanticId {
  type: string
  keys: Array<{ type: string; value: string }>
}
```

---

### 3.3 ValueTree – Rekursive Render-Komponente

**Zweck:** Darstellung hierarchisch verschachtelter Submodel-Elemente als aufklappbare Baumstruktur.

**Funktionalität:**

- Rekursives Rendering beliebig tief verschachtelter Strukturen
- Unterscheidung nach `modelType` (Property, SubmodelElementCollection, MultiLanguageProperty, etc.)
- Auf-/Zuklappen einzelner Knoten
- Formatierung von Werten je nach Datentyp

**Unterstützte modelTypes:**

- `Property` – Einfacher Schlüssel-Wert-Eintrag
- `SubmodelElementCollection` – Container mit Kindelementen
- `MultiLanguageProperty` – Mehrsprachige Textfelder
- `Range` – Wertebereich mit Min/Max
- `Blob` – Binärdaten-Referenz
- `File` – Datei-Referenz
- `ReferenceElement` – Verweis auf andere Elemente

---

## 4. Routing-Konfiguration

### 4.1 Routendefinition

```typescript
{
  path: '/dpp/list',
  name: 'DPPList',
  component: DPPListView,
  meta: { name: 'DPP List', subtitle: 'All Digital Product Passports' }
},
{
  path: '/dpp/detail/:productId?',
  name: 'DPPDetailPage',
  component: DPPDetailPage,
  meta: { name: 'DPP Detail', subtitle: 'Detail view for DPP' }
}
```

### 4.2 Query-Parameter

- **`aas`** – URL-sicher Base64-kodierter AAS- oder Produktbezug, der für die Navigation verwendet wird
- Die zentrale Kodierung/Dekodierung erfolgt über `EncodeDecodeUtils`

### 4.3 Router-Funktionen

- **Lazy Loading:** Teile des Routers werden dynamisch importiert; die DPP-Seiten werden direkt aus dem Router geladen
- **Navigation Guards:** Authentifizierungsprüfung über `meta.requiresAuth`
- **OAuth2-Callback:** Separater Route-Handler für Token-Austausch
- **Modul-Routen:** Dynamische Generierung aus Modul-Manifesten

---

## 5. Backend-Anbindung

### 5.1 API-Endpunkte

**Basis-URL:** `https://srv01.noah-becker.de/uni/swe/api/dpp`

| Endpunkt | Methode | Beschreibung |
|----------|---------|--------------|

> **/dppsByProductIds** – POST – Liefert alle DPP-Versionen zu einer Liste von Product-IDs

> **/dpps/{dppId}** – GET – Liefert ein vollständiges DPP inklusive Submodel-Werten

> **/dppsByProductId/{productId}** – GET – Liefert die neueste DPP-Version zu einer Product-ID

### 5.2 Fehlerbehandlung

- HTTP-Statuscodes werden ausgewertet (4xx, 5xx)
- Netzwerkfehler werden abgefangen und als Benutzerhinweis angezeigt
- Timeout-Handling für langsame Backend-Antworten
- CORS-Header müssen serverseitig korrekt gesetzt sein

### 5.3 Authentifizierung

- OAuth2-basierte Autorisierung
- Token wird im Authorization-Header mitgesendet
- Callback-Route für Token-Austausch nach Login-Redirect

---

## 6. State Management

### 6.1 useAASStore (Pinia)

Der zentrale Store verwaltet den Zustand der ausgewählten AAS und stellt diesen komponentenübergreifend bereit.

**State-Eigenschaften:**

```typescript
interface AASStoreState {
  selectedAAS: AAS | null
  aasList: AAS[]
  loading: boolean
  error: string | null
}
```

**Actions:**

- `fetchAASList()` – Lädt alle AAS-Einträge
- `selectAAS(aas: AAS)` – Setzt die aktive AAS
- `clearSelection()` – Setzt die Auswahl zurück

**Getters:**

- `hasSelection` – Gibt an, ob eine AAS ausgewählt ist
- `filteredList(query: string)` – Gefilterte AAS-Liste

---

## 7. Benutzeroberfläche

### 7.1 Design-Prinzipien

- **Dark Theme:** Dunkle Hintergrundfarben mit heller Typografie
- **Responsive Layout:** Anpassung an Desktop- und Mobilgeräte
- **Konsistente Abstände:** Einheitliches Spacing-System
- **Scoped Styles:** Komponentenisolierte CSS-Regeln

### 7.2 AAS-Liste (Bildschirmansicht)

- Kopfbereich mit Suchfeld
- Scrollbare Liste mit Einträgen
- Jeder Eintrag zeigt: AAS-ID (gekürzt), Beschreibungstext, Asset-Typ-Badge
- Hover-Effekt und Auswahlmarkierung
- Ladeindikator während des Datenabrufs

### 7.3 DPP-Detailansicht (Bildschirmansicht)

- Kopfbereich mit AAS-Bezeichner und Zurück-Navigation
- Seitenleiste mit Submodel-Auswahl
- Hauptbereich mit ValueTree-Darstellung
- Aufklappbare Baumknoten für verschachtelte Strukturen
- Farbkodierung nach Element-Typ
- Kopierfunktion für einzelne Werte

---

## 8. Encoding-Strategie

### 8.1 Base64-Kodierung der AAS-Endpunkte

Da AAS-Identifikatoren häufig URLs oder URNs enthalten, die als Query-Parameter problematisch sind, wird eine URL-sichere Base64-Kodierung verwendet:

```typescript
// Kodierung (bei Navigation)
const encoded = base64Encode(aasEndpointUrl)

// Dekodierung (in der Zielkomponente)
const decoded = base64Decode(route.query.aas as string)
```

**Vorteile:**

- URL-sichere Übertragung beliebiger Identifikatoren
- Keine Konflikte mit Sonderzeichen in Query-Parametern
- Einfache Umkehrbarkeit ohne Informationsverlust
- Unicode-Zeichen werden über `encodeURIComponent`/`decodeURIComponent` korrekt behandelt

**Einschränkungen:**

- Erhöhte Zeichenlänge (ca. 33 % Overhead)

---

## 9. Modulintegration

### 9.1 Registrierung im Router

Das Modul wird über die Funktion `createAppRouter` registriert. Die Routen können statisch definiert oder dynamisch aus einem Modul-Manifest generiert werden:

```typescript
// Statische Registrierung
const routes = [
  ...coreRoutes,
  ...dppModuleRoutes
]

// Dynamische Registrierung
function registerModuleRoutes(router: Router, manifest: ModuleManifest) {
  manifest.routes.forEach(route => {
    router.addRoute(route)
  })
}
```

### 9.2 Modul-Manifest (optional)

```typescript
export const dppModuleManifest: ModuleManifest = {
  id: 'dpp-aas-interface',
  name: 'DPP & AAS Web Interface',
  version: '1.0.0',
  routes: [
    { path: '/dpp', name: 'DPPList', component: AASList },
    { path: '/dpp/detail', name: 'DPPDetailPage', component: DPPDetailPage }
  ],
  navigation: [
    { label: 'Digitale Produktpässe', icon: 'mdi-file-document', route: '/dpp' }
  ]
}
```

---

## 10. Sicherheitsaspekte

- **Authentifizierung:** Alle DPP/AAS-Routen erfordern eine gültige Sitzung (`meta.requiresAuth`)
- **CORS:** Backend muss Anfragen von der Frontend-Domain erlauben
- **Input-Validierung:** Base64-Dekodierung wird in Try-Catch-Blöcke eingebettet
- **XSS-Prävention:** Vue 3 escaped Inhalte automatisch im Template
- **Token-Handling:** Access-Tokens werden nicht im LocalStorage persistiert (empfohlen: HttpOnly-Cookie oder Memory)

---

## 11. Abhängigkeiten

**Laufzeit-Abhängigkeiten:**

- `vue` ^3.3
- `vue-router` ^4.2
- `pinia` ^2.1
- `typescript` ^5.0

**Entwicklungs-Abhängigkeiten:**

- `vite` ^5.0
- `@vitejs/plugin-vue` ^4.0
- `eslint` + Vue/TypeScript-Regeln

---

## 12. Bekannte Einschränkungen

- Die DPP-Backend-URL ist im Frontend aktuell hartcodiert und sollte über Umgebungsvariablen konfigurierbar sein.
- Die AAS-Repository-URL ist abhängig von der Umgebungskonfiguration.
- Offline-Funktionalität ist nicht implementiert; eine Netzwerkverbindung zum Backend ist zwingend erforderlich.
- Die rekursive ValueTree-Komponente hat bei sehr tiefen Verschachtelungen (>20 Ebenen) potenzielle Performance-Einbußen.

---

## 13. Weiterentwicklung

**Geplante Erweiterungen:**

- Export des DPP als PDF oder JSON-LD
- Vergleichsansicht für mehrere Produktpässe
- Editiermodus für berechtigte Benutzer
- Caching-Strategie für häufig abgerufene Submodels
- WebSocket-basierte Echtzeit-Aktualisierung bei Datenänderungen
- Internationalisierung (i18n) der Benutzeroberfläche

---

## 14. Zusammenfassung

Das DPP & AAS Web Interface Modul bietet eine vollständige, typsichere und erweiterbare Lösung zur Visualisierung digitaler Produktpässe auf Basis der Asset Administration Shell. Durch die modulare Architektur mit Vue 3, TypeScript und Pinia ist eine einfache Integration in bestehende Anwendungslandschaften sowie eine unabhängige Weiterentwicklung einzelner Teilkomponenten gewährleistet.