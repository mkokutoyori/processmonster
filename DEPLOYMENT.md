# ProcessMonster Banking BPM - Deployment Guide

This guide covers deploying ProcessMonster Banking BPM using Docker, Docker Compose, and various cloud platforms.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Configuration](#configuration)
- [Docker Deployment](#docker-deployment)
- [Production Deployment](#production-deployment)
- [Cloud Platforms](#cloud-platforms)
- [Monitoring & Maintenance](#monitoring--maintenance)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

### Required Software

- **Docker**: Version 20.10 or higher
- **Docker Compose**: Version 2.0 or higher
- **Git**: For cloning the repository

### System Requirements

**Minimum (Development):**
- 4 GB RAM
- 2 CPU cores
- 20 GB disk space

**Recommended (Production):**
- 8 GB RAM
- 4 CPU cores
- 50 GB disk space
- SSD storage

---

## Quick Start

### 1. Clone Repository

```bash
git clone https://github.com/your-org/processmonster.git
cd processmonster
```

### 2. Initialize Environment

```bash
make init
# or
cp .env.example .env
```

### 3. Configure Environment Variables

Edit `.env` file with your configuration:

```bash
# Database
POSTGRES_DB=processmonster
POSTGRES_USER=processmonster_user
POSTGRES_PASSWORD=your_secure_password_here

# JWT
JWT_SECRET=your_very_secure_secret_key_minimum_256_bits
```

### 4. Deploy

**Development:**
```bash
make dev
# or
./deploy-docker.sh dev
```

**Production:**
```bash
make prod
# or
./deploy-docker.sh prod
```

### 5. Access Application

- **Frontend**: http://localhost
- **Backend API**: http://localhost:8080
- **API Documentation**: http://localhost:8080/swagger-ui.html

---

## Configuration

### Environment Variables

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `POSTGRES_DB` | Database name | processmonster | Yes |
| `POSTGRES_USER` | Database user | processmonster_user | Yes |
| `POSTGRES_PASSWORD` | Database password | changeme | Yes |
| `JWT_SECRET` | JWT signing key | - | Yes |
| `JWT_EXPIRATION` | Access token expiration (ms) | 86400000 | No |
| `SPRING_PROFILES_ACTIVE` | Spring profile | prod | No |
| `CORS_ALLOWED_ORIGINS` | CORS origins | http://localhost | Yes |
| `UPLOAD_MAX_FILE_SIZE` | Max file upload size | 10MB | No |

### Security Configuration

**Production Security Checklist:**

- [ ] Generate strong JWT secret (minimum 256 bits)
- [ ] Use strong database passwords (minimum 32 characters)
- [ ] Configure proper CORS origins
- [ ] Enable HTTPS/TLS
- [ ] Set up firewall rules
- [ ] Enable rate limiting
- [ ] Configure proper logging
- [ ] Set up monitoring and alerts

**Generate Secure JWT Secret:**

```bash
openssl rand -base64 64
```

---

## Docker Deployment

### Build Images

```bash
# Build all images
make build

# Or using docker-compose
docker-compose build
```

### Start Services

```bash
# Production
docker-compose up -d

# Development (with additional tools)
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d
```

### View Logs

```bash
# All services
make logs

# Specific service
make logs-backend
make logs-frontend
make logs-db
```

### Stop Services

```bash
make down
# or
docker-compose down
```

### Database Backup & Restore

**Backup:**
```bash
make backup-db
```

**Restore:**
```bash
make restore-db FILE=backup_20240101_120000.sql
```

---

## Production Deployment

### 1. Server Preparation

**Update system:**
```bash
sudo apt update && sudo apt upgrade -y
```

**Install Docker:**
```bash
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER
```

**Install Docker Compose:**
```bash
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
```

### 2. Clone & Configure

```bash
cd /opt
sudo git clone https://github.com/your-org/processmonster.git
cd processmonster
sudo cp .env.example .env
sudo nano .env  # Update with production values
```

### 3. Production Configuration

**Important Settings:**

```bash
# .env for production
SPRING_PROFILES_ACTIVE=prod
SPRING_JPA_DDL_AUTO=validate  # NEVER use 'update' in production
SPRING_JPA_SHOW_SQL=false
LOGGING_LEVEL_ROOT=WARN
LOGGING_LEVEL_APP=INFO

# Strong security
POSTGRES_PASSWORD=<64-char-random-password>
JWT_SECRET=<base64-256-bit-secret>

# Production URLs
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### 4. Deploy

```bash
sudo ./deploy-docker.sh prod
```

### 5. Setup Reverse Proxy (Nginx)

**Install Nginx:**
```bash
sudo apt install nginx -y
```

**Configure virtual host:**
```nginx
# /etc/nginx/sites-available/processmonster
server {
    listen 80;
    server_name yourdomain.com;

    location / {
        proxy_pass http://localhost:80;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Enable site:**
```bash
sudo ln -s /etc/nginx/sites-available/processmonster /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

### 6. Setup SSL with Let's Encrypt

```bash
sudo apt install certbot python3-certbot-nginx -y
sudo certbot --nginx -d yourdomain.com
```

### 7. Setup Systemd Service (Auto-start)

```bash
# /etc/systemd/system/processmonster.service
[Unit]
Description=ProcessMonster Banking BPM
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/processmonster
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
TimeoutStartSec=0

[Install]
WantedBy=multi-user.target
```

**Enable service:**
```bash
sudo systemctl enable processmonster
sudo systemctl start processmonster
```

---

## Cloud Platforms

### AWS (EC2 + RDS)

**1. Launch EC2 Instance:**
- AMI: Ubuntu 22.04 LTS
- Instance type: t3.medium (minimum)
- Security group: Allow ports 80, 443, 22

**2. Setup RDS PostgreSQL:**
- Engine: PostgreSQL 16
- Instance class: db.t3.micro (minimum)
- Storage: 20 GB SSD

**3. Configure environment:**
```bash
POSTGRES_HOST=your-rds-endpoint.rds.amazonaws.com
POSTGRES_PORT=5432
```

### Google Cloud (GCE + Cloud SQL)

**1. Create Compute Engine instance:**
```bash
gcloud compute instances create processmonster \
  --machine-type=n1-standard-2 \
  --image-family=ubuntu-2204-lts \
  --image-project=ubuntu-os-cloud
```

**2. Create Cloud SQL PostgreSQL:**
```bash
gcloud sql instances create processmonster-db \
  --database-version=POSTGRES_16 \
  --tier=db-f1-micro \
  --region=us-central1
```

### Azure (VM + Database for PostgreSQL)

**1. Create VM:**
```bash
az vm create \
  --resource-group processmonster-rg \
  --name processmonster-vm \
  --image Ubuntu2204 \
  --size Standard_B2s
```

**2. Create PostgreSQL:**
```bash
az postgres server create \
  --resource-group processmonster-rg \
  --name processmonster-db \
  --sku-name B_Gen5_1
```

### Docker Hub Registry

**1. Login:**
```bash
docker login
```

**2. Tag images:**
```bash
docker tag processmonster-backend:latest youruser/processmonster-backend:latest
docker tag processmonster-frontend:latest youruser/processmonster-frontend:latest
```

**3. Push:**
```bash
docker push youruser/processmonster-backend:latest
docker push youruser/processmonster-frontend:latest
```

---

## Monitoring & Maintenance

### Health Checks

```bash
# Check service health
make health

# Check individual services
curl http://localhost:8080/actuator/health
curl http://localhost/health
```

### Resource Monitoring

```bash
# Container stats
make stats

# Detailed monitoring
docker stats
```

### Log Management

```bash
# View logs
docker-compose logs -f --tail=100

# Export logs
docker-compose logs > logs_$(date +%Y%m%d).txt
```

### Backup Strategy

**Automated Daily Backups:**

```bash
# Create backup script
#!/bin/bash
BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)

# Database backup
docker-compose exec -T postgres pg_dump -U processmonster_user processmonster | gzip > "$BACKUP_DIR/db_$DATE.sql.gz"

# Keep last 7 days
find $BACKUP_DIR -name "db_*.sql.gz" -mtime +7 -delete
```

**Setup cron job:**
```bash
0 2 * * * /opt/processmonster/backup.sh
```

### Updates

```bash
# Pull latest changes
git pull origin main

# Rebuild images
docker-compose build

# Restart services
docker-compose down && docker-compose up -d
```

---

## Troubleshooting

### Container Won't Start

**Check logs:**
```bash
docker-compose logs backend
docker-compose logs frontend
```

**Check container status:**
```bash
docker-compose ps
```

### Database Connection Issues

**Test connection:**
```bash
docker-compose exec postgres psql -U processmonster_user -d processmonster -c "SELECT 1"
```

**Check network:**
```bash
docker network inspect processmonster_processmonster-network
```

### Port Conflicts

**Check port usage:**
```bash
sudo netstat -tulpn | grep :8080
sudo netstat -tulpn | grep :80
```

**Change ports in .env:**
```bash
BACKEND_PORT=8081
FRONTEND_PORT=8000
```

### Performance Issues

**Increase resources:**

Edit `docker-compose.yml`:
```yaml
services:
  backend:
    deploy:
      resources:
        limits:
          cpus: '2'
          memory: 2G
```

**Check resource usage:**
```bash
docker stats
```

### Out of Disk Space

**Clean up:**
```bash
# Remove unused containers, images, volumes
make clean-all

# Or manually
docker system prune -af --volumes
```

---

## Support

For issues and questions:
- **GitHub Issues**: https://github.com/your-org/processmonster/issues
- **Documentation**: https://docs.processmonster.com
- **Email**: support@processmonster.com

---

## License

ProcessMonster Banking BPM is proprietary software. All rights reserved.
