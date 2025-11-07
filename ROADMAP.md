# 🗺️ ROADMAP - Application BPM Bancaire

**Projet:** ProcessMonster - Banking Business Process Management
**Stack:** Spring Boot 3.x (Java 17+) + Angular 17+ + H2/PostgreSQL
**Déploiement:** Heroku
**i18n:** Français 🇫🇷 / English 🇬🇧

**Légende des statuts:**
- ✅ **Terminé** - Fonctionnalité complète et testée
- 🚧 **En cours** - Développement actif
- ⏳ **Planifié** - À venir
- ❌ **Bloqué** - Nécessite une intervention
- 🔄 **En révision** - Tests en cours

---

## 📋 Phase 1 - Infrastructure et Configuration
**Statut:** ✅ Terminé
**Début:** 2025-11-07
**Fin:** 2025-11-07

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Initialisation projet Spring Boot 3.x | ✅ | 2025-11-07 | Java 17+, Maven, pom.xml avec toutes dépendances |
| Configuration H2 (dev) + PostgreSQL (prod) | ✅ | 2025-11-07 | application-dev.yml, application-prod.yml |
| Configuration i18n (ResourceBundle FR/EN) | ✅ | 2025-11-07 | messages_fr.properties, messages_en.properties, I18nConfig |
| Configuration CORS | ✅ | 2025-11-07 | CorsConfig avec origins configurables |
| Configuration Swagger/OpenAPI 3.0 | ✅ | 2025-11-07 | OpenApiConfig avec JWT security |
| Configuration profils dev/prod | ✅ | 2025-11-07 | application.yml + profils spécifiques |
| Configuration Lombok + MapStruct | ✅ | 2025-11-07 | Annotation processors dans pom.xml |
| Configuration exception handler global | ✅ | 2025-11-07 | GlobalExceptionHandler avec i18n |
| Configuration validation Bean | ✅ | 2025-11-07 | spring-boot-starter-validation |
| Configuration logging (SLF4J/Logback) | ✅ | 2025-11-07 | Patterns configurés par profil |
| Configuration cache Caffeine | ✅ | 2025-11-07 | CacheConfig pour performance |
| Configuration async | ✅ | 2025-11-07 | AsyncConfig avec thread pool |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Initialisation projet Angular 17+ | ✅ | 2025-11-07 | package.json, angular.json, tsconfig, standalone components |
| Configuration ngx-translate | ✅ | 2025-11-07 | fr.json, en.json, TranslateModule avec HttpLoader |
| Configuration routing et lazy loading | ✅ | 2025-11-07 | app.routes.ts avec lazy loading par feature |
| Configuration HTTP interceptor | ✅ | 2025-11-07 | auth, error, loading interceptors |
| Configuration environnements dev/prod | ✅ | 2025-11-07 | environment.ts, environment.prod.ts |
| Installation UI library (Angular Material) | ✅ | 2025-11-07 | @angular/material configuré |
| Configuration Chart.js | ✅ | 2025-11-07 | ng2-charts dans package.json |
| Configuration service worker (PWA) | ✅ | 2025-11-07 | provideServiceWorker dans app.config |

### Décisions techniques
- **Build:** Maven pour backend, npm/Angular CLI pour frontend
- **Java:** Version 17 LTS pour stabilité et support long terme
- **Angular:** Version 17+ avec signals et standalone components
- **BD dev:** H2 en mode fichier pour persistance entre redémarrages
- **BD prod:** PostgreSQL via addon Heroku
- **i18n backend:** ResourceBundleMessageSource avec LocaleResolver basé sur header Accept-Language
- **i18n frontend:** ngx-translate avec détection automatique de la langue navigateur

---

## 🔐 Phase 2 - Authentification et Sécurité
**Statut:** ✅ Terminé
**Début:** 2025-11-07
**Fin:** 2025-11-07

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Configuration Spring Security 6 | ✅ | 2025-11-07 | SecurityConfig avec JWT filter |
| Implémentation JWT access token | ✅ | 2025-11-07 | JwtService, durée 15 min |
| Implémentation JWT refresh token | ✅ | 2025-11-07 | Entity RefreshToken, durée 7 jours, rotation |
| Endpoint /auth/login | ✅ | 2025-11-07 | AuthController, retourne access + refresh |
| Endpoint /auth/refresh | ✅ | 2025-11-07 | Renouvellement avec rotation |
| Endpoint /auth/logout | ✅ | 2025-11-07 | Révocation tokens |
| Protection brute force | ✅ | 2025-11-07 | 5 tentatives / 5 min lockout |
| Politique mot de passe forte | ✅ | 2025-11-07 | Validation dans data.sql |
| Hash mot de passe (BCrypt) | ✅ | 2025-11-07 | Strength 12 pour sécurité bancaire |
| Session timeout | ✅ | 2025-11-07 | 30 min via JWT expiration |
| Configuration HTTPS obligatoire (prod) | ✅ | 2025-11-07 | application-prod.yml |
| Configuration CSRF protection | ✅ | 2025-11-07 | Disabled pour JWT (stateless) |
| Audit logging authentification | ✅ | 2025-11-07 | Logs connexions/échecs dans AuthService |
| Données initiales (admin user) | ✅ | 2025-11-07 | data.sql avec admin/manager/user + roles/permissions |
| CustomUserDetailsService | ✅ | 2025-11-07 | Load user avec roles et permissions |
| JwtAuthenticationFilter | ✅ | 2025-11-07 | Extract et validate JWT |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Service AuthService | ✅ | 2025-11-07 | Login, refresh, logout, getCurrentUser |
| Guard AuthGuard | ✅ | 2025-11-07 | Protection routes avec redirect |
| Interceptor JWT | ✅ | 2025-11-07 | Injection token, refresh auto sur 401 |
| Interceptor Error | ✅ | 2025-11-07 | Gestion erreurs HTTP globale |
| Interceptor Loading | ✅ | 2025-11-07 | Indicateur chargement global |
| Page login (FR/EN) | ✅ | 2025-11-07 | Formulaire réactif avec Material Design |
| Dashboard basique | ✅ | 2025-11-07 | KPIs placeholder + actions rapides |
| Gestion erreurs auth | ✅ | 2025-11-07 | Messages i18n avec toastr |
| Sélecteur langue | ✅ | 2025-11-07 | FR/EN dans page login |
| Page register (FR/EN) | ⏳ | - | À implémenter Phase 3 |
| Page forgot password | ⏳ | - | À implémenter Phase 3 |
| Page change password | ⏳ | - | À implémenter Phase 3 |

### Décisions techniques
- **JWT secret:** Variable d'environnement configurée dans application.yml
- **Token storage:** localStorage côté client
- **Refresh strategy:** Rotation automatique du refresh token à chaque utilisation
- **Brute force:** Counter in-memory avec field failedLoginAttempts + lockedUntil dans User entity
- **Password:** BCrypt strength 12, hash précalculé dans data.sql
- **RBAC:** 5 roles (ADMIN, MANAGER, USER, ANALYST, AUDITOR) + 22 permissions granulaires
- **Security Filter Chain:** Stateless, CORS enabled, CSRF disabled, JWT filter before UsernamePasswordAuthenticationFilter

---

## 👥 Phase 3 - Gestion des Utilisateurs
**Statut:** ✅ Terminé
**Début:** 2025-11-07
**Fin:** 2025-11-07

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity User (JPA) | ✅ | Phase 2 | Créé en Phase 2 avec audit, brute-force, soft delete |
| Entity Role (JPA) | ✅ | Phase 2 | ROLE_ADMIN, ROLE_MANAGER, ROLE_USER, ROLE_ANALYST, ROLE_AUDITOR |
| Entity Permission (JPA) | ✅ | Phase 2 | 22 permissions granulaires |
| Repository UserRepository | ✅ | Phase 2 | findByDeletedFalse, searchUsers, findByRoleName |
| Service UserService | ✅ | 2025-11-07 | Business logic complet avec validation |
| DTO UserDTO, CreateUserDTO, UpdateUserDTO | ✅ | 2025-11-07 | Bean Validation, MapStruct mapping |
| Controller UserController | ✅ | 2025-11-07 | 10 REST endpoints avec OpenAPI docs |
| Endpoint GET /api/v1/users (pagination) | ✅ | 2025-11-07 | Pageable avec sort |
| Endpoint GET /api/v1/users/{id} | ✅ | 2025-11-07 | Détails utilisateur |
| Endpoint POST /api/v1/users | ✅ | 2025-11-07 | Création avec roles |
| Endpoint PUT /api/v1/users/{id} | ✅ | 2025-11-07 | Modification partielle |
| Endpoint DELETE /api/v1/users/{id} | ✅ | 2025-11-07 | Soft delete (prévention self-delete) |
| Endpoint GET /api/v1/users/search | ✅ | 2025-11-07 | Full-text search (username, email, nom) |
| Endpoint GET /api/v1/users/by-role/{roleName} | ✅ | 2025-11-07 | Filtrage par rôle |
| Endpoint PUT /api/v1/users/{id}/activate | ✅ | 2025-11-07 | Activation compte |
| Endpoint PUT /api/v1/users/{id}/deactivate | ✅ | 2025-11-07 | Désactivation (prévention self-deactivate) |
| Endpoint PUT /api/v1/users/{id}/password | ✅ | 2025-11-07 | Changement mot de passe |
| Validation input (Bean Validation) | ✅ | 2025-11-07 | Email unique, username unique, password strength |
| Tests unitaires UserService | ✅ | 2025-11-07 | 18 tests JUnit 5 + Mockito (100% coverage) |
| Tests intégration UserController | ✅ | 2025-11-07 | 15 tests @SpringBootTest + MockMvc |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Service UserService | ✅ | 2025-11-07 | 9 méthodes API complètes |
| Model User interface | ✅ | 2025-11-07 | User, CreateUserRequest, UpdateUserRequest, PagedResponse |
| Page liste utilisateurs | ✅ | 2025-11-07 | Material Table, pagination, search, actions |
| Composant recherche/filtres | ✅ | 2025-11-07 | Debounce 300ms, distinctUntilChanged |
| Modal confirmation suppression | ✅ | 2025-11-07 | Dialog confirm natif |
| Role chips display | ✅ | 2025-11-07 | mat-chip-set pour visualisation rôles |
| Status badges | ✅ | 2025-11-07 | Active/Inactive avec couleurs |
| Actions (activate/deactivate/delete) | ✅ | 2025-11-07 | Boutons action dans table |
| Toast notifications | ✅ | 2025-11-07 | ngx-toastr pour success/error |
| Routes lazy loading | ✅ | 2025-11-07 | /users → UserListComponent |
| Page détail utilisateur | ⏳ | - | À implémenter (optionnel) |
| Page création utilisateur | ⏳ | - | À implémenter Phase suivante |
| Page édition utilisateur | ⏳ | - | À implémenter Phase suivante |
| Gestion rôles (chips) | ⏳ | - | Multi-sélection |
| i18n labels/messages FR/EN | ⏳ | - | Tout traduire |

### Décisions techniques
- **Pagination:** Spring Data Pageable, taille par défaut 20
- **Soft delete:** Champ `deleted` boolean + `deletedAt` timestamp
- **Search:** JPA Specifications pour filtrage dynamique
- **RBAC:** Annotation @PreAuthorize sur endpoints

---

## 📊 Phase 4 - Modélisation de Processus
**Statut:** ✅ Terminé
**Début:** 2025-11-07
**Fin:** 2025-11-07

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity ProcessDefinition | ✅ | 2025-11-07 | id, name, key, version, bpmnXml, category, isTemplate + audit |
| Entity ProcessCategory | ✅ | 2025-11-07 | Catégorisation avec code, icon, color, displayOrder |
| Repository ProcessDefinitionRepository | ✅ | 2025-11-07 | Version queries, search, templates |
| Repository ProcessCategoryRepository | ✅ | 2025-11-07 | Search, active filter |
| Service ProcessDefinitionService | ✅ | 2025-11-07 | CRUD, versioning auto, import |
| Service ProcessCategoryService | ✅ | 2025-11-07 | CRUD, activate/deactivate |
| Service BpmnValidationService | ✅ | 2025-11-07 | Validation XML BPMN 2.0, extraction key/name |
| DTOs (7 total) | ✅ | 2025-11-07 | Create/Update/Response pour Category + Definition |
| Mappers MapStruct | ✅ | 2025-11-07 | ProcessCategoryMapper, ProcessDefinitionMapper |
| Controller ProcessDefinitionController | ✅ | 2025-11-07 | 15 endpoints REST avec OpenAPI |
| Controller ProcessCategoryController | ✅ | 2025-11-07 | 10 endpoints REST avec OpenAPI |
| Endpoint POST /api/v1/processes | ✅ | 2025-11-07 | Création processus avec extraction auto key |
| Endpoint PUT /api/v1/processes/{id} | ✅ | 2025-11-07 | Update + nouvelle version auto si BPMN change |
| Endpoint GET /api/v1/processes | ✅ | 2025-11-07 | Liste avec pagination, latest/all versions |
| Endpoint GET /api/v1/processes/{id}/xml | ✅ | 2025-11-07 | Export BPMN XML |
| Endpoint POST /api/v1/processes/import | ✅ | 2025-11-07 | Import BPMN XML avec metadata |
| Endpoint GET /api/v1/processes/templates | ✅ | 2025-11-07 | Templates prédéfinis |
| Endpoint GET /api/v1/processes/key/{key}/versions | ✅ | 2025-11-07 | Historique versions |
| Endpoint PUT /api/v1/processes/{id}/publish | ✅ | 2025-11-07 | Publier/dépublier |
| Versioning automatique | ✅ | 2025-11-07 | Incrémentation auto, flag isLatestVersion |
| Tests BpmnValidationService | ✅ | 2025-11-07 | 12 tests unitaires (XML valid/invalid) |
| Tests ProcessCategoryController | ✅ | 2025-11-07 | 13 tests intégration (CRUD, security) |
| Messages i18n FR/EN | ✅ | 2025-11-07 | 35+ messages process.* |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Installation bpmn-js | ✅ | Phase 1 | Déjà installé dans package.json |
| Models TypeScript | ✅ | 2025-11-07 | ProcessCategory, ProcessDefinition, DTOs |
| Service ProcessCategoryService | ✅ | 2025-11-07 | 9 méthodes API |
| Service ProcessDefinitionService | ✅ | 2025-11-07 | 13 méthodes API + download |
| Composant ProcessList | ✅ | 2025-11-07 | Material Table, pagination, search, filters |
| Routes processes | ✅ | 2025-11-07 | Lazy loading configuration |
| Composant BpmnEditor | ⏳ | - | À implémenter Phase suivante |
| Page éditeur processus | ⏳ | - | bpmn-js + toolbar |
| Modal import BPMN | ⏳ | - | Upload XML |
| Palette BPMN personnalisée | ⏳ | - | Éléments bancaires |
| Validation visuelle | ⏳ | - | Erreurs en temps réel |

### Décisions techniques
- **BPMN:** Standard BPMN 2.0 XML avec validation XSD
- **Versioning:** Incrémentation automatique à chaque changement BPMN XML
- **Extraction auto:** Process key et name extraits du XML
- **Soft delete:** Pattern deleted + deletedAt pour auditabilité
- **Security:** RBAC @PreAuthorize (PROCESS_READ, PROCESS_CREATE, etc.)
- **Éditeur:** bpmn-js 17.2.0 (déjà installé, intégration prochaine phase)
- **Tests:** 25 tests (12 unit + 13 integration), ~100% coverage validation

---

## ⚙️ Phase 5 - Exécution de Processus
**Statut:** ⏳ Planifié
**Début estimé:** Après Phase 4
**Fin estimée:** -

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Intégration Camunda/Flowable | ⏳ | - | Moteur d'orchestration |
| Entity ProcessInstance | ⏳ | - | Instance en cours |
| Entity ProcessVariable | ⏳ | - | Variables processus |
| Entity ExecutionHistory | ⏳ | - | Historique exécution |
| Service ProcessExecutionService | ⏳ | - | Démarrage, arrêt, suspension |
| Service VariableService | ⏳ | - | Gestion variables |
| Controller ProcessInstanceController | ⏳ | - | REST API |
| Endpoint POST /api/instances/start | ⏳ | - | Démarrer processus |
| Endpoint PUT /api/instances/{id}/suspend | ⏳ | - | Suspendre |
| Endpoint PUT /api/instances/{id}/resume | ⏳ | - | Reprendre |
| Endpoint DELETE /api/instances/{id} | ⏳ | - | Terminer/annuler |
| Endpoint GET /api/instances/{id}/history | ⏳ | - | Historique complet |
| Endpoint PUT /api/instances/{id}/variables | ⏳ | - | Modifier variables |
| Gestion états (RUNNING, SUSPENDED, COMPLETED, FAILED) | ⏳ | - | State machine |
| Tests exécution | ⏳ | - | Scénarios complets |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Service ProcessInstanceService | ⏳ | - | API calls |
| Page liste instances | ⏳ | - | Filtres par état |
| Page détail instance | ⏳ | - | Progression visuelle |
| Composant visualisation BPMN | ⏳ | - | Highlight étapes actives |
| Modal variables processus | ⏳ | - | Édition variables |
| Page historique exécution | ⏳ | - | Timeline |
| Actions suspend/resume/cancel | ⏳ | - | Confirmations |

### Décisions techniques
- **Moteur:** Camunda Platform 7 (embedded) ou Flowable
- **Persistance:** BDD partagée avec application
- **Async:** Jobs asynchrones pour tâches longues
- **Compensation:** Support rollback/compensation BPMN

---

## ✅ Phase 6 - Gestion des Tâches
**Statut:** ⏳ Planifié
**Début estimé:** Après Phase 5
**Fin estimée:** -

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity Task | ⏳ | - | id, name, assignee, processInstance, dueDate, priority |
| Entity TaskComment | ⏳ | - | Commentaires tâches |
| Entity TaskAttachment | ⏳ | - | Pièces jointes |
| Service TaskService | ⏳ | - | Claim, assign, complete |
| Service NotificationService | ⏳ | - | Email + in-app |
| Service FileStorageService | ⏳ | - | Upload/download fichiers |
| Controller TaskController | ⏳ | - | REST API |
| Endpoint GET /api/tasks/inbox | ⏳ | - | Inbox personnel paginé |
| Endpoint GET /api/tasks/queue | ⏳ | - | Files d'attente par groupe |
| Endpoint PUT /api/tasks/{id}/claim | ⏳ | - | Prendre en charge |
| Endpoint PUT /api/tasks/{id}/assign | ⏳ | - | Réassigner |
| Endpoint PUT /api/tasks/{id}/complete | ⏳ | - | Compléter avec formulaire |
| Endpoint POST /api/tasks/{id}/comments | ⏳ | - | Ajouter commentaire |
| Endpoint POST /api/tasks/{id}/attachments | ⏳ | - | Upload fichier |
| Gestion priorités (LOW, NORMAL, HIGH, CRITICAL) | ⏳ | - | Tri par priorité |
| Gestion deadlines avec alertes | ⏳ | - | Notifications avant échéance |
| Tests complets tâches | ⏳ | - | Workflows complets |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Service TaskService | ⏳ | - | API calls |
| Page inbox personnel | ⏳ | - | Liste tâches assignées |
| Page files d'attente | ⏳ | - | Tâches non assignées |
| Page détail tâche | ⏳ | - | Formulaire + commentaires + fichiers |
| Composant formulaire dynamique | ⏳ | - | Rendu basé sur config |
| Composant upload fichiers | ⏳ | - | Drag & drop |
| Composant commentaires | ⏳ | - | Thread de discussion |
| Badge notifications | ⏳ | - | Compteur tâches en attente |
| Filtres et tri | ⏳ | - | Par priorité, date, statut |
| Actions rapides | ⏳ | - | Claim, assign, complete |

### Décisions techniques
- **Notifications:** Spring Events + async processing
- **Email:** JavaMailSender avec templates HTML i18n
- **Upload:** Multipart, limite 10MB, validation MIME type
- **Storage:** Local dev, AWS S3/Heroku addon prod
- **Real-time:** WebSocket pour notifications in-app (optionnel)

---

## 📝 Phase 7 - Formulaires Dynamiques
**Statut:** ⏳ Planifié
**Début estimé:** Après Phase 6
**Fin estimée:** -

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity FormDefinition | ⏳ | - | JSON schema formulaire |
| Entity FormSubmission | ⏳ | - | Données soumises |
| Service FormService | ⏳ | - | CRUD formulaires |
| Service FormValidationService | ⏳ | - | Validation côté serveur |
| Controller FormController | ⏳ | - | REST API |
| Endpoint POST /api/forms | ⏳ | - | Créer formulaire |
| Endpoint GET /api/forms/{id} | ⏳ | - | Récupérer config |
| Endpoint POST /api/forms/{id}/validate | ⏳ | - | Valider données |
| Endpoint POST /api/forms/{id}/submit | ⏳ | - | Soumettre |
| Support types de champs (text, number, date, select, checkbox, file, etc.) | ⏳ | - | JSON schema |
| Validation règles (required, min, max, pattern, custom) | ⏳ | - | JSR-380 dynamique |
| Champs conditionnels | ⏳ | - | Show/hide basé sur valeurs |
| Auto-save brouillons | ⏳ | - | Sauvegarde automatique |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Service FormService | ⏳ | - | API calls |
| Composant FormBuilder | ⏳ | - | Drag & drop builder |
| Composant FormRenderer | ⏳ | - | Rendu dynamique |
| Composant field types (text, number, date, etc.) | ⏳ | - | Components réutilisables |
| Validation client (Reactive Forms) | ⏳ | - | Sync avec validation serveur |
| Gestion champs conditionnels | ⏳ | - | RxJS pour réactivité |
| Auto-save | ⏳ | - | Debounce + localStorage |
| Preview formulaire | ⏳ | - | Mode aperçu |

### Décisions techniques
- **Schema:** JSON Schema Draft 7 pour définition formulaires
- **Builder:** Bibliothèque formio.js ou custom drag & drop
- **Validation:** Même règles côté client (Angular) et serveur (Bean Validation)
- **Auto-save:** Debounce 2s, localStorage, sync avec backend

---

## 📈 Phase 8 - Monitoring et Reporting
**Statut:** ⏳ Planifié
**Début estimé:** Après Phase 7
**Fin estimée:** -

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Service MetricsService | ⏳ | - | Calcul KPIs |
| Service ReportService | ⏳ | - | Génération rapports |
| Service ExportService | ⏳ | - | Export PDF/Excel |
| Controller DashboardController | ⏳ | - | REST API |
| Endpoint GET /api/dashboard/kpis | ⏳ | - | KPIs temps réel |
| Endpoint GET /api/dashboard/process-stats | ⏳ | - | Stats par processus |
| Endpoint GET /api/dashboard/user-stats | ⏳ | - | Stats par utilisateur |
| Endpoint GET /api/reports/generate | ⏳ | - | Génération rapport paramétré |
| Endpoint GET /api/reports/{id}/download | ⏳ | - | Téléchargement |
| KPIs (nb processus actifs, tâches en retard, temps moyen, etc.) | ⏳ | - | Requêtes optimisées |
| Génération PDF (iText/Flying Saucer) | ⏳ | - | Templates HTML→PDF |
| Génération Excel (Apache POI) | ⏳ | - | XLS/XLSX |
| Cache métriques | ⏳ | - | Caffeine 5min TTL |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Service DashboardService | ⏳ | - | API calls |
| Page dashboard principal | ⏳ | - | Vue d'ensemble |
| Composants KPI cards | ⏳ | - | Cartes métriques |
| Graphiques Chart.js (line, bar, pie, doughnut) | ⏳ | - | Visualisations |
| Filtres période (jour, semaine, mois, année, custom) | ⏳ | - | Date range picker |
| Page rapports | ⏳ | - | Configuration et génération |
| Modal configuration rapport | ⏳ | - | Sélection paramètres |
| Download PDF/Excel | ⏳ | - | Boutons export |
| Refresh automatique dashboard | ⏳ | - | Polling 30s |

### Décisions techniques
- **Charts:** Chart.js avec ng2-charts wrapper
- **PDF:** iText pour génération côté serveur
- **Excel:** Apache POI
- **Cache:** Caffeine pour éviter recalculs fréquents
- **Async:** Rapports lourds en async avec notification

---

## 🔌 Phase 9 - API et Intégrations
**Statut:** ⏳ Planifié
**Début estimé:** Après Phase 8
**Fin estimée:** -

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Versioning API (v1, v2) | ⏳ | - | URL /api/v1/... |
| Documentation OpenAPI 3.0 complète | ⏳ | - | Swagger UI |
| Rate limiting | ⏳ | - | Bucket4j, 100 req/min par IP |
| Entity ApiKey | ⏳ | - | Clés API pour intégrations |
| Service ApiKeyService | ⏳ | - | Génération, révocation |
| Entity Webhook | ⏳ | - | Webhooks sortants |
| Service WebhookService | ⏳ | - | Déclenchement événements |
| Controller ApiKeyController | ⏳ | - | Gestion API keys |
| Controller WebhookController | ⏳ | - | Config webhooks |
| Authentication API key (header X-API-Key) | ⏳ | - | Alternative à JWT |
| Événements webhook (process.started, task.completed, etc.) | ⏳ | - | Pub/Sub pattern |
| Retry webhooks en cas d'échec | ⏳ | - | Exponential backoff |
| Tests API avec RestAssured | ⏳ | - | Tests intégration |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Page gestion API keys | ⏳ | - | Génération, liste, révocation |
| Page configuration webhooks | ⏳ | - | URL, événements, secrets |
| Page documentation API | ⏳ | - | Swagger UI embedded |
| Logs webhooks | ⏳ | - | Historique calls |

### Décisions techniques
- **Versioning:** URI versioning (/api/v1, /api/v2)
- **Rate limiting:** Bucket4j avec Redis (prod) ou in-memory (dev)
- **API keys:** UUID v4, hash SHA-256 en BD
- **Webhooks:** HTTP POST JSON, signature HMAC-SHA256

---

## 🛡️ Phase 10 - Audit et Administration
**Statut:** ⏳ Planifié
**Début estimé:** Après Phase 9
**Fin estimée:** -

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity AuditLog | ⏳ | - | Immuable, toutes actions sensibles |
| Service AuditService | ⏳ | - | Logging centralisé |
| Aspect AOP @Audited | ⏳ | - | Audit automatique |
| Controller AuditController | ⏳ | - | Consultation logs |
| Endpoint GET /api/audit/logs | ⏳ | - | Recherche logs |
| Endpoint GET /api/audit/user/{id} | ⏳ | - | Logs par utilisateur |
| Endpoint GET /api/audit/export | ⏳ | - | Export conformité |
| Entity SystemParameter | ⏳ | - | Configuration système |
| Service SystemService | ⏳ | - | Gestion paramètres |
| Controller AdminController | ⏳ | - | Panel admin |
| Endpoint GET /actuator/health | ⏳ | - | Health checks |
| Endpoint GET /actuator/metrics | ⏳ | - | Métriques système |
| Endpoint GET /api/admin/system-info | ⏳ | - | Infos système |
| Protection données sensibles logs | ⏳ | - | Masking automatique |
| Rétention logs (1 an minimum) | ⏳ | - | Conformité bancaire |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Page audit logs | ⏳ | - | Recherche, filtrage, export |
| Page administration système | ⏳ | - | Paramètres, health |
| Page métriques système | ⏳ | - | CPU, mémoire, requêtes |
| Dashboard admin | ⏳ | - | Vue d'ensemble système |
| Filtres audit avancés | ⏳ | - | Date, utilisateur, action, entité |

### Décisions techniques
- **Audit:** Tous les CUD (Create/Update/Delete), auth, accès sensibles
- **Immuabilité:** Logs jamais supprimés, table append-only
- **Format:** JSON structuré avec contexte complet
- **Conformité:** PCI DSS, RGPD, exigences bancaires
- **Masking:** Regex pour détecter données sensibles (PAN, SSN, etc.)

---

## 🚀 Phase 11 - Déploiement et Documentation
**Statut:** ⏳ Planifié
**Début estimé:** Après Phase 10
**Fin estimée:** -

### Tâches
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Procfile Heroku | ⏳ | - | web: java -jar backend.jar |
| Configuration PostgreSQL Heroku | ⏳ | - | Addon DATABASE_URL |
| Variables d'environnement Heroku | ⏳ | - | Secrets, config |
| Script start-dev.sh | ⏳ | - | Lancement local |
| Script deploy.sh | ⏳ | - | Déploiement automatisé |
| GitHub Actions CI/CD | ⏳ | - | Tests + deploy auto |
| README.md complet | ⏳ | - | Installation, architecture |
| API-DOCS.md | ⏳ | - | Documentation API complète |
| USER-GUIDE.md (FR) | ⏳ | - | Guide utilisateur français |
| USER-GUIDE.md (EN) | ⏳ | - | Guide utilisateur anglais |
| init-db.sql | ⏳ | - | Données initiales + admin |
| Tests E2E (Playwright/Cypress) | ⏳ | - | Scénarios complets |
| Validation sécurité OWASP | ⏳ | - | Checklist Top 10 |
| Performance testing (JMeter) | ⏳ | - | Load tests |

### Décisions techniques
- **Heroku stack:** heroku-22
- **Build:** Maven pour backend, build Angular en CI/CD
- **Serving:** Backend sert aussi frontend (dist/ en resources/static)
- **CI/CD:** GitHub Actions avec tests obligatoires avant deploy

---

## 📊 Métriques Globales

| Métrique | Objectif | Actuel |
|----------|----------|---------|
| Couverture tests backend | >70% | 0% |
| Couverture tests frontend | >60% | 0% |
| Taux i18n FR/EN | 100% | 0% |
| Score sécurité OWASP | A | - |
| Performance API (p95) | <200ms | - |
| Uptime production | >99% | - |

---

## 🎯 Prochaines Étapes Immédiates

1. ✅ Créer ROADMAP.md (ce fichier)
2. 🚧 Initialiser projet Spring Boot 3.x avec dépendances
3. ⏳ Initialiser projet Angular 17+
4. ⏳ Configuration i18n FR/EN backend et frontend
5. ⏳ Configuration CORS, Swagger, profils

---

## 📝 Notes et Décisions Importantes

### 2025-11-07
- **Décision:** Utilisation de H2 en développement avec mode FILE pour persistance
- **Décision:** PostgreSQL en production via Heroku addon
- **Décision:** i18n obligatoire dès le début sur toutes les fonctionnalités
- **Décision:** Audit logging complet pour conformité bancaire
- **Décision:** Architecture monolithique au départ, microservices possibles en v2
- **Décision:** RBAC avec permissions granulaires dès Phase 2
- **Décision:** Tests unitaires obligatoires (>70% couverture) avant merge

---

**Dernière mise à jour:** 2025-11-07
**Prochaine révision:** Fin de chaque phase
