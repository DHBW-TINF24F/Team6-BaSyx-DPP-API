# User Manual: BaSyx Digital Product Pass (DPP) Web UI

## Project 6: API for the Digital Product Pass (DPP) in the BaSyx Framework

### Customer
|Name|Mail|
|---|---|
|Markus Rentschler|rentschler@lehre.dhbw-stuttgart.de|
|Pawel Wojcik|pawel.wojcik@lehre.dhbw-stuttgart.de|

---

### Introduction
This document serves as the **User Manual** for the **BaSyx Digital Product Pass (DPP) Web UI**. It describes how end users can interact with and explore Digital Product Passes through the web-based interface. The interface is based on the DIN EN 18222 standard and is seamlessly integrated into the BaSyx Framework.

**Live Web UI:** [https://srv01.noah-becker.de/uni/swe/basyx/](https://srv01.noah-becker.de/uni/swe/basyx/)

---

### Document History

|Version|Author|Date|Comment|
|---|---|---|---|
|1.0|Fabian Steiß|10.05.2026|Creation of the User Manual|

---

### Terms and Abbreviations
|Abbreviation|Meaning|
|---|---|
|DPP|Digital Product Pass|
|AAS|Asset Administration Shell|
|BaSyx|Open Source Framework|
|Submodel|Part of an AAS|
|UI|User Interface|

---

### Table of Contents
1. [Purpose and Scope](#1-purpose-and-scope)
    1. [Purpose](#11-purpose)
    2. [Scope](#12-scope)
2. [Use Cases](#2-use-cases)
3. [Web UI Features](#3-web-ui-features)
    1. [General Operation](#31-general-operation)
    2. [DPP-specific Features](#32-dpp-specific-features)
4. [Detailed Instructions](#4-detailed-instructions)
5. [Usability Concept & Workflows](#5-usability-concept--workflows)
6. [Tips and Troubleshooting](#6-tips-and-troubleshooting)
7. [MockUps & Screenshots](#7-mockups--screenshots)
8. [References](#8-references)

---

## 1. Purpose and Scope

### 1.1 Purpose
This User Manual helps end users to easily and efficiently use the **Digital Product Pass** through the BaSyx Web UI. It explains all relevant functions of the DPP Viewer and shows how to navigate and view product information in a clear and structured way.

### 1.2 Scope
This manual refers exclusively to the **Web UI (Frontend)** available at  
[https://srv01.noah-becker.de/uni/swe/basyx/](https://srv01.noah-becker.de/uni/swe/basyx/).  
It is intended for end users who want to view and explore Digital Product Passes.

---

## 2. Use Cases
The most important use cases from the user perspective are:

- Display and explore an existing Digital Product Pass
- Navigate between different Submodels
- View detailed product information
- Switch between DPP View, AAS View, and Submodel View

---

## 3. Web UI Features

### 3.1 General Operation

#### FR-FE-05 – Main Menu
At the top of the application there is a menu that allows you to switch between the following views:
- **DPP Viewer**
- **AAS Viewer**
- **Submodel Viewer**

### 3.2 DPP-specific Features

#### FR-FE-01 – Visualize a Digital Product Pass
1. Open the Web UI
2. Switch to the **DPP Viewer**
3. Enter an **AAS ID**, **DPP ID**, or a full URL
4. The complete Digital Product Pass will be loaded

#### FR-FE-02 & FR-FE-03 – Navigation within the DPP
- A **sidebar** on the left side shows all available DPP Submodels
- Click on a Submodel to display its content in the main area

#### FR-FE-04 – Submodel Highlighting
The currently selected Submodel is visually highlighted in the sidebar.

#### FR-FE-06 – Product Information
The header area of the viewer displays:
- Product Name
- Product ID
- DPP Version
- Other key metadata

#### FR-FE-07 – Information Display
The data of each Submodel is presented in a clear **two-column layout**.

#### FR-FE-08 – Missing Data
If data is not available, it is clearly marked.

#### FR-FE-09 – Tooltips
Hover over individual data fields to see explanatory tooltips based on DIN EN 18222 and IDTA specifications.

#### FR-FE-10 – Responsive Design
The interface automatically adapts to desktop and mobile devices.

---

## 4. Detailed Instructions

### How to Display a DPP
1. Open the Web UI
2. Select **DPP Viewer** in the top menu
3. Enter the desired ID or URL and confirm
4. Use the left sidebar to browse through the Submodels

### Switching Submodels
Simply click on the desired Submodel in the left sidebar (e.g. Digital Nameplate, Product Carbon Footprint, Material Composition, etc.).

---

## 5. Usability Concept & Workflows

**Core Workflow:**
1. Find a product / DPP
2. Switch to the **DPP Viewer**
3. Select the desired Submodel via the sidebar
4. View the information and use tooltips for more details

Additional features:
- Direct deep links to specific DPPs
- Clear visual highlighting and separation
- High readability thanks to the two-column layout

---

## 6. Tips and Troubleshooting

| Problem | Solution |
|---------|----------|
| DPP does not load | Verify the ID and ensure the AAS exists in the system |
| Submodels are missing | The DPP must contain the corresponding IDTA Submodels |
| Display is incomplete | Reload the page or try another browser (Chrome/Firefox recommended) |
| No tooltips appear | Hover the mouse over the field labels |

---

## 7. MockUps & Screenshots
Screenshots of the current interface can be found directly in the Web UI or in the project repository under `/PROJECT/PM/Mockups & Wireframes`.

---

## 8. References

| No. | Reference | Title |
|-----|-----------|-------|
| 1 | DIN EN 18222 | Digital Product Passport - APIs |
| 2 | IDTA-02035-x | Submodel Templates for DPP |
| 3 | BaSyx Framework | Eclipse BaSyx |
| 4 | HARTING Demo | Reference Implementation |