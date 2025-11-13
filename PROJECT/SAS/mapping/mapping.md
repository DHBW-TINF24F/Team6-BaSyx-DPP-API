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

> DIN Dokument hier ungenau: Tabelle 5 &mdash; Create DPP gibt als Ausgabeparameter `dppId` an, während Tabelle 17 &mdash; Lebensweg-API als Ausgabe `DPP`(-Objekt) fordert.

```mermaid
sequenceDiagram
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  Web->>API: POST /dpps
  Note right of API: idShort: idShort of AAS Shell
  API->>Env: GET /submodels?idShort
  Env-->>API: All Submodel in the AAS Shell

  rect purple
    Note right of API: DPP Metadata Submodel
    Note left of Env: ID: AAS ID + /dpp/
    API->>Env: POST /submodels
    Env-->>API: return submodelIdentifier
    Note left of Env: Creating version based on current time + date (as SubmodelElementCollection)
  end

  loop je DPP-relevantes Submodel*
    API->>API: Duplicate check (with idShorts)

    rect yellow
        opt Duplicate
            API->>Env: ?
        end
    end

    API->>Env: POST /submodels
    Env-->>API: return submodelIdentifier
    API->>Env: POST /submodels/{submodelIdentifier}/submodel-elements
    Env-->>API: return submodel JSON

    Note right of API: aasIdentifier: [base64 encoded] AAS idShort
    API->>Env: POST /shells/{aasIdentifier}/submodel-refs
    Env->>API: return added Submodel reference (JSON)
  end

  API->>API: Store submodelIdentifier of the relevant submodels in {DPP Submodel + Version}

  API-->>Web: return JSON
```

<br>

**Erläuterung**  

| **Actor**      | **API-Call** | **Recipient** | **Parameters** | **Body expected?** | **Return element** | **Note** |
|----------------|--------------|---------------|----------------|--------------------|--------------------|----------|
| **AAS Web UI** | `POST` /dpps | DPP-API | - | *Yes* <br> dppId <br> productId^*^ | JSON | Initial Frontend call <br> dppId can be empty <br> productId has to be idShort of AAS Shell <br> *Both base64-encoded!* <br> **Return: JSON** |
| **DPP-API**    | `GET` /submodels | Environment API | idShort | No | JSON | idShort is productId <br> **Return: Array of submodels** |
|                | `POST` /submodels | Environment API | 

<br>

### `GET` /dpps/{dppId}

[KI example](https://mermaid.live/edit#pako:eNqVVclu2zAQ_ZUBTwmgeJO8RGhTZLELX1KjLnJoHAS0SMesJVElKceu4X_vaLPltakOAjV8b97McIZaEU8yTlyi-e-Yhx5_EPRN0WAUAj4RVUZ4IqKhgXtf8NAc2m8HfaAaelSbZNkNWSTFMWA3TKRUAr6jmrecwnIIfZDeLEPmqyFXc-HxDJm9s3iubm5Q1YWv3R9QZVGkv-Crzz4vFosMhruIyZXcXBJSUAbIt64KTzz9Zq8lSNlTFlAuqONxgGBfV1c7tHXGyLAbz9-5jmSoObwLM4WC2_V5gIno55ey2KM0HOQcU0-pA6qQpwoH2qjYM7Hin8aqenNh1BIYNbSy79OCQ4vEuipFl5dlOV_KCHq4xak3heEu6V76PveMkGEGPhJfd2EU9fDgBoMnrjRCwYiAa0ODaEuivoEn6gu23YSJjEO2hZzMvT_8Bp1WrQ5VmFDfH1NvhlwVUHOW3BMhS6IqUtLgHclmW4ji2S1IUUXoP4AId_zt0k53XMnFIeVUDxakV8EOSaeU_9WhJafrzf4VzxvkIzrJOfa4SQvjeVzrSewfoo5MQFG1tFuPM_bO75YxWAlmpYw1GLkp5GZgyg-aeR7ahAqfsw-F1VVKqv8LhyeUD8Sz39sHhrPdexcLP21fmOdTJce_sHnddO5XudHajpNVDme99Z3W5VHCfHf6zooPZyICMxU6CaDkqshgszh6Yw2lMtgofkLWMF6WRv4i5O-4hIlQ2lzuX7F4KNnV7sIqOXQXniuVyouFgxsnxnBNLPKmBCMu3oLcIgHHWyD5JKvEy4iYKbbyiLi4ZFTNRmSUcvDP8lPKoKApGb9NiYuXicavOEKt4ue3gWCKXN0nusTttBqpD-KuyIK4jbZdaTfqdqdds22n7tRsiyyJ67Qrtt1qNq_bDcdp1R1nbZE_qWq9YjdqzXan6didVvu6XnfWfwEHPUAO)

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  User->>Web: Search DPP by ID: {dppId}
  Web->>API: GET /dpps/{dppId}

  rect purple
    Note right of API: submodelIdentifier = [base64 encoded] dppId
    API->>Env: /submodels/{submodelIdentifier}
    alt success
        Env-->>API: return whole submodel tree
    else failed
        Env-->>API: return HTTP Errorcode
    end
  end

  API->>API: Re-model Submodel tree sorted by version and their corresponding DPPSubmodels (only "value" important)

  loop
  Note right of API: submodelIdentifier = [base64 encoded] Parameter "value"
    API->>Env: GET /submodels/{submodelIdentifier}/submodel-elements
    alt success
      Env-->>API: return HTTP 204: All DPP-relevant submodel data (JSON)
    else failed
      Env-->>API: return HTTP Errorcode
    end
  end

  API-->>Web: return Submodel JSON
  Web-->>User: Display all relevant data
```

<br>

### `PATCH` /dpps/{dppId}

> Zu klären: Soll hier der timestamp geändert werden?

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

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  User->>Web: Wants to delete a DPP by ID
  Web->>API: PATCH /dpps/{dppId}


  rect purple
    API->>API: GET /dpp/{dppId}
    alt success
        API-->>API: return HTTP 204: All DPP versions and their relevant Submodels (JSON)
    else failed
        API-->>API: return HTTP Errorcode
    end
  end

  loop for every (DPP-)submodel
    API->>Env: DELETE /submodels/{submodelIdentifier}
    
    alt success
        Env-->>API: return HTTP 204
    else failed
        Env-->>API: return HTTP Errorcode
    end
  end

  API->>Env: DELETE /submodels/dppId/
  alt success
    Env-->>API: return HTTP 204
  else failed
    Env-->>API: return HTTP Errorcode
  end
  
  API-->>Web: return Status
  Web-->>User: Displays Status

```


<br>

### `GET` /dppsByProductId/{productId}

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API

  User->>Web: Wants to get a DPP with a specific productId.
  Web->>API: GET /dppsByProductId/{productId}
  Note right of API: Per definition: dppId = productId + /DPP/
  loop for each productId
    Note right of API: dppId: Add "/DPP/" to productId
    API->>API: GET /dpps/{dppId}
    Note left of API: Choose the newest version ([0]) of DPP

    alt success
        API-->>API: add DPP to return array
    else failed
        API-->>API: do not add DPP to return array
    end
  end
  
  API-->>Web: return DPP JSON
  Web-->>User: Display all relevant DPP data
```

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

  API-->>Web: return DPP Json
  Web-->>User: Display all relevant dpp data
```

<br>

| **Input-Parameter** | **Description** | **Format** | **Note** |
|---------------------|-----------------|------------|----------|
| **productId**       | Product-ID      | *base64-encoded* | submodelIdentifier of (Product) AAS <br> |
| **timestamp**       | Timestamp       | YYYY-MM-DD*T*HH-MM-SS*Z* | - |

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /dpp/{dppId}** | dppId | [See here](#api-calls) | dppId = *productId* + *"DPP"* <br> productId is *base64*-encoded and needs to be decoded in order to generate submodelIdentifier for DPP |
| **GET /dppsByProductIdAndDate/{productId}?date={timestamp}** | productId <br> timestamp | ![](./src/RETURN_GET-ddpsByProductIds_2.png) | Only return the to the given timestamp newest DPP, no older or newer DPPs(!) |

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
| **Request body**<br>*productIds<br>limit<br>cursor* | <br> Product-ID <br> . <br> . | <br> *base64-encoded* <br> ? <br> ? | <br> submodelIdentifier of (Product) AAS <br> ? <br> ? |

| **API-Call** | **Parameter** | **Return** | **Note** |
|--------------|---------------|------------|----------|
| **GET /submodels/{submodelIdentifier}/$value** | submodelIdentifier | [See here](#api-calls) | submodelIdentifier is put together by the given *productId* and *"/DPP"* |
| **GET /dppsByProductIds** | Request body:<br>*productIds<br>limit<br>cursor* | ![](./src/RETURN_GET-ddpsByProductIds_2.png) | productId is *base64*-encoded and needs to be decoded in order to generate submodelIdentifier for DPP |

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
    API-->>API: GET /dpps/{dppId}
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
