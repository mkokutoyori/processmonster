#!/bin/bash

# ProcessMonster Banking BPM - Docker Deployment Script
# Usage: ./deploy-docker.sh [environment]
# Environments: dev, prod

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
ENVIRONMENT=${1:-prod}
COMPOSE_FILE="docker-compose.yml"
PROJECT_NAME="processmonster"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}ProcessMonster Banking BPM Deployment${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""

# Check if .env file exists
if [ ! -f ".env" ]; then
    echo -e "${YELLOW}Warning: .env file not found!${NC}"
    echo -e "${YELLOW}Creating .env from .env.example...${NC}"
    cp .env.example .env
    echo -e "${RED}Please update .env with your configuration before proceeding!${NC}"
    exit 1
fi

# Validate environment
if [ "$ENVIRONMENT" != "dev" ] && [ "$ENVIRONMENT" != "prod" ]; then
    echo -e "${RED}Error: Invalid environment '$ENVIRONMENT'${NC}"
    echo -e "Usage: $0 [dev|prod]"
    exit 1
fi

echo -e "${GREEN}Environment: $ENVIRONMENT${NC}"
echo ""

# Add dev compose file if in dev environment
if [ "$ENVIRONMENT" = "dev" ]; then
    COMPOSE_FILE="$COMPOSE_FILE -f docker-compose.dev.yml"
    echo -e "${YELLOW}Development mode: Additional services will be started${NC}"
fi

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker is not running!${NC}"
    exit 1
fi

# Check if Docker Compose is available
if ! docker-compose version > /dev/null 2>&1; then
    echo -e "${RED}Error: Docker Compose is not installed!${NC}"
    exit 1
fi

echo -e "${BLUE}Step 1: Stopping existing containers...${NC}"
docker-compose -p $PROJECT_NAME down

echo ""
echo -e "${BLUE}Step 2: Building images...${NC}"
docker-compose -p $PROJECT_NAME -f $COMPOSE_FILE build --no-cache

echo ""
echo -e "${BLUE}Step 3: Starting services...${NC}"
if [ "$ENVIRONMENT" = "dev" ]; then
    docker-compose -p $PROJECT_NAME -f $COMPOSE_FILE up -d --remove-orphans
else
    docker-compose -p $PROJECT_NAME -f $COMPOSE_FILE up -d
fi

echo ""
echo -e "${BLUE}Step 4: Waiting for services to be healthy...${NC}"
sleep 10

# Check service health
echo ""
echo -e "${GREEN}Service Status:${NC}"
docker-compose -p $PROJECT_NAME ps

echo ""
echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment Complete!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo -e "${GREEN}Access the application:${NC}"
echo -e "  Frontend: ${BLUE}http://localhost${NC}"
echo -e "  Backend API: ${BLUE}http://localhost:8080${NC}"
echo -e "  API Docs: ${BLUE}http://localhost:8080/swagger-ui.html${NC}"

if [ "$ENVIRONMENT" = "dev" ]; then
    echo ""
    echo -e "${YELLOW}Development Tools:${NC}"
    echo -e "  pgAdmin: ${BLUE}http://localhost:5050${NC}"
    echo -e "  MailHog: ${BLUE}http://localhost:8025${NC}"
    echo -e "  Frontend Dev: ${BLUE}http://localhost:4200${NC}"
fi

echo ""
echo -e "${GREEN}Useful commands:${NC}"
echo -e "  View logs: ${BLUE}docker-compose -p $PROJECT_NAME logs -f [service]${NC}"
echo -e "  Stop: ${BLUE}docker-compose -p $PROJECT_NAME down${NC}"
echo -e "  Restart: ${BLUE}docker-compose -p $PROJECT_NAME restart [service]${NC}"
echo ""
