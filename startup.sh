#!/bin/bash

PRJ_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DOCKER_FRONTEND='0'
DOCKER_BACKEND='0'
RED='\033[0;31m'
YELLOW='\033[1;33m'
COLOR_OFF='\033[0m'

# Run the backend dependencies
echo -e "${YELLOW}Do you also want to start the backend dependencies (via Docker)?  (y/n) Default:n ${COLOR_OFF}"
read start_docker

if [ "$start_docker" = "y" ]; then
    DOCKER_BACKEND='1'
fi

if [ "$DOCKER_BACKEND" = "1" ]; then
    echo "Starting backend dependencies via Docker..."

    docker compose -f "$PRJ_ROOT/EXECUTABLE/scripts/docker-compose.backend.yml" up -d
    
    echo "Backend dependencies started."
    echo ""
    
    #echo "Starting backend dependencies via Docker... (to be done)"
fi

echo -e "${RED}If you want to start the backend in DEV mode, please execute the backend dev script afterwards in a separate terminal ${COLOR_OFF}"
echo ""


# Containerized vs dev frontend
echo -e "${YELLOW}Do you want to build and run the frontend in a container instead of starting the dev server?  (y/n) Default:n ${COLOR_OFF}"
read build_frontend_container

if [ "$build_frontend_container" = "y" ]; then
    DOCKER_FRONTEND='1'
fi
if [ "$DOCKER_FRONTEND" = "1" ]; then
    echo "Building and running the frontend in a container..."
    docker compose -f "$PRJ_ROOT/EXECUTABLE/scripts/docker-compose.frontend.yml" up -d
    echo "Frontend container started."
    exit 0
fi

docker stop basyx-aas-web-ui
# Start the frontend (dev)
echo ""
echo -e "${RED}!NOTE: On first startup, this may take a while${COLOR_OFF}"
echo "Starting frontend... (CTRL+C to stop)"

cd "$PRJ_ROOT/SOURCE/frontend/aas-web-ui"

yarn install
yarn dev

# Back to project root
cd $PRJ_ROOT