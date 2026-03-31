![GitHub](https://img.shields.io/github/license/eclipse-basyx/basyx-aas-web-ui) [![Deploy Web UI (WIP)](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_webui.yml/badge.svg?branch=main)](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_webui.yml) [![Deploy Swagger Specification](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_swagger.yml/badge.svg)](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/actions/workflows/deploy_swagger.yml)

# TINF24F_Team6_BaSyx_DPP_API – Developer README

## Setup

For basic startup, follow the procedure with the `startup.sh` script in the base folder of the repository.

Run the script by typing `./startup.sh`

> [!INFO]
> If the script is not executable, ensure you are in the correct folder and also (on Linux/MacOS) run `chmod +x ./startup.sh`.

<br>

## Manual frontend dev startup

Navigate to `/SOURCE/frontend`

> [!INFO]
> Make sure you are in the correct folder!

1. Run `yarn install`
2. Run `yarn dev`

The startup process of the Vite Dev Setup should start.

> [!WARNING]
> At first startup, this may take a while.

<br>

## Backend dev setup

> [!IMPORTANT]
> To work with the backend, ensure you are turning the corresponding docker container <span style="text-decoration: underline">**off**</span>, so the local Maven dev server can hop on the port.

**Development Steps:**
1. `git checkout backend_test`
2. `cd EXECUTABLE/backend/dpp-backend/`
3. `mvn clean install`
4. `java -jar target/dpp-backend-0.0.1-SNAPSHOT.jar`

**Server runs at:** `http://localhost:8080`

**More information:** [Detailed README](https://github.com/DHBW-TINF24F/Team6-BaSyx-DPP-API/blob/backend_test/EXECUTABLE/backend/dpp-backend/README.md)