#!/bin/bash

# ProcessMonster Banking BPM - Heroku Deployment Script

echo "🚀 ProcessMonster Banking BPM - Heroku Deployment"
echo ""

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

# Check if Heroku CLI is installed
if ! command -v heroku &> /dev/null; then
    echo -e "${RED}❌ Heroku CLI not installed. Install from: https://devcenter.heroku.com/articles/heroku-cli${NC}"
    exit 1
fi

# Check if logged in
if ! heroku auth:whoami &> /dev/null; then
    echo -e "${YELLOW}⚠️  Not logged in to Heroku. Please login:${NC}"
    heroku login
fi

echo -e "${BLUE}📦 Building frontend...${NC}"
cd frontend
npm install
npm run build:prod
cd ..

echo -e "${BLUE}📦 Building backend...${NC}"
cd backend
mvn clean package -DskipTests
cd ..

echo -e "${BLUE}🔧 Checking Heroku app...${NC}"
APP_NAME=${1:-processmonster-banking-bpm}

if ! heroku apps:info --app $APP_NAME &> /dev/null; then
    echo -e "${YELLOW}App $APP_NAME doesn't exist. Creating...${NC}"
    heroku create $APP_NAME
    
    echo -e "${BLUE}Adding PostgreSQL addon...${NC}"
    heroku addons:create heroku-postgresql:mini --app $APP_NAME
    
    echo -e "${BLUE}Setting environment variables...${NC}"
    heroku config:set SPRING_PROFILES_ACTIVE=prod --app $APP_NAME
    heroku config:set JWT_SECRET=$(openssl rand -base64 64) --app $APP_NAME
fi

echo -e "${BLUE}🚀 Deploying to Heroku...${NC}"
git push heroku main

echo -e "${BLUE}🔍 Checking deployment status...${NC}"
heroku ps --app $APP_NAME

echo ""
echo -e "${GREEN}✅ Deployment complete!${NC}"
echo ""
echo "📍 Your app: https://$APP_NAME.herokuapp.com"
echo ""
echo "📊 View logs: heroku logs --tail --app $APP_NAME"
echo "🔧 Manage app: heroku open --app $APP_NAME"
echo ""
