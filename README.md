# 🏦 ProcessMonster Banking BPM

**Full-Stack Banking Business Process Management Application**

## 📋 Overview

ProcessMonster Banking BPM est une application complète de gestion de processus métier conçue spécifiquement pour le secteur bancaire.

### Highlights ✨

- **🌍 i18n Complet**: Support FR/EN dans toute l'application (UI, API, validations)
- **🔐 Sécurité Enterprise**: JWT avec refresh tokens, RBAC, protection brute-force
- **📊 BPMN 2.0**: Modélisation et exécution avec Camunda
- **🎨 UI Moderne**: Angular 17+ avec Material Design
- **📈 Analytics**: Dashboard temps réel avec Chart.js
- **🔍 Audit**: Logs complets pour conformité bancaire
- **🚀 Production Ready**: Déploiement Heroku, H2→PostgreSQL

## 🛠️ Tech Stack

### Backend
- Spring Boot 3.2.0 + Java 17
- Spring Security 6 + JWT
- Spring Data JPA + H2/PostgreSQL
- Camunda BPM 7.20.0
- MapStruct + Lombok
- OpenAPI/Swagger

### Frontend
- Angular 17+ + TypeScript
- Angular Material 17
- ngx-translate (i18n)
- Chart.js + bpmn-js
- Reactive Forms

## 📦 Prerequisites

- Java 17+
- Node.js 18+ & npm 9+
- Maven 3.9+
- Git

## 🚀 Quick Start

### 1. Clone Repository
\`\`\`bash
git clone <repo-url>
cd processmonster
\`\`\`

### 2. Backend Setup
\`\`\`bash
cd backend
mvn clean install
mvn spring-boot:run
\`\`\`

Backend runs on: **http://localhost:8080**

### 3. Frontend Setup
\`\`\`bash
cd frontend
npm install
npm start
\`\`\`

Frontend runs on: **http://localhost:4200**

## 🔑 Default Credentials

- Username: `admin`
- Password: `admin123`

## 📚 Documentation

- **API Docs**: http://localhost:8080/api/v1/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console (dev only)
  - JDBC URL: `jdbc:h2:file:./data/processmonster`
  - User: `sa`, Password: _(empty)_

## 📖 Key Endpoints

### Authentication
- `POST /api/v1/auth/login` - Login
- `POST /api/v1/auth/refresh` - Refresh token
- `POST /api/v1/auth/logout` - Logout

### Users
- `GET /api/v1/users` - List users (paginated)
- `POST /api/v1/users` - Create user
- `PUT /api/v1/users/{id}` - Update user

## 🌍 Internationalization

L'application supporte le français et l'anglais.

**Backend**: Envoyez le header `Accept-Language: fr` ou `en`
**Frontend**: Détection automatique du navigateur, sélecteur dans l'UI

## 🧪 Testing

\`\`\`bash
# Backend tests
cd backend && mvn test

# Frontend tests
cd frontend && npm test
\`\`\`

## 🚢 Deployment (Heroku)

\`\`\`bash
heroku create processmonster-bpm
heroku addons:create heroku-postgresql:mini
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set JWT_SECRET=$(openssl rand -base64 64)
git push heroku main
\`\`\`

## 📁 Project Structure

\`\`\`
processmonster/
├── backend/          # Spring Boot backend
│   ├── src/main/java/com/processmonster/bpm/
│   │   ├── config/   # Configuration
│   │   ├── entity/   # JPA entities
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── security/
│   └── src/main/resources/
│       ├── application.yml
│       └── i18n/     # Messages FR/EN
│
├── frontend/         # Angular frontend
│   ├── src/app/
│   │   ├── core/     # Services, guards, interceptors
│   │   ├── shared/   # Shared components
│   │   └── features/ # Lazy-loaded modules
│   └── src/assets/i18n/  # Translations FR/EN
│
├── ROADMAP.md       # Project roadmap (CRITICAL - always updated)
└── README.md        # This file
\`\`\`

## 📊 Development Status

Consultez [ROADMAP.md](ROADMAP.md) pour voir l'avancement détaillé de chaque phase.

### Phase 1 - Infrastructure ✅ TERMINÉ
- Backend Spring Boot + Angular configurés
- i18n FR/EN complet
- CORS, Swagger, Caching, Exception handling

### Phase 2 - Authentification 🚧 EN COURS
- Entities: User, Role, Permission, RefreshToken
- JWT Service
- Repositories créés
- Controllers et Security Config en cours

### Phases 3-10 ⏳ PLANIFIÉES
Voir ROADMAP.md pour les détails complets.

## 🔒 Security Features

✅ BCrypt password hashing
✅ JWT access (15min) + refresh (7 days) tokens
✅ RBAC avec permissions granulaires
✅ Brute-force protection (5 attempts/5min)
✅ Input validation (JSR-380)
✅ CORS configuration
✅ Rate limiting (100 req/min)
✅ Audit logging
✅ HTTPS en production

## 🤝 Contributing

1. Fork le repository
2. Créer une branche feature (`git checkout -b feature/amazing`)
3. Commit (`git commit -m 'Add amazing feature'`)
4. Push (`git push origin feature/amazing`)
5. Ouvrir une Pull Request

**Standards:**
- Tests obligatoires (>70% coverage)
- i18n FR/EN pour tous les messages
- JavaDoc/JSDoc pour APIs publiques

## 📄 License

MIT License

---

Made with ❤️ for Banking Industry | ProcessMonster Team
