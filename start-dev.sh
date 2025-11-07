#!/bin/bash

# ProcessMonster Banking BPM - Development Startup Script
# This script starts both backend and frontend in development mode

echo "🏦 ProcessMonster Banking BPM - Starting Development Environment..."
echo ""

# Colors for output
GREEN='\033[0.32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Check prerequisites
echo "📋 Checking prerequisites..."

if ! command -v java &> /dev/null; then
    echo -e "${YELLOW}⚠️  Java is not installed. Please install Java 17+${NC}"
    exit 1
fi

if ! command -v mvn &> /dev/null; then
    echo -e "${YELLOW}⚠️  Maven is not installed. Please install Maven 3.9+${NC}"
    exit 1
fi

if ! command -v node &> /dev/null; then
    echo -e "${YELLOW}⚠️  Node.js is not installed. Please install Node.js 18+${NC}"
    exit 1
fi

echo -e "${GREEN}✅ All prerequisites installed${NC}"
echo ""

# Start backend
echo -e "${BLUE}🚀 Starting Backend (Spring Boot)...${NC}"
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../backend.log 2>&1 &
BACKEND_PID=$!
echo "Backend PID: $BACKEND_PID"
cd ..

# Wait a bit for backend to start
sleep 5

# Start frontend
echo -e "${BLUE}🎨 Starting Frontend (Angular)...${NC}"
cd frontend
npm start > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "Frontend PID: $FRONTEND_PID"
cd ..

echo ""
echo -e "${GREEN}✅ Application started successfully!${NC}"
echo ""
echo "📍 Access points:"
echo "   Frontend: http://localhost:4200"
echo "   Backend API: http://localhost:8080/api/v1"
echo "   Swagger UI: http://localhost:8080/api/v1/swagger-ui.html"
echo "   H2 Console: http://localhost:8080/h2-console"
echo ""
echo "🔑 Default credentials:"
echo "   Username: admin"
echo "   Password: admin123"
echo ""
echo "📝 Logs:"
echo "   Backend: tail -f backend.log"
echo "   Frontend: tail -f frontend.log"
echo ""
echo "⏹️  To stop: kill $BACKEND_PID $FRONTEND_PID"
echo ""
echo "🎉 Happy coding!"
