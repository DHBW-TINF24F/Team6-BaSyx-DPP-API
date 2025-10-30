# **System Architecture Specification (SAS)**

*TIN24F, Software Engineering &mdash; Practice project 2025/2026*

<br>

| **Metadata**    | **Value**                           |
|-----------------|-------------------------------------|
| **Projectname** | Team 6 BaSyx DPP API (DIN EN 18222) |
| **Version**     | 1.5                                 |
| **Date**        | 2025-10-22                          |
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

---

<br>

## Table of Contents

1. [Introduction](#1-introduction)  
2. [Stakeholders and Concerns](#2-stakeholders-and-concerns)  
3. [Architectural Overview](#3-architectural-overview)  
    3.1. [System Context](#31-system-context)  
    3.2. [Design Approach](#32-design-approach)  
4. [Structural Views](#4-structural-views)  
    4.1. [Grey-Box View](#41-grey-box-view)  
    4.2. [White-Box View](#42-white-box-view)  
5. [Behavioral Views](#5-behavioral-views)  
    5.1. [Communication Diagram](#51-communication-diagram)  
    5.2. [Sequence Diagrams](#52-sequence-diagram)  
6. [Data View](#6-data-view)
    6.1. [Purpose and Stakeholder Concerns](#61-purpose-and-stakeholder-concerns)
    6.2. [Data Model and Data Flow](#62-data-model-and-data-flow)
    6.3. [Data Formats, Constraints, and Key Decisions](#63-data-formats-constraints-and-key-decisions)
7. [Deployment View]()  
8. [Architectural Decisions and Rationale]()  
9. [Summary and Outlook]()  
10. [Appendices]()  

<br>

---

## 1. Introduction

### 1.1. Purpose and Scope

This SAS defines the architectural design of the Digital Product Passport (DPP  &ndash; dt. *Digitaler Produkt Pass*) software, including API endpoint specifications, frontend integration with the BaSyx WebUI, component responsibilities, and deployment considerations.

The SAS defines how the system fulfills the functional and non-functional requirements defined in the *[Software Requirements Specifications (SRS)]()*.

#### **Scope:**  

The architecture described here covers frontend, backend and API specifications.  
The following areas are considered out of scope: BaSyx software architecture *(mainly Vue.js & Java SpringBoot)*.

<br>

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
Primary technologies include a React-sided Frontend, Python-sided Backend, and a pipeline server deployment.  
External dependencies include the BaSyx Backend Services *BaSyx AAS Environment*, *BaSyx AAS Registry*, *BaSyx Submodel Registry* and *BaSyx AAS Discovery*.

<br>

### 1.3. References

| **Ref ID**  | **Document Title**                                                                                   | **Version/Date** | **Source** |
|-------------|------------------------------------------------------------------------------------------------------|------------------|------------|
| [SRS]       | Software Requirements Specification                                                                  | &ndash;          | Internal   |
| [IEEE 1471] | IEEE Std 1471-2000: Recommended Practice for Architectural Description of Software-intensive Systems | 10/2000          | IEEE       |

<br><br>

## 2. Stakeholders and Concerns

*This section identifies the primary stakeholders involved in the development, deployment, and maintenance of the BaSyx DPP API, along with their main architectural concerns.  
Addressing these concerns ensures that the architecture meets the expectations and constraints of everyone affected by the system's design.*

### 2.1. Stakeholder Overview

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

<br>

### 2.2. Stakeholder Concerns and Architectural Impact

| **Stakeholder** | **Key Concerns** | **Architectural Impact / Response** |
|-----------------|------------------|-------------------------------------|
| Project Manager | &bull; Meeting deadlines and budgets <br> &bull; Predictable development progress <br> &bull; Risk management | &rArr; Use modular design for parallel development <br> &rArr; Support incremental delivery (CI/CD) <br> &rArr; Provide clear component responsibilities |
| Product Manager | &bull; Product fulfills functional goals <br> &bull; Scalability for future features <br> &bull; Alignment with user need | &rArr; Maintain flexible architecture (e.g. layered or service-oriented) <br> &rArr; Separate domain logic from UI for easier feature extension |
| Test Manager | &bull; Testability and reproducibility <br> &bull; Automation support <br> &bull; Traceability from requirements to components | &rArr; Provide decoupled modules with mockable interfaces <br> &rArr; Use standardized test environments and CI integration <br> &rArr; Document traceability in SAS |
| System Architect | &bull; Technical feasibility and consistency <br> &bull; Performance and maintainability <br> &bull; Compliance with standards | &rArr; Define architecture views (Grey-Box/White-Box) <br> &rArr; Enforce coding and interface standards <br> &rArr; Use clear component interfaces and versioned APIs |
| Technical Editor | &bull; Up-to-date and accurate documentation <br> &bull; Consistency between design and implementation | &rArr; Use architecture diagrams as single source of truth <br> &rArr; Maintain auto-generated API and data documentation (OpenAPI, ERM) |
| End Users | &bull; Usability and responsiveness <br> &bull; Reliability and data integrity <br> &bull; Accessibility and support | &rArr; Emphasize performance in deployment design <br> &rArr; Apply UI/UX consistency standards <br> &rArr; Include validation and fallback mechanisms. |
| External Systems / API Consumers | &bull; Stable, versioned interfaces <br> &bull; Predictable behavior and error handling <br> &bull; Secure access and data formats | &rArr; Define REST/GraphQL endpoints and schemas <br> &rArr; Apply authentication (OAuth, API keys) <br> &rArr; Provide versioned API documentation |

<br><br>

## 3. Architectural Overview

The BaSyx DPP API operates as part of a broader environment that includes external users, services, and data sources.

<br>

### 3.1. System Context

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

*Figure 3-1 &mdash; System Context Diagram (Black-Box-View) of the BaSyx DPP API showing external actors and data flows.*

<br>

### 3.2. Design Approach

*This section outlines the architectural style, principles, and technologies used to implement the system.*

**Architectural Style**  
The system follows a microservice architecture.  
This approach separates the system into independently deployable services &mdash; primarily a React-based frontend and a Django-based backend &mdash; managed and orchestrated through Docker and Traefik.  
This approach was selected to ensure seamless integration with the existing BaSyx microservices architecure, enabling modular expansion of the system.

<br>

This microservice architectural style emphasizes:

- **Service Isolation:** Each service (frontend, backend) runs it its own container.
- **Scalability:** Individual services can be scaled horizontally as needed.
- **Independent Deployment:** Updates can be applied to each component separately via CI/CD pipelines.
- **Loose Coupling:** Services communicate through well-defined HTTP APIs.

<br>

**Logical Layers**  

| **Layer**                | **Description**                                          | **Implementation** |
|--------------------------|----------------------------------------------------------|--------------------|
| **Presentation Layer**   | Provides the user interface and hanles client-side logic | React (TypeScript) |
| **Application Layer**    | Implements the core business logic and RESTful API       | Django (Python) |
| **Data Layer**           | Manages persistent data and ensures data integrity       | mongoDB *via BaSyx Environment API* |
| **Infrastructure Layer** | Handles routing, deployment, and orchestration           | Docker, Traefik Reverse Proxy, GitHub Actions CI/CD |

<br>

**Architectural Principles**  

- **Separation of Concerns:** UI, logic, and data are clearly divided across independent services.  
- **Loose Coupling / High Cohesion:** Components interact only via HTTP interfaces, maintaining clear boundaries.  
- **Containerization:** All services are encapsulated as Docker containers for consistent runtime environments.  
- **Automated Deployment:** GitHub Actions automates build, test, and deployment processes to ensure reliability and traceability.  
- **Security:** Traefik enforces HTTPS routing, and Django handles authentication and role-based access control.  
- **Scalability & Maintainability:** Each microservice can be updated or scaled independently without system downtime.  

<br>

**Technology Stack**  

| **Layer / Aspect**   | **Technology**   | **Purpose** |
|----------------------|------------------|-------------|
| **Frontend**         | React            | User interface and interaction |
| **Backend**          | Django (Python)  | Application logic and API gateway |
| **Proxy / Router**   | Traefik          | Reverse proxy, SSL termination, routing |
| **Containerization** | Docker           | Service packaging and isolation |
| **CI/CD Pipeline**   | GitHub Actions   | Automated build, test, and deployment |

<br>

**Design Justification**  
The chosen microservice-based architecture allows modular development and simplifies maintenance by separating concerns across independent components.  
Using Docker ensures consistent environments across development and production.  
Traefik dynamically routes requests between containers and provides secure HTTPS access.  
The GitHub Actions pipeline ensures continuous integration and deployment, improving code quality and deployment speed.  
*This design was selected to meet key stakeholder concerns regarding scalability, maintainability, and automation.*

<br><br>

## 4. Structural Views

*This section provides the structural architecture of the BaSyx DPP API.*  

It presents the system at different abstraction levels to illustrate how the main subsystems are organized and how their internal components collaborate.  
The Grey-Box view describes subsystem boundaries and interactions, while the White-Box view details internal components and class-level structure.

### 4.1. Grey-Box View

The BaSyx DPP API system is decomposed into multiple independent submodules to support scalability, maintainability, and modular development. Each submodule encapsulates a distinct responsibility and communicates through well-defined interfaces.  
This decomposition enables parallel development, reduces coupling, and allows individual servies to be deployed and scaled independently.

<br>

<img src="./src/grey-box-view/TINF24F_SAS_Team_6_Grey-Box-View_R10.drawio.svg" alt="BaSyx DPP API – Grey Box View" width="100%" height="100%">

*Figure 4-1 &mdash; Subsystem architecture overview of the BaSyx DPP (API), showing the central microservices and their integration points with existing BaSyx backend services.*

<br>

**Subsystem Responsibilities:**

| **Submodule**         | **Responsibility**                                                                 | **Technology**             |
|-----------------------|------------------------------------------------------------------------------------|----------------------------|
| React Frontend        | User interface rendering, interaction handling, form validation, HTTP request flow | React                      |
| Django Backend        | Business logic, request validation, domain mapping, REST endpoints                 | Django (Python)            |
| BaSyx Environment API | Provides access to persisted AAS-related product information and submodel data     | &ndash; (external service) |
| Traefik               | Routes incoming requests, performs SSL termination, and handles service discovery  | Traefik                    |

<br>

**Subsystem Collaboration**  

The React Frontend communicates with the Django Backend through RESTful HTTP requests routed via the Docker internal networking. 
The Django Backend retrieves product- and submodel-related information by querying the BaSyx Environment API, which internally connects to a MongoDB database. 
Traefik dynamically routes traffic based on container labels, ensuring request isolation and secure HTTPS access.

<br>

**Subsystem Boundaries**  

- Presentation concerns are strictly contained within the React Frontend.  
- Domain logic, schema mapping, and validation remain inside the Django Backend.  
- Persistence and AAS service interaction are delegated to the BaSyx Environment API.  
- Infrastructure-level routing and TLS termination are handled centrally by Traefik.

All interfaces are language-agnostic, promoting loose coupling and long-term interoperability.

<br>

**Rationale**

This subsystem decomposition addresses several stakeholder concerns:

- Scalability &mdash; services can scale independently based on workload  
- Maintainability &mdash; isolated responsibilities reduce change impact  
- Testability &mdash; each service can be validated separately in CI/CD  
- Extensibility &mdash; new submodules can be added without modifying existing ones

Furthermore, it aligns with the BaSyx microservice ecosystem, ensuring seamless integration with existing AAS infrastructure.

<br>

**Known Limitations**  

- The BaSyx Environment API is treated as a black-box dependency. Changes to its data schema may require adaptation layers in the backend.
- Runtime coupling exists with the BaSyx Environment availability; fallback strategies are limited in the current state.

<br>

### 4.2. White-Box View

==tbd==

<br><br>

## 5. Behavioral Views

### 5.1. Communication Diagram

<img src="./src/communication-diagram/TINF24F_SAS_Team_6_Communication-Diagram_R10.drawio.svg" alt="BaSyx DPP API – Communication Diagram" width="100%" height="100%">

*Figure 5-1 &mdash; Communication Diagram for the DPP Data Retrieval &ndash; Communication flow between user, frontend, Traefik, backend, and the BaSyx Environment API during a Digital Product Passport (DPP) request.*

<br>

The user triggers the request via the React frontend. The frontend calls the Django backend service using RESTful HTTP requests. 
*Traefik routes the request to the correct container based on service labels. (On server deployment)* The backend queries the BaSyx Environent API to retrieve AAS and submodel data and then maps the result to the internal DPP schema. 
The processed data is returned to the frontend as a JSON payload and finally rendered in the UI.

If the BaSyx Environment API is unavailable, the backend returns a descriptive error response.

<br>

### 5.2. Sequence Diagram

The following sequence diagram provides a detailed view of the runtime behavior of the system when a user requests Digital Product Passport (DPP) data.
It shows the chronological order of messages exchanged between the involved components and highlights the responsibilities of each subsystem during the request-response lifecycle.

<br>

<img src="./src/sequence-diagram/TINF24F_SAS_Team_6_Sequence-Diagram_R10.drawio.svg" alt="BaSyx DPP API – Sequence Diagram" width="100%" height="100%">

*Figure 5-2 &mdash; Sequence Diagram for DPP Data Retrieval &ndash; chronological interaction between the user, frontend, Traefik, backend, and the BaSyx Environment API during a Digital Product Passport (DPP) request.*

<br>

When a user initiates a DPP data request through the frontend, the React application sends an HTTP request to the Django backend service via the Traefik reverse proxy. Traefik forwards the request to the appropriate backend container based on routing rules.
The backend validates the request, queries the BaSyx Environment API for the required AAS and submodel information, and maps the received data to the internal DPP schema.
Once processing is complete, the backend returns the consolidated JSON response to the frontend, which then renders the corresponding product information to the user.

<br>

**Rationale**  

This sequence demonstrates a clear separation of concerns: user interaction and rendering are handled by the frontend, data orchestration and processing by the backend, and AAS data retrieval by the BaSyx Environment API. This separation improves maintainability, testability, and supports the modular microservice architecture.

<br>

**Known Limitations**  

If the BaSyx Environment API fails to respond, the backend returns an appropriate error message to the frontend to prevent partial or incorrect data from being displayed.

<br><br>

## 6. Data View

*This view provides an architectural understanding of how data is represented, transformed, and exchanged between system components and external services.*

### 6.1. Purpose and Stakeholder Concerns

**Purpose**  
The purpose of the Data View is to describe the structure, flow, ownership, and constraints of the data that is relevant to the Digital Product Passport (DPP) system.
It ensures transparency regarding data semantics and supports architectural decisions that impact maintainability, interoperability, and data integrity.

This view is architecturally relevant because the system relies on external data sources (BaSyx AAS infrastructure) and performs internal data mapping to deliver DPP-specific information to end users and API consumers. As a result, data consistency, interpretation, and transformation are central architectural concerns.

**Stakeholder Concerns**  

| **Stakeholder**        | **Data-Related Concerns** |
|------------------------|---------------------------|
| System Architect       | Data consistency, integration with AAS data models, clarity of data transformations, compliance with architectural principles |
| Product Manager        | Correctness and completeness of data presented to end users, alignment with DPP information requirements |
| Test Manager           | Traceability of data through the system, verifiable and predictable data transformations, ability to validate data flows |
| External API Consumers | Stability of data formats, versioning of schema, clear contract for data consumption |

<br>

### 6.2. Data Model and Data Flow

The system does not maintain its own persistent storage. Instead, it retrieves product-related information from the BaSyx AAS infrastructure and converts this data into a DPP-specific format. The conceptual model is centred around four core entities:

- **Asset Administration Shell (AAS):** Digital representation of a product and anchor point for related data.
- **Submodels:** Semantically grouped data sets under the AAS (e.g., product characteristics, identification, lifecycle).
- **DPP Data Object:** A structured representation derived from one or more Submodels, shaped into a DPP-aligned model for presentation and API exposure.
- **Product:** Logical domain reference used to request and interpret DPP information for a specific product instance.

<br>

**Data Flow Overview:**  

**1.** The frontend requests DPP data from the backend.
**2.** The backend queries the BaSyx Environment API for AAS and Submodel data.
**3.** The backend validates and maps this data into a unified DPP schema.
**4.** The processed result is returned as JSON to the frontend (and optionally external API consumers).

The backend acts as the sole integration and transformation point to ensure a consistent interpretation of data. No data modification or persistence occurs beyond runtime transformation for display or API output.

<br>

### 6.3. Data Formats, Constraints, and Key Decisions

Data exchanged across system boundaries is represented in JSON format for interoperability. Incoming data adheres to BaSyx AAS and Submodel conventions, while outgoing data follows a DPP-specific schema that abstracts away BaSyx internals. This prevents coupling between consumer applications and the underlying AAS model.

Key architectural decisions regarding data handling include:

- Use of BaSyx as the authoritative data source to avoid redundancy and ensure that the latest product information is always served.
- Centralization of mapping logic in the backend to prevent duplicated transformation logic and ensure testability, maintainability, and schema evolution control.
- JSON as a uniform data exchange standard to provide compatibility with web frontends and external API consumers.

Constraints influencing the architecture include the dependency on BaSyx data availability and schema stability, the requirement for valid DPP-relevant fields, and the need to provide clear error responses if incomplete data is received. Since no local persistence exists, offline access and historical comparison of product data are out of scope for the current architecture.
