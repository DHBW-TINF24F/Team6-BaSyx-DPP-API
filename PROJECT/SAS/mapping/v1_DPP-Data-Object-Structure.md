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
<td><code>!</code> <a href="#only-dppid-schema">Only dppId schema</a> <code>!</code> <br> Payload: -</td>
<td><a href="#with-result-object">Normal schema</a> <br> Payload: <a href="#dpp-updated">DPP updated</a></td>
<td><code>!</code> <a href="#without-result-object">Only statusCode schema</a> <code>!</code> <br> Payload: -</td>
<td><code>!</code> <a href="#registry-schema">Registry schema</a> <code>!</code> <br> Payload: -</td>
</tr>

<tr>
<th>Payload</th>
<td>-</td>
<td><a href="#dpp-updated">DPP updated</a></td>
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
<td><a href="#with-result-object">Normal schema</a> <br> Payload: <a href="#dpp-dataelementcollection">DPP DataElementCollection</a></td>
<td><a href="#with-result-object">Normal schema</a> <br> Payload: <a href="#dpp-property">DPP Property</a></td>
<td><a href="#with-result-object">Normal schema</a> <br> Payload: <a href="#dpp-dataelementcollection">DPP DataElementCollection</a></td>
<td><a href="#with-result-object">Normal schema</a> <br> Payload: <a href="#dpp-property">DPP Property</a></td>
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

### DPP object
> *This object contains the main DPP information to the requested DPP entry.*

```json
// to be done

"payload": [

]
```

### DPP Identifiers

### DPP updated

### DPP DataElementCollection

### DPP Property

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
