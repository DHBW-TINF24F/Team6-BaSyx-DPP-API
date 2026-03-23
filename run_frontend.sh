#!/bin/bash

PRJ_ROOT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
DOCKER_BACKEND='0'
YELLOW='\033[1;33m'
COLOR_OFF='\033[0m'

# Run the backend dependencies
echo -e "Do you also want to start the backend dependencies (via Docker)?  (y/n) Default:n ${ColorOff}"
read start_docker

if [ "$start_docker" = "y" ]; then
    DOCKER_BACKEND='1'
fi

if [ "$DOCKER_BACKEND" = "1" ]; then
    echo "Starting backend dependencies via Docker..."
    docker compose -f "$PRJ_ROOT/EXECUTABLE/scripts/docker-compose.backend.yml" up -d
    echo "Backend dependencies started."
fi


# Start the frontend
echo "Starting frontend... (CTRL+C to stop)"
echo -e "${YELLOW}!NOTE: On first startup, this may take a while${COLOR_OFF}"

cd "$PRJ_ROOT/SOURCE/frontend/aas-web-ui"

yarn install
yarn dev