# DPP API Call-mappings

Dieses Dokument dient der Nachvollziehbarkeit der internen DPP API Abläufe, insbesondere den genutzten internen API Endpunkten der AAS Environment API, wie die Eingabeparameter auf bestehende Parameter der AAS Environment API gemapped werden und wie diese Rückgaben intern auf die von der DIN18222-Norm geforderten Rückgabeparameter gemapped werden.

<br>

| Version | Datum      | Autoren                    |
|---------|------------|----------------------------|
| 1.0     | 2025-11-08 | Luca Schmoll & Noah Becker |

---

## DPP Life Cycle API

*Hauptmethoden zur Verwaltung des DPP-Lebenszyklus*  
<br>

### `POST` /dpps

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

### `GET` /dppsByProductIdAndDate/{productId}

```mermaid
%%Mit Rentschler besprechen
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API

  User->>Web: Wants to get a DPP with a specific productId and timeStamp.
  Web->>API: GET /dppsByProductId/{productId}
  Note right of API: dppId = productId + /DPP/
  API->>API: GET /dpps/{dppId}
  rect red
    Note right of API: Search for correct TimeStamp (No future Version & only 1 !!)
  end

  API-->>Web: return DPP Json
  Web-->>User: Display all relevant dpp data
```

<br>

### `GET` /dppsByProductIds/{prodcutId}?limit&cursor

> DIN 18222 hier unschlüssig: Siehe Seite 19 in PDF Tabelle HTTP-Methode für Methode "ReadDPPIdsByProductIds" ist "POST", während Seite 11 sagt "gibt eine Liste [...] zurück"

> Nochmal Blick in die Norm werfen – insbesondere für die Umsetzung der Parameter *limit* und *cursor*

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Enviroment API

  User->>Web: Wants to get a list of dppIds by productIds
  Web->>API: POST /dppsByProductIds
  Note right of API: dppId = productId + /DPP/ + (neueste) Version base64-encoded
  
  loop for all productIds
    API->>Env: GET /dpps/{dppId}
    Env->>API: return DPPs (JSON)
  end

  API->>Web: Collection of DPPs (JSON)
  Web->>User: return visuals
```

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
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  User->>Web: Wants a specific Submodel in a DPP with dppId and elementId
  Web->>API: GET /dpps/{dppId}/collections/{elementId}

  rect purple
    API->>API: /dpp/{dppId}
    Note right of API: elementId = submodelIdentifier
  end

  loop for every dpp version
    API-->>API: Check if elementId/submodel exists in DPP submodel list
    API->>Env: GET /submodels/{submodelIdentifiers}/submodel-elements
    alt success
      Env-->>API: return HTTP 204: All DPP-relevant submodel data (JSON)
    else failed
      Env-->>API: return HTTP Errorcode
    end
  end

  Note right of Web: Structure: "Version > Data | Version > Data"
  API-->>Web: return Submodel JSON (Version + Data)
  Web-->>User: Display all relevant data
```

<br>

### `PATCH` /dpps/{dppId}/collections/{elementId}

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  User->>Web: Wants to update a specific Submodel in a DPP with dppId and elementId
  Web->>API: PATCH /dpps/{dppId}/collections/{elementId}

  rect purple
    API->>API: /dpp/{dppId}
    Note right of API: elementId = submodelIdentifier
  end

  loop for whole dpp list
    API-->>API: Check if elementId/submodel exists in DPP submodel list
    opt
      API-->>Web: return error
    end

    Note right of API: submodelIdentifier has to be base64-encoded
    API->>Env: PATCH /submodels/{submodelIdentifier}/$values
    Env-->>API: Returns updated Submodel 
  end

  API-->>Web: return updated Submodel
  Web-->>User: Display all relevant data
```

<br>

### `GET` /dpps/{dppId}/elements/{elementPath}

```mermaid
sequenceDiagram
  actor User
  participant Web as AAS Web UI (DPP Viewer)
  participant API as DPP-API
  participant Env as AAS Environment API

  User->>Web: Wants a specific Value of a Submodel in a DPP with dppId and elementPath
  Web->>API: GET /dpps/{dppId}/elements/{elementPath}

  rect purple
    API-->>API: /dpp/{dppId}
  end

  loop for every dpp version
    loop for every submodel of a version
        API->>Env: GET /submodels/{submodelIdentifier}/submodel-elements/{elementPath}
        alt
            Env-->>API: Error
        else
            Env-->>API: HTTP 204: Value of Element
        end
    end
  end

  API-->>Web: return Submodel JSON (Version + Data)
  Web-->>User: Display all relevant data
```

<br>

### `PATCH` /dpps/{dppId}/elements/{elementPath}
