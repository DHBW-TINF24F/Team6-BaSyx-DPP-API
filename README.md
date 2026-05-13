![GitHub](https://img.shields.io/github/license/eclipse-basyx/basyx-aas-web-ui) [![Build and Deploy Frontend](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_frontend.yml/badge.svg)](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_frontend.yml) [![Build and Deploy Backend](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_backend.yml/badge.svg)](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_backend.yml) [![Deploy Swagger Specification](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_swagger.yml/badge.svg)](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_swagger.yml)

# TINF24F_Team6_BaSyx_DPP_API

<p align="center">
  <a href="https://srv01.noah-becker.de/uni/swe/swagger/">Swagger</a> &bull;
  <a href="https://srv01.noah-becker.de/uni/swe/basyx/">BaSyx Web UI</a> &bull;
  <a href="https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/tree/main/PROJECT/MEETING_PROTOCOLS">Meeting Minutes</a> &bull;
  <a href="https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/blob/main/PROJECT/PRESENTATION/Team%206%20BaSyx%20DPP%20API.pptx">Presentation</a>
</p>
<hr>

<p align="center">
  <img src="./SOURCE/media/basyx_logo.png" alt="BaSyx Logo" height="60" />
  &nbsp;&nbsp;&nbsp;&nbsp;
  <img src="./SOURCE/media/dhbw.png" alt="DHBW Logo" height="60" />
</p>

<br>

<img src="./SOURCE/media/dpp_harting.png" alt="DPP Harting Example">

<br>

---

## Table of Contents

1. [Project Description](#project-description)
2. [Architecture](#architecture)
3. [Frontend Screenshots](#frontend-screenshots)
4. [Main Tasks](#main-tasks)
5. [Team Members](#team-members)
6. [Technologies & Tools](#technologies--tools)
7. [Documentation](#documentation)
8. [Useful Links](#useful-links)

---

## Project Description

This project implements a REST API for the Digital Product Passport (DPP) according to the [**DIN EN 18222**](https://www.dinmedia.de/en/draft-standard/din-en-18222/393321021) draft standard, integrated into the Eclipse BaSyx framework.

Our main task is to define an API according to the DIN standard and provide it through a backend service. As a potential release, we plan to offer a new DPP "BaSyx" Docker container. The core idea is to call existing BaSyx APIs — particularly the "Asset Administration Shell Repository API" — and map their responses to the required DIN-compliant output format.

In addition, we developed a completely independent frontend that is not directly connected to BaSyx. This frontend displays the DPPs of uploaded shells (AASX, JSON, etc.) in a well-structured and user-friendly way, accessible via the "AAS DPP Viewer" entry in the BaSyx Web UI.

---

## Architecture

The system follows a microservices (Docker) architecture, structured from general to specific:

```
End User / Developer
       │
       ▼
  Traefik (Reverse Proxy, HTTPS)
       │
  ┌────┴────┐
  │         │
Frontend  DPP API (Backend)
(Vue.js)  (Spring Boot)
              │
              ▼
      BaSyx Environment API
      (AAS Repository, Submodel Repository,
       AAS Registry, Discovery Service)
              │
              ▼
          MongoDB
```

| Layer | Technology | Purpose |
|-------|------------|---------|
| Presentation | Vue.js | User interface, DPP Viewer |
| Application | Spring Boot (Java) | Business logic, REST API |
| Data | MongoDB via BaSyx | Persistent AAS/Submodel storage |
| Infrastructure | Traefik, Docker, GitHub Actions | Routing, deployment, CI/CD |

For the full architectural specification, see the [SAS (Software Architecture Specification)](./PROJECT/SAS/TIN24F_SAS_Team_6_0v1.md).

---

## Frontend Screenshots

<!-- TODO: Add screenshots -->
> 📸 *Screenshots will be added here.*

<!-- Example:
<img src="./SOURCE/media/screenshots/dpp_viewer_overview.png" alt="DPP Viewer Overview" width="100%">
<img src="./SOURCE/media/screenshots/dpp_viewer_submodel.png" alt="DPP Viewer Submodel Detail" width="100%">
-->

---

## Main Tasks

1. **OpenAPI Specification**
   - [x] Derive a complete OpenAPI (Swagger) specification from [DIN EN 18222](https://www.dinmedia.de/en/draft-standard/din-en-18222/393321021)
   - [x] Ensure compliance and interoperability with BaSyx REST standards

2. **BaSyx Environment Setup**
   - [x] Install and configure a local BaSyx environment

3. **UI Analysis & Design**
   - [x] Analyze existing BaSyx and DPP UI solutions
   - [x] Define designs for the API frontend

4. **Development & Integration**
   - [x] Fork and modify required BaSyx repositories
   - [ ] Implement and test DPP API and UI components

5. **Deployment & Documentation**
   - [x] Host the DPP API and frontend on a public demo server
   - [x] Provide structured online documentation via GitHub Pages or BaSyx Wiki
   - [ ] Present the implementation for community acceptance in the BaSyx open-source project *(currently in discussion)*

---

## Team Members

| Role | Responsible Person |
|------|--------------------|
| Project Manager | Nataliia Chubak |
| Product Manager | Luca Schmoll, Magnus Lörcher |
| Test Manager | Manuel Lutz |
| System Architect | Noah Becker |
| Documentation | Fabian Steiß |
| UI Designer | Felix Schulz |
| Developer | All |

---

## Technologies & Tools

| Component | Technology |
|-----------|------------|
| **Backend** | Java / Spring Boot (BaSyx SDK) |
| **Frontend** | Vue.js (BaSyx UI) |
| **Infrastructure** | Eclipse BaSyx Framework |
| **API Definition** | OpenAPI 3.0 / Swagger |
| **Data Model** | Asset Administration Shell (AAS) |
| **Hosting** | Traefik (Reverse Proxy) & Docker — [Swagger](https://srv01.noah-becker.de/uni/swe/swagger/) · [BaSyx WebUI](https://srv01.noah-becker.de/uni/swe/basyx/) |
| **Documentation** | Markdown, GitHub Wiki, Swagger UI |

---

## Documentation

An overview of all project documents. The documents are cross-linked: the SRS references the CRS (use cases), the SAS references the SRS (requirements), and the User Documentation builds on the implemented frontend.

| Document | Description | Link |
|----------|-------------|------|
| **SRS** — Software Requirements Specification | Functional & non-functional requirements | [SRS](./PROJECT/SRS/SRS.md) |
| **SAS** — Software Architecture Specification | System architecture, diagrams, technical concepts | [SAS](./PROJECT/SAS/TIN24F_SAS_Team_6_0v1.md) |
| **CRS** — Customer Requirements Specification | Customer requirements & use cases | [CRS](./PROJECT/CRS/crs.md) |
| **MOD** — Module Documentation | Module descriptions & interfaces | [MOD](./PROJECT/MOD/) |
| **User Manual** | End-user guide for the DPP Viewer | [User Manual](./PROJECT/USER_MANUAL/usermanual.md) |
| **Developer README** | Setup, local development, backend & frontend | [dev README](./README.dev.md) |
| **Meeting Protocols** | All project meeting minutes | [Meeting Protocols](./PROJECT/MEETING_PROTOCOLS/) |
| **Presentation** | Final project presentation | [PowerPoint](./PROJECT/PRESENTATION/Team%206%20BaSyx%20DPP%20API.pptx) |
| **Swagger / OpenAPI** | Live API documentation (DIN EN 18222) | [Swagger UI](https://srv01.noah-becker.de/uni/swe/swagger/) |

---

## Useful Links

- [BaSyx Hack — Useful API information](https://basyxhack.iese.de/docs.html#gettingstarted)
- [AAS Web UI overview](https://wiki.basyx.org/en/latest/content/user_documentation/basyx_components/web_ui/index.html)
- [DIN EN 18222](https://www.dinmedia.de/en/draft-standard/din-en-18222/393321021)
- [Tutorials & Resources](https://github.com/DHBW-TINF24F/.github/blob/main/Tutorials.md)
- [Open Issues](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/issues)
- [Roadmap](https://github.com/orgs/DHBW-TINF24F/projects/9)