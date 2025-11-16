# **System Architecture Specification (SAS)**

*TIN24F, Software Engineering &mdash; Practice project 2025/2026*

<br>

| **Metadata**    | **Value**                           |
|-----------------|-------------------------------------|
| **Projectname** | Team 6 BaSyx DPP API (DIN EN 18222) |
| **Version**     | 2.1                                 |
| **Date**        | 2025-11-16                          |
| **Author**      | [Noah Becker](https://github.com/noahdbecker) |

---

##### Change History

| **Version** | **Date**   | **Author**  | **Comment**                         |
|-------------|------------|-------------|-------------------------------------|
| 1.0         | 2025-10-22 | Noah Becker | Initialize and first sketch of content |
| 1.1         | 2025-10-22 | Noah Becker | Introduction |
| 1.2         | 2025-10-25 | Noah Becker | Stakeholders and Concerns |
| 1.3         | 2025-10-26 | Noah Becker | Architectural Overview &mdash; System Context |
| 1.4         | 2025-10-28 | Noah Becker | &bull; Architectural Overview &mdash; Design Approach <br> &bull; Structural Views &mdash; Grey-Box View <br> &bull; Behavioral Views &mdash; Communication Diagram |
| 1.5         | 2025-10-29 | Noah Becker | &bull; Behavioral Views &mdash; Sequence Diagram |
| 1.6         | 2025-11-05 | Noah Becker | Altering Frontend & Backend Technologies |
| 2.0         | 2025-11-07 | Noah Becker | Adapting software infrastructure to changing stakeholder needs |
| 2.1         | 2025-11-16 | Noah Becker | Added partial information to DPP Data Composition |

---

<br>

## Table of Contents

1. [Introduction](#1-introduction)  
    1.1. [Purpose and Scope](#11-purpose-and-scope)  
    1.2. [System Overview](#12-system-overview)  
2. [Architectural Overview](#2-architectural-overview)  
    2.1. [System Context](#21-system-context)  
    2.2. [Design Approach](#22-design-approach)  
3. [Structural Views](#3-structural-views)  
    3.1. [Grey-Box View](#31-grey-box-view)  
    3.2. [White-Box View](#32-white-box-view)  
4. [Behavioral Views](#4-behavioral-views)  
    4.1. [Communication Diagram](#41-communication-diagram)  
    4.2. [Sequence Diagrams](#42-sequence-diagram)  
5. [Data View](#5-data-view)  
    5.1. [Purpose](#51-purpose)  
    5.2. [Data Model and Data Flow](#52-data-model-and-data-flow)  
    5.3. [DPP Data Composition](#53-dpp-data-composition)
6. [Deployment View]()  
7. [Architectural Decisions and Rationale]()  
8. [Summary and Outlook]()  
9. [Appendices]()  

<br>

---

## 1. Introduction

### 1.1. Purpose and Scope

This SAS defines the architectural design of the Digital Product Passport (DPP  &ndash; dt. *Digitaler Produkt Pass*) software, including API endpoint specifications, frontend integration within the BaSyx WebUI, component responsibilities, and deployment considerations.

==The SAS defines how the system fulfills the functional and non-functional requirements defined in the *[Software Requirements Specifications (SRS)](/PROJECT/SRS/SRS.md)*.==

#### **Scope:**  

The architecture described here covers frontend, backend and API specifications of the software segment within the DPP API and Frontend views.  
The following areas are considered out of scope: general BaSyx software architecture.

<br>

### 1.2. System Overview

The system comprises two primary components: the *DPP Viewer* and the *DPP API*.

- ***DPP Viewer*** &mdash; A web viewpoint that presents DPP-related AAS submodels in a clear, responsive UI. It emphasises usability and maintainability and integrates into the BaSyx WebUI.
- ***DPP API*** &mdash; RESTful API endpoints exposing DPP data and submodel elements to developers and integrators, directly integrated into the BaSyx Environment API. It offers JSON responses, query/filter endpoints, and machine-readable API documentation (OpenAPI/Swagger).

Key capabilities:

- Visualisation of DPP-related AAS submodels for end users
- Programmatic access to DPP data via REST endpoints
- Easy-to-use API documentation through Swagger/OpenAPI
- Integration into the BaSyx WebUI and the BaSyx Backend Environment API

The system follows a microservices (Docker) architecture.  
Primary technologies include a Vue.js-sided Frontend, Java SpringBoot Backend, and a pipeline server deployment.  
External dependencies include the BaSyx Backend Services *BaSyx AAS Environment*, *BaSyx AAS Registry*, *BaSyx Submodel Registry* and *BaSyx AAS Discovery*.

<br><br>

## 2. Architectural Overview

The BaSyx DPP API operates as part of a broader environment that includes external users, services, and data sources.

<br>

### 2.1. System Context

*This section provides a Black-Box perspective, showing the system's boundaries, inputs, outputs, and primary interactions.*

**Purpose:**
To define what the system communicates with &ndash; *who* or *what* it depends on, and *what* depends on it.

**Context Description:**
The system receives user requests via a web frontend, processes this data through the exisiting AAS API, maps these responses to the DPP requirements, and produces an API response.

| **External Entity**     | **Type**     | **Interaction / Data Flow**                    | **Communication Channel** |
|-------------------------|--------------|------------------------------------------------|---------------------------|
| **End User**            | Human actor  | Submits requests via UI and receives Feedback  | Web UI / Browser          |
| **AAS Environment API** | API          | Provides necessary AAS content                 | REST API / HTTPS          |
| **Administrator**       | Human actor  | Monitors, configures, and maintains the system | SSH                       |

<br>

<img src="./src/black-box-view/TINF24F_SAS_Team_6_Black-Box-View_R10.drawio.svg" alt="BaSyx DPP API – Black Box View" width="100%" height="100%">

*Figure 2-1 &mdash; System Context Diagram (Black-Box-View) of the BaSyx DPP API showing external actors and data flows.*

<br>

### 2.2. Design Approach

*This section outlines the architectural style, principles, and technologies used to implement the system.*

**Architectural Style**  
The system integrates into the existing BaSyx infrastructure, working microservice-based:  

- **DPP Viewer *(Frontend)*:** The frontend integrates into the BaSyx WebUI as a web view, based as a module.
- **DPP API *(Backend)*:** The backend integrates into the BaSyx Environment API, providing new API endpoints.

<br>

The given microservice architectural style emphasizes:

- **Service Isolation:** Each service runs it its own container.
- **Scalability:** Individual services can be scaled horizontally as needed.
- **Independent Deployment:** Updates can be applied to each component separately via CI/CD pipelines.
- **Loose Coupling:** Services communicate through well-defined HTTP APIs.

<br>

**Logical Layers**  

| **Layer**                | **Description**                                           | **Implementation** (more [*here*](#technology-stack)) |
|--------------------------|-----------------------------------------------------------|--------------------|
| **Presentation Layer**   | Provides the user interface and handles client-side logic | Vue.js (JavaScript) |
| **Application Layer**    | Implements the core business logic and RESTful API        | Spring Boot (Java) |
| **Data Layer**           | Manages persistent data and ensures data integrity        | mongoDB *via existing BaSyx Infrastructure* |
| **Infrastructure Layer** | Handles routing, deployment, and orchestration            | Traefik Reverse Proxy, Docker, GitHub Actions CI/CD |

<br>

**Architectural Principles**  

- **Separation of Concerns:** UI, logic, and data are clearly divided across independent services.  
- **Loose Coupling / High Cohesion:** Components interact only via HTTP interfaces, maintaining clear boundaries.  
- **Containerization:** All services are encapsulated as Docker containers for consistent runtime environments.  
- **Automated Deployment:** GitHub Actions automates build, test, and deployment processes to ensure reliability and traceability.  
- **Security:** Traefik enforces HTTPS routing, and Spring Boot handles authentication and role-based access control.  
- **Scalability & Maintainability:** Each microservice can be updated or scaled independently without system downtime.  

<br>

<div id="technology-stack"></div>

**Technology Stack**  

| **Layer / Aspect**   | **Technology**      | **Purpose** |
|----------------------|---------------------|-------------|
| **Frontend**         | Vue.js (JavaScript) | User interface and interaction |
| **Backend**          | Spring Boot (Java)  | Application logic and API gateway |
| **Proxy / Router**   | Traefik             | Reverse proxy, SSL termination, routing |
| **Containerization** | Docker              | Service packaging and isolation |
| **CI/CD Pipeline**   | GitHub Actions      | Automated build, test, and deployment |

<br>

**Design Justification**  
The chosen microservice-based architecture allows modular development and simplifies maintenance by separating concerns across independent components.  
Using Docker ensures consistent environments across development and production.  
Traefik dynamically routes requests between containers and provides secure HTTPS access.  
The GitHub Actions pipeline ensures continuous integration and deployment, improving code quality and deployment speed.  
*This design was selected to meet key stakeholder concerns regarding scalability, maintainability, and automation.*

<br><br>

## 3. Structural Views

*This section provides the structural architecture of the BaSyx DPP API.*  

It presents the system at different abstraction levels to illustrate how the main subsystems are organized and how their internal components collaborate.  
The Grey-Box view describes subsystem boundaries and interactions, while the White-Box view details internal components and class-level structure.

### 3.1. Grey-Box View

The BaSyx DPP API system is decomposed into multiple independent submodules to support scalability, maintainability, and modular development. Each submodule encapsulates a distinct responsibility and communicates through well-defined interfaces.  
This decomposition enables parallel development, reduces coupling, and allows individual servies to be deployed and scaled independently.

<br>

<img src="./src/grey-box-view/TINF24F_SAS_Team_6_Grey-Box-View_R10.drawio.svg" alt="BaSyx DPP API – Grey Box View" width="100%" height="100%">

*Figure 3-1 &mdash; Subsystem architecture overview of the BaSyx DPP (API), showing the central microservices and their integration points with existing BaSyx backend services.*

<br>

**Subsystem Responsibilities:**

| **Submodule**         | **Responsibility**                                                                 |
|-----------------------|------------------------------------------------------------------------------------|
| Frontend              | User interface rendering, interaction handling, form validation, HTTP request flow |
| DPP API               | Business logic, request validation, domain mapping, REST endpoints                 |
| BaSyx Environment API | Provides access to persisted AAS-related product information and submodel data     |
| Traefik               | Routes incoming requests, performs SSL termination, and handles service discovery  |

<br>

**Subsystem Collaboration**  

The Frontend communicates with the Backend through RESTful HTTP requests routed via the Docker internal networking. 
The Backend retrieves product- and submodel-related information by internal queryies to the BaSyx Environment API, which connects to a MongoDB database. 
Traefik dynamically routes traffic based on container labels, ensuring request isolation and secure HTTPS access.

<br>

**Subsystem Boundaries**  

- Presentation concerns are strictly contained within the Frontend.  
- Domain logic, schema mapping, and validation remain inside the Backend.  
- Persistence and AAS service interaction are delegated to the corresponding API endpoints provided by the BaSyx Environment API.  
- Infrastructure-level routing and TLS termination are handled centrally by Traefik.

<br>

**Known Limitations**  

- The BaSyx Environment API is treated as a black-box dependency. Changes to its data schema may require adaptation layers in the backend.
- Runtime coupling exists with the BaSyx Environment availability; fallback strategies are limited in the current state.

<br>

### 3.2. White-Box View

==tbd==

<br><br>

## 4. Behavioral Views

### 4.1. Communication Diagram

<img src="./src/communication-diagram/TINF24F_SAS_Team_6_Communication-Diagram_R10.drawio.svg" alt="BaSyx DPP API – Communication Diagram" width="100%" height="100%">

*Figure 4-1 &mdash; Communication Diagram for the DPP Data Retrieval &ndash; Communication flow between user, frontend, Traefik, backend, and the BaSyx Environment API during a Digital Product Passport (DPP) request.*

<br>

The user triggers the request via the frontend. The frontend calls the backend service using RESTful HTTP requests. 
*Traefik routes the request to the correct container based on service labels. (On server deployment)*  
The backend internally queries the BaSyx Environent API endpoints to retrieve AAS and submodel data and then maps the result to the internal DPP schema. 
The processed data is returned to the frontend as a JSON payload and finally rendered in the UI.

If the BaSyx Environment API endpoints are unavailable, the backend returns a descriptive error response.

<br>

### 4.2. Sequence Diagram

The following sequence diagram provides a detailed view of the runtime behavior of the system when a user requests Digital Product Passport (DPP) data.
It shows the chronological order of messages exchanged between the involved components and highlights the responsibilities of each subsystem during the request-response lifecycle.

<br>

<img src="./src/sequence-diagram/TINF24F_SAS_Team_6_Sequence-Diagram_R10.drawio.svg" alt="BaSyx DPP API – Sequence Diagram" width="100%" height="100%">

*Figure 4-2 &mdash; Sequence Diagram for DPP Data Retrieval &ndash; chronological interaction between the user, frontend, Traefik, backend, and the BaSyx Environment API endpoints during a Digital Product Passport (DPP) request.*

<br>

When a user initiates a DPP data request through the frontend, the application sends an HTTP request to the backend service via the Traefik reverse proxy. Traefik forwards the request to the appropriate backend container based on routing rules.
The backend validates the request, queries the BaSyx Environment API endpoints for the required AAS and submodel information, and maps the received data to the internal DPP schema.
Once processing is complete, the backend returns the consolidated JSON response to the frontend, which then renders the corresponding product information to the user.

<br>

**Rationale**  

This sequence demonstrates a clear separation of concerns: user interaction and rendering are handled by the frontend, data orchestration and processing by the backend, and AAS data retrieval by the BaSyx Environment API endpoints. This separation improves maintainability, testability, and supports the modular microservice architecture.

<br>

**Known Limitations**  

If the BaSyx Environment API fails to respond, the backend returns an appropriate error message to the frontend to prevent partial or incorrect data from being displayed.

<br><br>

## 5. Data View

*This view provides an architectural understanding of how data is represented, transformed, and exchanged between system components and external services.*

### 5.1. Purpose

The purpose of the Data View is to describe the structure, flow, ownership, and constraints of the data that is relevant to the Digital Product Passport (DPP) system.
It ensures transparency regarding data semantics and supports architectural decisions that impact maintainability, interoperability, and data integrity.

This view is architecturally relevant because the system relies on external data sources (BaSyx AAS infrastructure) and performs internal data mapping to deliver DPP-specific information to end users and API consumers. As a result, data consistency, interpretation, and transformation are central architectural concerns.

<br>

### 5.2. Data Model and Data Flow

The system does not maintain its own persistent storage. Instead, it retrieves product-related information from the BaSyx AAS infrastructure and converts this data into a DPP-specific format. The conceptual model is centred around four core entities:

- **Asset Administration Shell (AAS):** Digital representation of a product and anchor point for related data.
- **Submodels:** Semantically grouped data sets under the AAS (e.g., product characteristics, identification, lifecycle).
- **DPP Data Object:** A structured representation derived from one or more Submodels, shaped into a DPP-aligned model for presentation and API exposure.
- **Product:** Logical domain reference used to request and interpret DPP information for a specific product instance.

<br>

**Data Flow Overview:**  

**1.** The frontend requests DPP data from the backend.
**2.** The backend queries the BaSyx Environment API endpoints for AAS and Submodel data.
**3.** The backend validates and maps this data into a unified DPP schema.
**4.** The processed result is returned as JSON to the frontend (and optionally external API consumers).

The backend acts as the sole integration and transformation point to ensure a consistent interpretation of data. No data modification or persistence occurs beyond runtime transformation for display or API output.

<br>

### 5.3. DPP Data Composition

**Relevant Submodels:**  
The DPP of a product includes information about the following seven Submodels:

- Digital Nameplate *&mdash; (IDTA-02035-1)*
- Handover Documentation *&mdash; (IDTA-02035-2)*
- Product Carbon Footprint *&mdash; (IDTA-02035-3)*
- Technical Data *&mdash; (IDTA-02035-4)*
- Product Condition *&mdash; (IDTA-02035-5)*
- Material Composition *&mdash; (IDTA-02035-6)*
- Circularity *&mdash; (IDTA-02035-7)*

All relevant Submodels are stored in the AAS and need to be retrieved in order to return the DPP of a product.

<br>

**Data flow &mdash; DPP request:**  

| **Step (no.)** | **Step (title)** | **Description** | **Involved endpoint(s)** |
|----------------|------------------|-----------------|--------------------------|
| **1**          | Request          | User or third-party-application requests the DPP of a product | Frontend / DPP API endpoint |
| **2**          | Data retrieval   | DPP API has to gather the necessary information about the submodels of the product | DPP API <br> BaSyx Environment API |
| **3**          | Data mapping     | Reducing the response-data to the DPP-relevant keys and map the response to the required DPP scheme | DPP API |
| **4**          | Response         | Respond with the correct DPP data object | DPP API <br> Third-Party-Application |
| **5^\*^**      | Display data     | Display the data visually in the DPP Viewer | Frontend / User |

*\* Only when request origins from the DPP Viewer*

<br>

**Important considerations:**

*Hier bspw:  
Mapping bei POST und PATCH API Calls, gleiches Schema wie Environment API anwenden, sodass möglichst wenig gemappt werden muss  
Vorgaben der DIN-Norm akribisch berücksichtigen und Implementierung aufzeigen, Anmerkung zu OpenAPI File möglich  
Parameterbenamung teils inkonsistent in Vorgaben, hier Äquivalente aufzeigen bzw. generell definieren (Zweck, Synonyme/Aliase, Format, Beispiel [immer das gleiche Beispiel zum Verständnis])*

<br>

**Ambiguities and further outlook:**  

*Hier bspw:  
Versionierung von DPPs, sehr wichtig aber (eventuell) kompliziert umzusetzen*
