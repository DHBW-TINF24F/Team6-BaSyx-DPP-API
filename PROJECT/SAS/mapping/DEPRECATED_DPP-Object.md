# DPP Object &ndash; Data Retrieval Workflow

## Basic AAS Data

**Parameters**
aasIdentifier &ndash; base64 encoded ID

```powershell
$data = (Invoke-WebRequest -Uri "http://localhost:8081/shells/{aasIdentifier}" -Method Get).Content | ConvertFrom-JSON
```

<br>

### AAS: DisplayName
> Retrieve information about the configured displayName. Returns in all languages.

```powershell
$data | Select-Object -ExpandProperty displayName
```

<br>

### AAS: Description
> Retrieve information about the description. Returns in all languages.

```powershell
$data | Select-Object -ExpandProperty description
```

<br>

### AAS: Version
> Retrieve the *version*-Attribute of an AAS-Shell.

```powershell
$data | Select-Object -ExpandProperty administration | Select-Object -ExpandProperty version
```

<br><br>

---
