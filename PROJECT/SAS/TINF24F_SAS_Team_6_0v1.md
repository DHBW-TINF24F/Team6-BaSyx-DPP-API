# **System Architecture Specification (SAS)**
*TIN24F, Software Engineering &mdash; Practice project 2025/2026*

| **Metadata**    | **Value**                           |
|-----------------|-------------------------------------|
| **Projectname** | Team 6 BaSyx DPP API (DIN EN 18222) |
| **Version**     | 1.2                                 |
| **Date**        | 2025-10-22                          |
| **Author**      | [Noah Becker](https://github.com/noahdbecker) |

---

##### Change History

| **Version** | **Date**   | **Author**  | **Comment**                         |
|-------------|------------|-------------|-------------------------------------|
| 1.0         | 2025-10-22 | Noah Becker | First sketch of contents & setting up Table of Contents  |
| 1.1         | 2025-10-22 | Noah Becker | Introduction  |

---

## Table of Contents
1. [Introduction](#1-introduction)  
    1.1. [Purpose and Scope](#11-purpose-and-scope)  
    1.2. [System Overview](#12-system-overview) 
    1.3. [References](#13-references) 
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

### 1.3. References

| **Ref ID**  | **Document Title**                                                                                   | **Version/Date** | **Source** |
|-------------|------------------------------------------------------------------------------------------------------|------------------|------------|
| [SRS]       | Software Requirements Specification                                                                  | &ndash;          | Internal   |
| [IEEE 1471] | IEEE Std 1471-2000: Recommended Practice for Architectural Description of Software-intensive Systems | &ndash;          | IEEE       |

<br>

## 2. Stakeholders and Concerns

*This section identifies the primary stakeholders involved in the development, deployment, and maintenance of the BaSyx DPP API, along with their main architectural concerns.  
Addressing these concerns ensures that the architecture meets the expectations and constraints of everyone affected by the system's design.*

Stakeholders are individuals or groups with an interest in the system's structure, behavior, or performance. They influence architectural decisions and serve as reference points for validation and design trade-offs.

| **Stakeholder Role** | **Description** | **Example Person/Group** |
|----------------------|-----------------|--------------------------|
| Project Manager      | Oversees project planning, scheduling, ressources, and delivery milestones. Ensures the project stays on time and within budget. | Nataliia Chubak |
| Product Manager      | Defines the product vision and feature priorities based on stakeholder and user needs. Aligns development goals with business objectives. | Luca Schmoll & Magnus Lörcher |
| Test Manager         | Plans and manages verification & validation activities. Ensures test coverage for functional and non-functional requirements. | Manuel Lutz |
| System Architect     | Designs and maintains the overall system architecture, ensuring alignment between requirements, design, and technology choices. | Noah Becker |
| Technical Editor     | Prepares and maintains project documentation, ensuring clarity, consistency, and compliance with organizational or academic standards. | Fabian Steiß |
| End Users            | Use the system to perform daily tasks or consume its output. Their satisfaction determines usability and acceptance. | &ndash; |
| External Systems / API Consumers | Interact with the system via APIs or data interfaces. Depend on stable, well-documented external endpoints. | &ndash; |
