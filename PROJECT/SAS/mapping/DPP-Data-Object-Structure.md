# DPP &ndash; Data Object Structure

## Overview

<table width="100%">
<tr>
<td></td>
<th>ReadDPPById</th>
<th>ReadDPPByProductId</th>
<th>ReadDPPVersionByProductIdAndDate</th>
<th>ReadDPPIdsByProductIds</th>
</tr>

<tr>
<th>API endpoint</th>
<td><code>GET</code> /dpps/{dppId}</td>
<td><code>GET</code> /dppsByProductId/{productId}</td>
<td><code>GET</code> /ddpsByProductIdAndDate/{productId}</td>
<td><code>POST</code> /ddpsByProductIds</td>
</tr>

<tr>
<th>Schema*</th>
<td><a href="#with-result-object">Normal schema</a></td>
<td><a href="#with-result-object">Normal schema</a></td>
<td><a href="#with-result-object">Normal schema</a></td>
<td><a href="#with-result-object">Normal schema</a></td>
</tr>

<tr>
<th>Payload</th>
<td><a href="#dpp-object">DPP object</a></td>
<td><a href="#dpp-object">DPP object</a></td>
<td><a href="#dpp-object">DPP object</a></td>
<td><a href="#dpp-identifiers">DPP Identifiers</a></td>
</tr>

<tr>
<td colspan="5"></td>
</tr>

<td></td>
<th>CreateDPP</th>
<th>UpdateDPPById</th>
<th>DeleteDPPById</th>
<th>PostNewDPPToRegistry</th>
</tr>

<tr>
<th>API endpoint</th>
<td><code>POST</code> /dpps</td>
<td><code>PATCH</code> /dpps/{dppId}</td>
<td><code>DELETE</code> /dpps/{dppId}</td>
<td><code>POST</code> /registerDPP</td>
</tr>

<tr>
<th>Schema*</th>
<td><code>!</code> <a href="#only-dppid-schema">Only dppId schema</a> <code>!</code></td>
<td><a href="#with-result-object">Normal schema</a></td>
<td><code>!</code> <a href="#without-result-object">Only statusCode schema</a> <code>!</code></td>
<td><code>!</code> <a href="#registry-schema">Registry schema</a> <code>!</code></td>
</tr>

<tr>
<th>Payload</th>
<td>-</td>
<td><a href="#dpp-object">DPP object</a></td>
<td>-</td>
<td>-</td>
</tr>

<tr>
<td colspan="5"></td>
</tr>

<td></td>
<th>ReadDataElementCollection</th>
<th>ReadDataElement</th>
<th>UpdateDataElementCollection</th>
<th>UpdateDataElement</th>
</tr>

<tr>
<th>API endpoint</th>
<td><code>GET</code> /dpps/{dppId}/collections/{elementId}</td>
<td><code>PATCH</code> /dpps/{dppId}/collections/{elementId}</td>
<td><code>GET</code> /dpps/{dppId}/elements/{elementPath}</td>
<td><code>PATCH</code> /dpps/{dppId}/elements/{elementPath}</td>
</tr>

<tr>
<th>Schema*</th>
<td><a href="#with-result-object">Normal schema</a></td>
<td><a href="#with-result-object">Normal schema</a></td>
<td><a href="#with-result-object">Normal schema</a></td>
<td><a href="#with-result-object">Normal schema</a></td>
</tr>

<tr>
<th>Payload</th>
<td><a href="#dpp-dataelementcollection">DPP DataElementCollection</a></td>
<td><a href="#dpp-property">DPP Property</a></td>
<td><a href="#dpp-dataelementcollection">DPP DataElementCollection</a></td>
<td><a href="#dpp-property">DPP Property</a></td>
</tr>

</table>

<br>

\* *In case of an error, all endpoints will refer to the [General Error result object](#general-error-result-object)*

<br>

---

## `payload`-Object

> **Relevant Submodels:**
>> "[Digital]Nameplate"
>> "HandoverDocumentation"
>> "CarbonFootprint"
>> "TechnicalData"
>> "ProductCondition"
>> "MaterialComposition"
>> "Circularity"

<br>

### DPP object
*This object contains the main DPP information to the requested DPP entry.*

<details>
<summary><strong>v2</strong> &mdash; 2026-03-25 &nbsp;&nbsp; >>IN WORK!<<</summary>

> [!WARNING]
> Review is needed before proceeding to implement this. ~noah

```json
"payload": {
    "administration": {
        "creator": {
            "keys": [
                {
                "type": "GlobalReference",
                "value": "sebastian.eicke@harting.com"
                }
            ],
        },
        "assetInformation": {
            "assetKind": "Type",
            "defaultThumbnail": {
                "contentType": "image/png",
                "path": "b24b11da.png"
            },
            "globalAssetId": "https://pk.harting.com/?.20P=ZSN1"
        },
        "version": "1.0.1",
        "id": "https://dpp40.harting.com/shells/ZSN1",
        "description": [
            {
                "language": "en",
                "text": "HARTING Asset Administration Shell ZSN1"
            }
        ],
        "displayName": [
            {
                "language": "en",
                "text": "HARTING AAS ZSN1"
            },
            {
                "language": "de",
                "text": "HARTING AAS ZSN1 DE"
            }
        ],
        "idShort": "HARTING_AAS_ZSN1"
    },
    "submodels": [
        {
            "keys": [
                {
                    "type": "Submodel",
                    "value": "https://dpp40.harting.com/shells/ZSN1/submodels/Nameplate/3/0",
                    "reference": "https://admin-shell.io/zvei/nameplate/3/0/Nameplate", // from GET /submodels/{submodelIdentifier}/$metadata
                    "payload": { // from GET /submodels/{submodelIdentifier}/$value
                        "ProductArticleNumberOfManufacturer": [
                            {
                                "en": "09 30 024 0301, 09 33 024 2702, 09 33 000 6204"
                            }
                        ],
                        "ManufacturerName": [
                            {
                                "de": "HARTING Electric Stiftung & Co. KG"
                            }
                        ],
                        "OrderCodeOfManufacturer": [
                            {
                                "en": "09300240301, 09330242702, 09330006204"
                            }
                        ],
                        "CountryOfOrigin": "",
                        "YearOfConstruction": "",
                        "Markings": {
                            "Marking": {
                                "MarkingFile": {
                                    "contentType": "image/png",
                                    "value": "aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMS9zdWJtb2RlbHMvTmFtZXBsYXRlLzIvMA-Markings.Marking.MarkingFile-8abf5add.png"
                                },
                                "MarkingName": ""
                            }
                        },
                        "ManufacturerProductType": [
                            {
                                "de": "Han 24B Assembly ZSN1"
                            },
                            {
                                "en": "Han 24B Assembly ZSN1"
                            }
                        ],
                        "ContactInformation": {
                            "Company": [
                                {
                                    "de": "HARTING Electric Stiftung & Co. KG"
                                }
                            ],
                            "Phone": {
                                "TypeOfTelephone": "Office",
                                "TelephoneNumber": [
                                    {
                                        "de": "+49 5772 47-97100"
                                    }
                                ]
                            },
                            "NationalCode": [
                                {
                                    "de": "DE"
                                }
                            ],
                            "AddressOfAdditionalLink": "https://harting.com",
                            "Zipcode": [
                                {
                                    "de": "32339"
                                }
                            ],
                            "Street": [
                                {
                                    "de": "Wilhelm-Harting-Str. 1"
                                }
                            ],
                            "StateCounty": [
                                {
                                    "de": "Nordrhein-Westfalen"
                                }
                            ],
                            "CityTown": [
                                {
                                    "de": "Espelkamp"
                                }
                            ]
                        },
                        "ManufacturerProductFamily": [
                            {
                                "de": "Han® B"
                            },
                            {
                                "en": "Han® B"
                            }
                        ],
                        "CompanyLogo": {
                            "contentType": "image/png",
                            "value": "aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMS9zdWJtb2RlbHMvTmFtZXBsYXRlLzIvMA-CompanyLogo-ad7a78d6.png"
                        },
                        "ManufacturerProductRoot": [
                            {
                                "de": "Han®"
                            },
                            {
                                "en": "Han®"
                            }
                        ],
                        "URIOfTheProduct": "https://b2b.harting.com/ebusiness/de/hcpproductconfigurator?zConfID=ZSN1",
                        "ManufacturerProductDesignation": [
                            {
                                "de": "Han 24B Assembly ZSN1"
                            },
                            {
                                "en": "Han 24B Assembly ZSN1"
                            }
                        ]
                    }
                }
            ],
            "type": "ExternalReference"
        },
        {
            // ... for the previously mentioned relevant submodels
        }
    ]
}
```

</details>

<details>
<summary><strong>v1</strong> &mdash; 2026-03-22</summary>

```json
"payload": {
    "administration": {
        "creator": {
            "keys": [
                {
                "type": "GlobalReference",
                "value": "sebastian.eicke@harting.com"
                }
            ],
        },
        "assetInformation": {
            "assetKind": "Type",
            "defaultThumbnail": {
                "contentType": "image/png",
                "path": "b24b11da.png"
            },
            "globalAssetId": "https://pk.harting.com/?.20P=ZSN1"
        },
        "version": "1.0.1",
        "id": "https://dpp40.harting.com/shells/ZSN1",
        "description": [
            {
                "language": "en",
                "text": "HARTING Asset Administration Shell ZSN1"
            }
        ],
        "displayName": [
            {
                "language": "en",
                "text": "HARTING AAS ZSN1"
            },
            {
                "language": "de",
                "text": "HARTING AAS ZSN1 DE"
            }
        ],
        "idShort": "HARTING_AAS_ZSN1"
    },
    "submodels": [
        {
            "keys": [
                {
                    "type": "Submodel",
                    "value": "https://dpp40.harting.com/shells/ZSN1/submodels/Nameplate/3/0",
                    "reference": "https://admin-shell.io/zvei/nameplate/3/0/Nameplate", // from GET /submodels/{submodelIdentifier}/$metadata
                    "payload": { // from GET /submodels/{submodelIdentifier}/$value
                        "ProductArticleNumberOfManufacturer": [
                            {
                                "en": "09 30 024 0301, 09 33 024 2702, 09 33 000 6204"
                            }
                        ],
                        "ManufacturerName": [
                            {
                                "de": "HARTING Electric Stiftung & Co. KG"
                            }
                        ],
                        "OrderCodeOfManufacturer": [
                            {
                                "en": "09300240301, 09330242702, 09330006204"
                            }
                        ],
                        "CountryOfOrigin": "",
                        "YearOfConstruction": "",
                        "Markings": {
                            "Marking": {
                                "MarkingFile": {
                                    "contentType": "image/png",
                                    "value": "aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMS9zdWJtb2RlbHMvTmFtZXBsYXRlLzIvMA-Markings.Marking.MarkingFile-8abf5add.png"
                                },
                                "MarkingName": ""
                            }
                        },
                        "ManufacturerProductType": [
                            {
                                "de": "Han 24B Assembly ZSN1"
                            },
                            {
                                "en": "Han 24B Assembly ZSN1"
                            }
                        ],
                        "ContactInformation": {
                            "Company": [
                                {
                                    "de": "HARTING Electric Stiftung & Co. KG"
                                }
                            ],
                            "Phone": {
                                "TypeOfTelephone": "Office",
                                "TelephoneNumber": [
                                    {
                                        "de": "+49 5772 47-97100"
                                    }
                                ]
                            },
                            "NationalCode": [
                                {
                                    "de": "DE"
                                }
                            ],
                            "AddressOfAdditionalLink": "https://harting.com",
                            "Zipcode": [
                                {
                                    "de": "32339"
                                }
                            ],
                            "Street": [
                                {
                                    "de": "Wilhelm-Harting-Str. 1"
                                }
                            ],
                            "StateCounty": [
                                {
                                    "de": "Nordrhein-Westfalen"
                                }
                            ],
                            "CityTown": [
                                {
                                    "de": "Espelkamp"
                                }
                            ]
                        },
                        "ManufacturerProductFamily": [
                            {
                                "de": "Han® B"
                            },
                            {
                                "en": "Han® B"
                            }
                        ],
                        "CompanyLogo": {
                            "contentType": "image/png",
                            "value": "aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMS9zdWJtb2RlbHMvTmFtZXBsYXRlLzIvMA-CompanyLogo-ad7a78d6.png"
                        },
                        "ManufacturerProductRoot": [
                            {
                                "de": "Han®"
                            },
                            {
                                "en": "Han®"
                            }
                        ],
                        "URIOfTheProduct": "https://b2b.harting.com/ebusiness/de/hcpproductconfigurator?zConfID=ZSN1",
                        "ManufacturerProductDesignation": [
                            {
                                "de": "Han 24B Assembly ZSN1"
                            },
                            {
                                "en": "Han 24B Assembly ZSN1"
                            }
                        ]
                    }
                }
            ],
            "type": "ExternalReference"
        },
        {
            // ... for the previously mentioned relevant submodels
        }
    ]
}
```

</details>

<br>

### DPP Identifiers
Input parameters: `productId`
> [!NOTE]
> `productId` = `aasIdentifier`

<details>
<summary><strong>v2</strong> &mdash; 2026-03-25</summary>

> [!INFO]
> **V2:** Added productId as attribute at the top.

```json
"payload": {
    "productId": "<input parameter [not base64]>", // NEW
    "dpps": [
        {
            "dppId": "<string: dppId>"
            // To be declared if additional information like version or name is needed to be included
        },
        {
            "dppId": "<string: dppId>"
            // To be declared if additional information like version or name is needed to be included
        },
        // ...
    ]
}
```

</details>

<details>
<summary><strong>v1</strong> &mdash; 2026-03-22</summary>

```json
"payload": {
    "dpps": [
        {
            "dppId": "<string: dppId>"
            // To be declared if additional information like version or name is needed to be included
        },
        {
            "dppId": "<string: dppId>"
            // To be declared if additional information like version or name is needed to be included
        },
        // ...
    ]
}
```

</details>

<br>

### DPP DataElementCollection
Input parameters: `dppId` and `elementId`

> Get, for API call required parameter, `submodelIdentifier` from `dppId` via `GET /dpps/{dppId}`.payload.administration.id

> [!NOTE]
> `elementId` = `submodelIdentifier`

> [!WARNING]
> Only show requested DataElementCollection/Submodel if it exists in the requested DPP (previously executed check nessecary!)

<details>
<summary><strong>v1</strong> &mdash; 2026-03-22</summary>

```json
"payload": {
    // content from BaSyx AAS Environment Component API: GET /submodels/{submodelIdentifier}/$value

    // Example:
    "ProductArticleNumberOfManufacturer": "09 30 024 0301, 09 33 024 2702, 09 33 000 6204",
    "ManufacturerName": [
        {
            "de": "HARTING Electric Stiftung & Co. KG"
        }
    ],
    "AddressInformation": {
        "Company": [
            {
                "de": "HARTING Electric Stiftung & Co. KG"
            }
        ],
        "Phone": {
            "TypeOfTelephone": "Office",
            "TelephoneNumber": [
                {
                    "de": "+49 5772 47-97100"
                }
            ]
        },
        "NationalCode": [
            {
                "de": "DE"
            }
        ],
        "AddressOfAdditionalLink": "https://harting.com",
        "Zipcode": [
            {
                "de": "32339"
            }
        ],
        "Street": [
            {
                "de": "Wilhelm-Harting-Str. 1"
            }
        ],
        "StateCounty": [
            {
                "de": "Nordrhein-Westfalen"
            }
        ],
        "CityTown": [
            {
                "de": "Espelkamp"
            }
        ]
    },
    "OrderCodeOfManufacturer": "09300240301, 09330242702, 09330006204",
    "CountryOfOrigin": "",
    "Markings": [
        {
        "MarkingFile": {
            "contentType": "image/png",
            "value": "aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMS9zdWJtb2RlbHMvTmFtZXBsYXRlLzMvMA-Markings[0].MarkingFile-12670f46.png"
        },
        "MarkingName": "Communauté Européenne (CE)"
        }
    ],
    "ManufacturerProductType": "Han 24B Assembly ZSN1",
    "HardwareVersion": "",
    "FirmwareVersion": "",
    "ManufacturerProductFamily": [
        {
            "de": "Han® B"
        },
        {
            "en": "Han® B"
        }
    ],
    "CompanyLogo": {
        "contentType": "image/png",
        "value": "aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMS9zdWJtb2RlbHMvTmFtZXBsYXRlLzMvMA-CompanyLogo-a4d968da.png"
    },
    "SoftwareVersion": "",
    "ManufacturerProductRoot": [
        {
            "de": "Han®"
        },
        {
            "en": "Han®"
        }
    ],
    "SerialNumber": "",
    "URIOfTheProduct": "https://b2b.harting.com/ebusiness/de/hcpproductconfigurator?zConfID=ZSN1",
    "ManufacturerProductDesignation": [
        {
            "de": "Han 24B Assembly ZSN1"
        },
        {
            "en": "Han 24B Assembly ZSN1"
        }
    ],
    "DateOfManufacture": ""
}
```
</details>

<br>

### DPP Property

Input parameters: `dppId` and `elementPath`

<details>
<summary><strong>v1</strong> &mdash; 2026-03-025</summary>

> [!NOTE]
> **`elementPath`-structure:**
> `<DPP Submodel ID short: [Digital]Nameplate|HandoverDocumentation|CarbonFootprint|...>`**.**`<Rest of idShortPath beginning from submodel-sublevel: e.g. ProductCarbonFootprint.PCFCO2eq>`
>
> ![[./src/DPP-Property_structure.drawio.png]]

```json
"payload": {
    // content from BaSyx AAS Environment Component API: GET /submodels/{submodelIdentifier}/submodel-elements/{idShortPath}/$value
}
```

</details>

<br>

---

## General Success result object
### With result object
This result object only comes into place, when the action perfomed was succesful AND has A result object attached (see in [status codes](#status-codes))

```json
{
    "statusCode": <string: See under Status codes / 1>,
    "payload": [
        ...
    ]
}
```
The `payload`-Object in more detail can be found [here](#payload-object--dpp).

<br>

### Without result object
> **Reference:** Table 7 & 16 &mdash; DIN EN 18222

This result object only comes into place, when the action perfomed was succesful AND has NO result object attached (see in [status codes](#status-codes))

```json
{
    "statusCode": <string: See under Status codes / 1>
}
```

This occurence only happen with the API-Call `DeleteDPPById` / `DELETE /dpps/{dppId}`.

<br>

---

## Special result object

### Only dppId schema

```json
{
    "statusCode": <string: [Success: "SuccessCreated" / ...]>,
    "dppID": <string: identifier>
}
```

<br>

### Registry schema

```json
{
    "statusCode": <string: [Success: "SuccessCreated" / ...]>,
    "registryIdentifier": <string: identifier>
}
```

<br>

---

## General Error result object
> **Reference:** Table 13 &mdash; DIN EN 18222

This result object only comes into place, when an error occured AND has a result object (see in [status codes](#status-codes))!

```json
{
    "statusCode": <string: See under Status codes / 1>,
    "message": {
        "messageType": <string: ["Info"/"Warning"/"Error"/"Exception"] / 1>,
        "text": <string / 1>,
        "code": <int: Correlated error code (like 404,...) / 0..1>,
        "correlationId": <Identifier to relate several result messages throughout several systems / ShortIdType / 0..1>,
        "timestamp": <Timestamp of the message / dateTime / 0..1>
    }
}
```

<br>

---

## Status codes
> **Reference:** Table 16 &mdash; DIN EN 18222

| **Generic Status Code** | **Meaning** | **Has Result Object** |
| - | - | - |
| Success | Success | No |
| SuccessCreated | Successful creation of a new resource | No |
| SuccessAccepted | The reception of the request was successful | No |
| SuccessNoContent | Success with explicitly no content in the payload | No |
| ClientErrorBadRequest | Bad malformed request | Yes |
| ClientNotAuthorized | Wrong or missing authorization credentials | Yes |
| ClientForbidden | Authorization has been refused | Yes |
| ClientMethodNotAllowed | Method request is not allowed | Yes |
| ClientErrorResourceNotFound | Resource not found | Yes |
| ClientResourceConflict | Conflict-creating resource (resource already exists) | Yes |
| ServerInternalError | Unexpected Error | Yes |
| ServerErrorBadGateway | Bad gateway | Yes |
