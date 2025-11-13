# DPP API Call-mappings

Dieses Dokument dient der Nachvollziehbarkeit der internen DPP API Abläufe, insbesondere den genutzten internen API Endpunkten der AAS Environment API, wie die Eingabeparameter auf bestehende Parameter der AAS Environment API gemapped werden und wie diese Rückgaben intern auf die von der DIN18222-Norm geforderten Rückgabeparameter gemapped werden.

<br>

| Version | Datum      | Autoren                    | Bemerkung |
|---------|------------|----------------------------|-----------|
| 1.0     | 2025-11-08 | Luca Schmoll & Noah Becker | Inital thoughts & diagrams |
| 1.1     | 2025-11-09 | Luca Schmoll & Noah Becker | Refactor sequences & write down open questions |
| 1.2     | 2025-11-12 | Noah Becker                | Refactor Fine-Granular API Calls |

---

## DPP Life Cycle API

*Hauptmethoden zur Verwaltung des DPP-Lebenszyklus*  
<br>

### `POST` /dpps

> ***[noahbecker] 2025-11-10 21:06:*** DIN Dokument hier ungenau: Tabelle 5 &mdash; Create DPP gibt als Ausgabeparameter `dppId` an, während Tabelle 17 &mdash; Lebensweg-API als Ausgabe `DPP`(-Objekt) fordert.

```mermaid
sequenceDiagram
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  Web->>API: POST /dpps
  
  rect yellow
    Note right of API: submodelIdentifier: aasIdentifier + "/DPP"
    API->>Env: GET /submodels/{submodelIdentifier}/$value
    opt Not Found: No DPP
      Note right of API: No DPP Found: Create the DPP Submodel first
      Env-->>API: Return 404 Not Found
      API->>Env: POST /submodels
      Env-->>API: Return Success HTTP 200
    end
    Env-->>API: Return existing DPP Submodel
  end

  Note right of API: Create new DPP Version
  API->>Env: POST /submodels/{submodelIdentifier}/submodel-elements
  Env-->>API: Return Success HTTP 200

  loop for every given submodel
    API->>Env: POST /submodels
    Env-->>API: Return Success HTTP 200
    
    Note right of API: Add submodel reference to the DPP Submodel
    API->>Env: POST /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}
    Env-->>API: Return Success HTTP 200
  end

  API-->>Web: Return Success
```

<br>

| **Input-Parameter** | **Description** | **Format** | **Note** |
|---------------------|-----------------|------------|----------|
| **dppId**           | DPP-Identifier  | *base64-encoded* | [See here](#parameter) |
| **Request body**<br>aasIdentifier<br>dpp | <br>AAS-Identifier<br>Submodels | <br>*base64-encoded*<br>[See here](#parameter) | -

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /submodels/{submodelIdentifier}/$value** | submodelIdentifier | [See here](#api-calls) | submodelIdentifier must be build from the given *aasIdentifier* + *"/DPP"* |
| **POST /submodels** | [See here]() | - | 1. Create DPP Submodel <br> 2. Create Submodels |
| **POST /submodels/{submodelIdentifier}/submodel-elements** | submodelIdentifier <br>*Request body* | - | Parameter *submodelIdentifier* is submodelIdentifier of DPP Submodel |
| **POST /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}** | submodelIdentifier <br> idShortPath | - | Parameter *submodelIdentifier* is submodelIdentifier of DPP Submodel <br> idShortPath is build from scheme: "DPPSubmodels.DPPSubmodel_[shortId of Submodel]" |
| | | | |
| **POST /dpps** | - | ***Image soon*** | - |

<br>

### `GET` /dpps/{dppId}

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  User->>Web: Want to retrieve a specific DPP by ID
  Web->>API: GET /dpps/{dppId}

  API-->>API: strip out submodelIdentifier and idShort of DPP version out of dppId

  Note right of API: Use submodelIdentifier from stripped dppId & build idShortPath with requested [dppVersion].DPPSubmodels 
  API->>Env: GET /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/$value
  Env-->>API: Return submodelIdentifiers - references to DPP-related submodels

  loop for every submodel
    API->>Env: GET /submodels/{submodelIdentifier}/$value
    Env-->>API: Return submodel
    API-->>API: Add to return array
  end

  API-->>API: map to return scheme

  API-->>Web: Return Submodel JSON
  Web-->>User: Return data
```

<br>

| **Input-Parameter** | **Description** | **Format** | **Note** |
|---------------------|-----------------|------------|----------|
| **dppId**           | DPP-Identifier  | *base64-encoded* | [See here](#parameter) |

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/$value** | submodelIdentifier<br>idShortPath | - | submodelIdentifier and idShortPath need to be stripped out of given dppId; <br> idShortPath has to end with ".DPPSubmodels" |
| **GET /submodels/{submodelIdentifier}/$value** | submodelIdentifier | - | Returns submodel data |
| | | | |
| **GET /dpps/{dppId}** | dppId | ***image soon*** | - |

<br>

### `PATCH` /dpps/{dppId}

> ***[noahbecker] 2025-11-11:*** Zu klären: Soll hier der timestamp geändert werden?
> ***[noahbecker] 2025-11-13:*** Soll hier das DPP Submodel verändert werden (sprich: Änderungen an den Referenzierungen zu den Submodels) oder konkret die in den DPP verwendeten Submodels verändert werden?

> **To be included:** "Wenn die Aktualisierung einiger Teile scheitert, scheitert der vollständige Aktualisierungsprozess
und es sollten keine Änderungen im DPP übernommen werden."

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  User->>Web: Want to update a submodel within the DPP by ID
  Web->>API: PATCH /dpps/{dppId}

  rect purple
    API->>API: GET /dpps/{dppId}
    alt success
        API-->>API: return HTTP 204: All DPP versions and their relevant Submodels (JSON)
    else failed
        API-->>API: return HTTP Errorcode
    end
  end

  loop for n submodels
    API->>API: Fetch Submodel Identifier from Request body
    API->>Env: PUT /submodels/{submodelIdentifier} mit Request body
    
    alt success
        Env-->>API: return HTTP 204
    else failed
        Env-->>API: return HTTP Errorcode
    end
  end
  
  API-->>Web: return JSON
  Web-->>User: Displays Status
```

<br>

### `DELETE` /dpps/{dppId}

> ***[noahbecker] 2025-11-13 11:12:*** Will man hier komplettes DPP inkl. der referenzierten Submodels (Nameplate, Carbon Footprint,...) löschen **oder** nur den DPP an sich?
>> Diagramm zeigt Variante, nur den DPP an sich zu löschen, referenzierte Submodels existieren weiterhin

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  User->>Web: Wants to delete a DPP by ID
  Web->>API: DELETE /dpps/{dppId}

  API-->>API: Strip submodelIdentifier and DPP-version from given dppId

  Note right of API: idShortPath = "DPP_" + DPP-version
  API->>Env: DELETE /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}
  alt success
    Env-->>API: return HTTP 204
  else failed
    Env-->>API: return HTTP Errorcode
  end
  
  API-->>Web: Return StatusCode
  Web-->>User: Return data
```

<br>

| **Input-Parameter** | **Description** | **Format** | **Note** |
|---------------------|-----------------|------------|----------|
| **dppId**           | DPP-Identifier  | *base64-encoded* | [See here](#parameter) |
| **submodelIdentifier** | Submodel-Identifier | *base64-encoded* | submodelIdentifier is stripped out of given dppId |
| **idShortPath**     | Element-ID-Path | "DPP_" + timestamp | Stripped out of given dppId |

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **DELETE /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}** | submodelIdentifier<br>idShortPath | - | submodelIdentifier and idShortPath need to be stripped out of given dppId |
| | | | |
| **DELETE /dpps/{dppId}** | dppId | ![](./src/RETURN_DELETE-dpps.png) | Only return the to the given timestamp newest DPP, no older or newer DPPs(!) |

<br>

### `GET` /dppsByProductId/{productId}

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API

  User->>Web: Wants to get a DPP by a specific productId.
  Web->>API: GET /dppsByProductId/{productId}
  Note right of API: Per definition: dppId = productId + /DPP/

  API->>API: GET /dpps/{dppId}

  rect red
    Note right of API: Choose the newest version ([0]) of returned DPPs
  end

  API-->>API: map to return scheme
  
  API-->>Web: Return DPP JSON
  Web-->>User: Return data
```

<br>

| **Input-Parameter** | **Description** | **Format** | **Note** |
|---------------------|-----------------|------------|----------|
| **productId**       | Product-Identifier  | *base64-encoded* | [See here](#parameter) |

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /dpps/{dppId}** | dppId | [See here](#api-calls) | dppId = *productId* + *"DPP"* <br> productId is *base64*-encoded and needs to be decoded in order to generate submodelIdentifier for DPP |
| | | | |
| **GET /dppsByProductId/{productId}** | productId | ![](./src/RETURN_GET-dppsByProductId.png) | Only return the newest DPP to the given product(Id) |

<br>

### `GET` /dppsByProductIdAndDate/{productId}?date={timestamp}

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API

  User->>Web: Wants to get a DPP with a specific productId and timeStamp.
  Web->>API: GET /dppsByProductId/{productId}?date={timestamp}
  Note right of API: dppId = productId + /DPP/
  API->>API: GET /dpps/{dppId}
  rect red
    Note right of API: Search for correct TimeStamp (No future Version & only 1 !!)
  end
  API-->>API: map to return scheme

  API-->>Web: return DPP JSON
  Web-->>User: Return data
```

<br>

| **Input-Parameter** | **Description** | **Format** | **Note** |
|---------------------|-----------------|------------|----------|
| **productId**       | Product-ID      | *base64-encoded* | [See here](#parameter) |
| **timestamp**       | Timestamp       | YYYY-MM-DD*T*HH-MM-SS*Z* | - |

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /dpp/{dppId}** | dppId | [See here](#api-calls) | dppId = *productId* + *"DPP"* <br> productId is *base64*-encoded and needs to be decoded in order to generate submodelIdentifier for DPP |
| | | | |
| **GET /dppsByProductIdAndDate/{productId}?date={timestamp}** | productId <br> timestamp | ![](./src/RETURN_GET-dppsByProductIdAndDate.png) | Only return the to the given timestamp newest DPP, no older or newer DPPs(!) |

<br>


### `POST` /dppsByProductIds

> Nochmal Blick in die Norm werfen – insbesondere für die Umsetzung der Parameter *limit* und *cursor*

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Enviroment API

  User->>Web: Wants to get a list of dppIds by productIds
  Web->>API: POST /dppsByProductIds

  API-->>API: fetch the request body for productIds, limit and cursor
  
  loop for all productIds
    Note right of API: submodelIdentifier = productId + /DPP
    
    API->>Env: GET /submodels/{submodelIdentifier}/$value
    Env-->>API: Return all DPP versions
    API-->>API: map to return scheme
  end

  API-->>Web: Collection of DPPs (JSON)
  Web-->>User: Return data
```

<br>

| **Input-Parameter** | **Description** | **Format** | **Note** |
|---------------------|-----------------|------------|----------|
| **Request body**<br>*productId<br>limit<br>cursor* | <br> Product-ID <br> . <br> . | <br> *base64-encoded* <br> ? <br> ? | <br> [See here](#parameter) <br> ? <br> ? |

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /submodels/{submodelIdentifier}/$value** | submodelIdentifier | [See here](#api-calls) | submodelIdentifier is put together by the given *productId* and *"/DPP"* |
| | | | |
| **GET /dppsByProductIds** | Request body:<br>*productId<br>limit<br>cursor* | ![](./src/RETURN_GET-ddpsByProductIds.png) | productId is *base64*-encoded and needs to be decoded in order to generate submodelIdentifier for DPP |

<br>

---

## DPP Registry API

### `POST` /registerDPP

<br>

---

## DPP Fine-Granular Life Cycle API

### `GET` /dpps/{dppId}/collections/{elementId}

```mermaid
sequenceDiagram
  participant Web as Service
  participant API as DPP-API
  participant Env as AAS Environment API

  Web->>API: GET /dpps/{dppId}/collections/{elementId}

  API->>API: GET /dpps/{dppId}

  opt FEHLER: Nicht gefunden
    API-->>Web: Return HTTP 404
  end

  loop für jedes Submodel im DPP
    API->>Env: GET /submodels/{submodelIdentifier}/$value
    Env-->>API: Return Submodel structure & values
  end

  API-->>Web: Return JSON DPP Scheme
```

<br>

### `PATCH` /dpps/{dppId}/collections/{elementId}

> **To be included:** "Wenn die Aktualisierung einiger Teile scheitert, scheitert der vollständige Aktualisierungsprozess
und es sollten keine Änderungen im DPP übernommen werden."

```mermaid
sequenceDiagram
  participant Web as Service
  participant API as DPP-API
  participant Env as AAS Environment API

  Web->>API: PATCH /dpps/{dppId}/collections/{elementId}

  loop für jede Änderung
    Note right of API: submodelIdentifier = elementId
    API-->>API: Baue idShortPath aus Attribut-Hierarchie zusammen
    API->>Env: PATCH /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/$value 
    Env-->>API: Return HTTP 200 Success
  end

  API-->>Web: Return JSON DPP Scheme
```

<br>

### `GET` /dpps/{dppId}/elements/{elementPath}

```mermaid
sequenceDiagram
  participant Web as Service
  participant API as DPP-API
  participant Env as AAS Environment API

  Web->>API: PATCH /dpps/{dppId}/elements/{elementPath}

  Note right of API: elementPath = idShortPath with (idShort[Submodel].idShort[SubmodelElementCollection]…)
  rect lightgreen
    API->>API: GET /dpps/{dppId}
    API-->>API: Get idShort[Submodel] in structure "DPPSubmodel_(idShort[Submodel])" and retrieve submodelIdentifier
  end

  API->>Env: GET /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/$value
  Env-->>API: return JSON

  API-->>Web: Return JSON requested element
```

<br>

| **Input-Parameter** | **Description** |**Format** | **Note** |
|---------------------|-----------------|-----------|----------|
| dppId               | [See here](#parameter) | - | - |
| elementPath         | ElementId path to the specific data element | *idShort[Submodel]*.*idShort[ChildrenElement]*.[...] | Hierachic structure - start from idShort of submodel to end with specific submodel entry - divided by dots |

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /dpps/{dppId}** | dppId | [See here](#api-calls) | - |
| | | | |
| **GET /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/$value** | submodelIdentifier <br> idShortPath | [See here](#api-calls) | Use submodelIdentifier from *GET /dpps/{dppId}*; <br> Retrieve idShortPath from input parameter *elementPath* - divided by dots, use path given from elementPath[1] |

<br>

### `PATCH` /dpps/{dppId}/elements/{elementPath}

> **tbd**

<br>

---

## Important parameter & API Calls

### Parameter

| **Parameter** | **Description** | **Format** | **Note** |
|---------------|-----------------|------------|----------|
| **dppId**     | DPP identifer   | *submodelIdentifier* + "/DPP/" + timestamp | Used to identify a specific (versioned) DPP |

<br>

### API Calls

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /ddps/{dppId}** | dppId | <code>{<br>&nbsp;&nbsp;"status": "Success",<br>&nbsp;&nbsp;"payload": { <br> &nbsp;&nbsp;&nbsp;&nbsp; "dppId": "[base64-encoded dppId]", <br> &nbsp;&nbsp;&nbsp;&nbsp; "timestamp": "YYYY-MM-DDTHH-MM-SSZ" <br> &nbsp;&nbsp;&nbsp;&nbsp; "dpps": [ <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; { <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; DPPVersion: "YYYY-MM-DDTHH-MM-SSZ", <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "DPPSubmodels": { <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "DPPSubmodel_[submodelshortId]": "[submodelIdentifier]", <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; "DPPSubmodel_[submodelshortId]": "[submodelIdentifier]" <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;} <br> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp; } <br> &nbsp;&nbsp;&nbsp;&nbsp;]<br> &nbsp;&nbsp;} <br>   }</code> | - |
