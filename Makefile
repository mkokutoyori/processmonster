.PHONY: help build up down restart logs clean test dev prod

# Default target
.DEFAULT_GOAL := help

# Colors
BLUE := \033[0;34m
GREEN := \033[0;32m
YELLOW := \033[1;33m
NC := \033[0m

## help: Display this help message
help:
	@echo "$(GREEN)ProcessMonster Banking BPM - Make Commands$(NC)"
	@echo ""
	@echo "$(BLUE)Available commands:$(NC)"
	@sed -n 's/^##//p' ${MAKEFILE_LIST} | column -t -s ':' | sed -e 's/^/ /'

## build: Build all Docker images
build:
	@echo "$(BLUE)Building Docker images...$(NC)"
	docker-compose build --no-cache

## up: Start all services in production mode
up:
	@echo "$(GREEN)Starting services in production mode...$(NC)"
	docker-compose up -d

## dev: Start all services in development mode
dev:
	@echo "$(GREEN)Starting services in development mode...$(NC)"
	docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
	@echo ""
	@echo "$(GREEN)Development environment ready!$(NC)"
	@echo "  Frontend: http://localhost:4200"
	@echo "  Backend: http://localhost:8080"
	@echo "  pgAdmin: http://localhost:5050"
	@echo "  MailHog: http://localhost:8025"

## prod: Start all services in production mode (alias for 'up')
prod: up

## down: Stop and remove all containers
down:
	@echo "$(YELLOW)Stopping all services...$(NC)"
	docker-compose down

## restart: Restart all services
restart: down up

## logs: View logs from all services
logs:
	docker-compose logs -f

## logs-backend: View logs from backend service
logs-backend:
	docker-compose logs -f backend

## logs-frontend: View logs from frontend service
logs-frontend:
	docker-compose logs -f frontend

## logs-db: View logs from database service
logs-db:
	docker-compose logs -f postgres

## ps: List all running containers
ps:
	docker-compose ps

## clean: Stop containers and remove volumes
clean:
	@echo "$(YELLOW)Cleaning up containers and volumes...$(NC)"
	docker-compose down -v
	docker system prune -f

## clean-all: Remove everything including images
clean-all:
	@echo "$(YELLOW)Removing all containers, volumes, and images...$(NC)"
	docker-compose down -v --rmi all
	docker system prune -af

## test: Run all tests
test: test-backend test-frontend

## test-backend: Run backend tests
test-backend:
	@echo "$(BLUE)Running backend tests...$(NC)"
	cd backend && mvn test

## test-frontend: Run frontend tests
test-frontend:
	@echo "$(BLUE)Running frontend tests...$(NC)"
	cd frontend && npm test

## shell-backend: Open shell in backend container
shell-backend:
	docker-compose exec backend sh

## shell-frontend: Open shell in frontend container
shell-frontend:
	docker-compose exec frontend sh

## shell-db: Open PostgreSQL shell
shell-db:
	docker-compose exec postgres psql -U processmonster_user -d processmonster

## backup-db: Backup database
backup-db:
	@echo "$(BLUE)Creating database backup...$(NC)"
	docker-compose exec -T postgres pg_dump -U processmonster_user processmonster > backup_$(shell date +%Y%m%d_%H%M%S).sql
	@echo "$(GREEN)Backup created!$(NC)"

## restore-db: Restore database from backup (usage: make restore-db FILE=backup.sql)
restore-db:
	@echo "$(BLUE)Restoring database from $(FILE)...$(NC)"
	docker-compose exec -T postgres psql -U processmonster_user -d processmonster < $(FILE)
	@echo "$(GREEN)Database restored!$(NC)"

## init: Initialize project (copy .env.example to .env)
init:
	@if [ ! -f .env ]; then \
		echo "$(BLUE)Creating .env file...$(NC)"; \
		cp .env.example .env; \
		echo "$(YELLOW)Please update .env with your configuration!$(NC)"; \
	else \
		echo "$(YELLOW).env file already exists$(NC)"; \
	fi

## deploy-dev: Deploy in development mode
deploy-dev:
	@./deploy-docker.sh dev

## deploy-prod: Deploy in production mode
deploy-prod:
	@./deploy-docker.sh prod

## stats: Show container resource usage
stats:
	docker stats --no-stream

## health: Check health of all services
health:
	@echo "$(BLUE)Checking service health...$(NC)"
	@docker-compose ps
	@echo ""
	@echo "$(BLUE)Backend health:$(NC)"
	@curl -s http://localhost:8080/actuator/health | jq . || echo "Backend not responding"
	@echo ""
	@echo "$(BLUE)Frontend health:$(NC)"
	@curl -s http://localhost/health || echo "Frontend not responding"
