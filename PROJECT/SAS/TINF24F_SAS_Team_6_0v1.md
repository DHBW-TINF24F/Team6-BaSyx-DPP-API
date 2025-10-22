# **System Architecture Specification (SAS)**
*TIN24F, Software Engineering &mdash; Practice project 2025/2026*

---

##### Version Control

| **Version** | **Date**   | **Author**                                     | **Comment**                         |
|-------------|------------|------------------------------------------------|-------------------------------------|
| 1.0         | 22.10.2025 | [Noah Becker](https://github.com/noahdbecker)  | First sketch of contents & setting up Table of Contents  |
| 1.1         | 22.10.2025 | [Noah Becker](https://github.com/noahdbecker)  | ...  |

---

## Table of Contents
1. [Introduction](#1-introduction)  
    1.1. [Purpose and Scope](#11-purpose-and-scope)  
    1.2. [System Overview](#12-system-overview)  
2. [Stakeholders and Concerns]()  
3. [Architectural Overview]()  
    3.1. [System Context]()  
    3.2. [Design Approach]()  
4. [Structural Views]()  
    4.1. [Grey-Box View]()  
    4.2. [White-Box View]()  
5. [Behavioral Views]()  
    5.1. [Communication Diagram]()  
    5.2. [Sequence Diagrams]()  
6. [Data View]()  
    6.1. [ERM Diagram]()  
    6.2. [Data Model]()  
7. [Deployment View]()  
8. [Rationale and Traceability]()  
9. [Summary and Outlook]()  
10. [Appendices]()  

---

## 1. Introduction

### 1.1. Purpose and Scope

This SAS defines the architectural design of the Digital Product Passport (DPP  &ndash; dt. *Digitaler Produkt Pass*) software, including API endpoint specifications, frontend integration with the BaSyx WebUI, component responsibilities, and deployment considerations.

The SAS defines how the system fulfills the functional and non-functional requirements defined in the *[Software Requirements Specifications (SRS)]()*.

#### **Scope:**  

The architecture described here covers frontend, backend and API specifications.  
The following areas are considered out of scope: ==*to be done*==

### 1.2. System Overview

The system comprises two primary components: the *DPP Viewer* and the *DPP API*.

- ***DPP Viewer*** &mdash; A web application that presents DPP-related AAS submodels in a clear, responsive UI. It emphasises usability and maintainability and integrates into the BaSyx WebUI as a navigation module.
- ***DPP API*** &mdash; A RESTful API exposing DPP data and submodel elements to developers and integrators. It offers JSON responses, query/filter endpoints, and machine-readable API documentation (OpenAPI/Swagger).

Key capabilities:

- Visualisation of DPP-related AAS submodels for end users
- Programmatic access to DPP data via REST endpoints
- Easy-to-use API documentation through Swagger/OpenAPI
- Integration with the BaSyx WebUI navigation and existing authentication mechanisms

The system follows a microservices (Docker) architecture.  
Primary technologies include a *==?==*-sided Frontend, Python-sided Backend, and a pipeline server deployment.  
External dependencies include the BaSyx Backend Services *==?==*, *==?==*, and *==?==*.
