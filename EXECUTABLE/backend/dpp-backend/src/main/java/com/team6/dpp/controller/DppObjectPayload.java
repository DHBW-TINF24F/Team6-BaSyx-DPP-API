package com.team6.dpp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.fasterxml.jackson.databind.node.ArrayNode;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.*;

public class DppObjectPayload {

    public ObjectNode createAasPayload(JsonNode result) {

        ObjectMapper mapper = new ObjectMapper();

        // Root Node

        ObjectNode root = mapper.createObjectNode();

        ObjectNode payload = root.putObject("payload");

        // --- 1. ADMINISTRATION ---

        ObjectNode administration = payload.putObject("administration");

        // creator

        administration.putObject("creator")

                .putArray("keys")

                .addObject()

                .putPOJO("type", result.get("administration").get("creator").get("keys").get(0).get("type"))

                .putPOJO("value", result.get("administration").get("creator").get("keys").get(0).get("value"));

        // assetInformation

        ObjectNode assetInfo = administration.putObject("assetInformation");

        assetInfo.putPOJO("assetKind", result.get("assetInformation").get("assetKind"));

        assetInfo.putObject("defaultThumbnail")

                .putPOJO("contentType", result.get("assetInformation").get("defaultThumbnail").get("contentType"))

                .putPOJO("path", result.get("assetInformation").get("defaultThumbnail").get("path"));

        assetInfo.putPOJO("globalAssetId", result.get("assetInformation").get("globalAssetId"));

        // basic administration fields

        administration.putPOJO("version", result.get("administration").get("version"));

        administration.putPOJO("id", result.get("administration").get("id"));

        administration.putPOJO("idShort", result.get("idShort"));

        // description

        administration.putArray("description")

                .addObject().put("language", "en").put("text", "HARTING Asset Administration Shell ZSN1");

        // displayName

        ArrayNode displayName = administration.putArray("displayName");

        displayName.addObject().put("language", "en").put("text", "HARTING AAS ZSN1");

        displayName.addObject().put("language", "de").put("text", "HARTING AAS ZSN1 DE");

        // --- 2. SUBMODELS ---

        ArrayNode submodels = payload.putArray("submodels");

        ObjectNode submodel1 = submodels.addObject();

        submodel1.put("type", "ExternalReference"); // Gehört auf Ebene des Array-Eintrags

        ObjectNode submodelKey = submodel1.putArray("keys").addObject();

        submodelKey.put("type", "Submodel");

        submodelKey.put("value", "https://dpp40.harting.com/shells/ZSN1/submodels/Nameplate/3/0");

        submodelKey.put("reference", "https://admin-shell.io/zvei/nameplate/3/0/Nameplate");

        // Payload innerhalb von Submodel Key

        ObjectNode smPayload = submodelKey.putObject("payload");

        smPayload.putArray("ProductArticleNumberOfManufacturer")

                .addObject().put("en", "09 30 024 0301, 09 33 024 2702, 09 33 000 6204");

        smPayload.putArray("ManufacturerName")

                .addObject().put("de", "HARTING Electric Stiftung & Co. KG");

        smPayload.putArray("OrderCodeOfManufacturer")

                .addObject().put("en", "09300240301, 09330242702, 09330006204");

        smPayload.put("CountryOfOrigin", "");

        smPayload.put("YearOfConstruction", "");

        // Markings

        ObjectNode marking = smPayload.putObject("Markings").putObject("Marking");

        marking.putObject("MarkingFile")

                .put("contentType", "image/png")

                .put("value", "aHR0cHM6... (gekürzt für Übersichtlichkeit)");

        marking.put("MarkingName", "");

        // ManufacturerProductType

        ArrayNode prodType = smPayload.putArray("ManufacturerProductType");

        prodType.addObject().put("de", "Han 24B Assembly ZSN1");

        prodType.addObject().put("en", "Han 24B Assembly ZSN1");

        // ContactInformation

        ObjectNode contact = smPayload.putObject("ContactInformation");

        contact.putArray("Company").addObject().put("de", "HARTING Electric Stiftung & Co. KG");

        ObjectNode phone = contact.putObject("Phone");

        phone.put("TypeOfTelephone", "Office");

        phone.putArray("TelephoneNumber").addObject().put("de", "+49 5772 47-97100");

        contact.putArray("NationalCode").addObject().put("de", "DE");

        contact.put("AddressOfAdditionalLink", "https://harting.com");

        contact.putArray("Zipcode").addObject().put("de", "32339");

        contact.putArray("Street").addObject().put("de", "Wilhelm-Harting-Str. 1");

        contact.putArray("StateCounty").addObject().put("de", "Nordrhein-Westfalen");

        contact.putArray("CityTown").addObject().put("de", "Espelkamp");

        // Weitere Felder auf smPayload Ebene...

        smPayload.putArray("ManufacturerProductFamily").addObject().put("de", "Han® B");

        smPayload.putObject("CompanyLogo").put("contentType", "image/png").put("value", "aHR0cHM6...");

        smPayload.put("URIOfTheProduct", "https://b2b.harting.com/ebusiness/de/hcpproductconfigurator?zConfID=ZSN1");

        return root;

    }

}

/*
 * 
 * DppObjectPayload payload;
 * payload.administration.createor.keys.value = "neue value";
 * 
 * "payload": {
 * "administration": {
 * "creator": {
 * "keys": [
 * {
 * "type": "GlobalReference",
 * "value": "sebastian.eicke@harting.com"
 * }
 * ],
 * },
 * "assetInformation": {
 * "assetKind": "Type",
 * "defaultThumbnail": {
 * "contentType": "image/png",
 * "path": "b24b11da.png"
 * },
 * "globalAssetId": "https://pk.harting.com/?.20P=ZSN1"
 * },
 * "version": "1.0.1",
 * "id": "https://dpp40.harting.com/shells/ZSN1",
 * "description": [
 * {
 * "language": "en",
 * "text": "HARTING Asset Administration Shell ZSN1"
 * }
 * ],
 * "displayName": [
 * {
 * "language": "en",
 * "text": "HARTING AAS ZSN1"
 * },
 * {
 * "language": "de",
 * "text": "HARTING AAS ZSN1 DE"
 * }
 * ],
 * "idShort": "HARTING_AAS_ZSN1"
 * },
 * "submodels": [
 * {
 * "keys": [
 * {
 * "type": "Submodel",
 * "value": "https://dpp40.harting.com/shells/ZSN1/submodels/Nameplate/3/0",
 * "reference": "https://admin-shell.io/zvei/nameplate/3/0/Nameplate", // aus
 * GET /submodels/{submodelIdentifier}/$metadata
 * "payload": { // aus GET /submodels/{submodelIdentifier}/$value
 * "ProductArticleNumberOfManufacturer": [
 * {
 * "en": "09 30 024 0301, 09 33 024 2702, 09 33 000 6204"
 * }
 * ],
 * "ManufacturerName": [
 * {
 * "de": "HARTING Electric Stiftung & Co. KG"
 * }
 * ],
 * "OrderCodeOfManufacturer": [
 * {
 * "en": "09300240301, 09330242702, 09330006204"
 * }
 * ],
 * "CountryOfOrigin": "",
 * "YearOfConstruction": "",
 * "Markings": {
 * "Marking": {
 * "MarkingFile": {
 * "contentType": "image/png",
 * "value":
 * "aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMS9zdWJtb2RlbHMvTmFtZXBsYXRlLzIvMA-Markings.Marking.MarkingFile-8abf5add.png"
 * },
 * "MarkingName": ""
 * }
 * },
 * "ManufacturerProductType": [
 * {
 * "de": "Han 24B Assembly ZSN1"
 * },
 * {
 * "en": "Han 24B Assembly ZSN1"
 * }
 * ],
 * "ContactInformation": {
 * "Company": [
 * {
 * "de": "HARTING Electric Stiftung & Co. KG"
 * }
 * ],
 * "Phone": {
 * "TypeOfTelephone": "Office",
 * "TelephoneNumber": [
 * {
 * "de": "+49 5772 47-97100"
 * }
 * ]
 * },
 * "NationalCode": [
 * {
 * "de": "DE"
 * }
 * ],
 * "AddressOfAdditionalLink": "https://harting.com",
 * "Zipcode": [
 * {
 * "de": "32339"
 * }
 * ],
 * "Street": [
 * {
 * "de": "Wilhelm-Harting-Str. 1"
 * }
 * ],
 * "StateCounty": [
 * {
 * "de": "Nordrhein-Westfalen"
 * }
 * ],
 * "CityTown": [
 * {
 * "de": "Espelkamp"
 * }
 * ]
 * },
 * "ManufacturerProductFamily": [
 * {
 * "de": "Han® B"
 * },
 * {
 * "en": "Han® B"
 * }
 * ],
 * "CompanyLogo": {
 * "contentType": "image/png",
 * "value":
 * "aHR0cHM6Ly9kcHA0MC5oYXJ0aW5nLmNvbS9zaGVsbHMvWlNOMS9zdWJtb2RlbHMvTmFtZXBsYXRlLzIvMA-CompanyLogo-ad7a78d6.png"
 * },
 * "ManufacturerProductRoot": [
 * {
 * "de": "Han®"
 * },
 * {
 * "en": "Han®"
 * }
 * ],
 * "URIOfTheProduct":
 * "https://b2b.harting.com/ebusiness/de/hcpproductconfigurator?zConfID=ZSN1",
 * "ManufacturerProductDesignation": [
 * {
 * "de": "Han 24B Assembly ZSN1"
 * },
 * {
 * "en": "Han 24B Assembly ZSN1"
 * }
 * ]
 * }
 * }
 * ],
 * "type": "ExternalReference"
 * },
 * {
 * // ... für die oben genannten Submodels
 * }
 * ]
 * }
 */