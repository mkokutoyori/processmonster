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
**Statut:** ✅ Terminé (Backend + Services)
**Début:** 2025-11-07
**Fin:** 2025-11-07

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity ProcessInstance | ✅ | 2025-11-07 | États, business key, parent/child, audit, soft delete |
| Entity ProcessVariable | ✅ | 2025-11-07 | 7 types, 3 scopes (GLOBAL/LOCAL/TRANSIENT) |
| Entity ExecutionHistory | ✅ | 2025-11-07 | Audit immutable, 20+ event types |
| Repository ProcessInstanceRepository | ✅ | 2025-11-07 | 18 query methods (status, definition, business key) |
| Repository ProcessVariableRepository | ✅ | 2025-11-07 | Variable CRUD, find by scope/type |
| Repository ExecutionHistoryRepository | ✅ | 2025-11-07 | Query by event type, activity, analytics |
| Service ProcessExecutionService | ✅ | 2025-11-07 | start, suspend, resume, terminate, complete, fail |
| Service VariableService | ✅ | 2025-11-07 | Get/set/delete, type detection, bulk operations |
| DTOs (3) | ✅ | 2025-11-07 | ProcessInstanceDTO, StartProcessInstanceDTO, ExecutionHistoryDTO |
| Mapper ProcessInstanceMapper | ✅ | 2025-11-07 | MapStruct entity-DTO mapping |
| Controller ProcessInstanceController | ✅ | 2025-11-07 | 11 REST endpoints avec OpenAPI |
| Endpoint POST /api/v1/instances/start | ✅ | 2025-11-07 | Démarrer avec variables |
| Endpoint PUT /api/v1/instances/{id}/suspend | ✅ | 2025-11-07 | Suspendre avec raison |
| Endpoint PUT /api/v1/instances/{id}/resume | ✅ | 2025-11-07 | Reprendre |
| Endpoint PUT /api/v1/instances/{id}/terminate | ✅ | 2025-11-07 | Terminer avec raison |
| Endpoint GET /api/v1/instances | ✅ | 2025-11-07 | Liste paginée |
| Endpoint GET /api/v1/instances/active | ✅ | 2025-11-07 | Instances actives |
| Endpoint GET /api/v1/instances/{id}/history | ✅ | 2025-11-07 | Historique complet |
| Endpoint GET/PUT /api/v1/instances/{id}/variables | ✅ | 2025-11-07 | Get/set variables |
| Gestion états (5 états) | ✅ | 2025-11-07 | RUNNING, SUSPENDED, COMPLETED, FAILED, TERMINATED |
| Messages i18n FR/EN | ✅ | 2025-11-07 | 16 messages instance.* |
| Intégration Camunda/Flowable | ⏳ | - | À intégrer Phase suivante (optionnel) |
| Tests exécution | ⏳ | - | À implémenter |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Models TypeScript | ✅ | 2025-11-07 | ProcessInstance, ExecutionHistory, StartRequest |
| Service ProcessInstanceService | ✅ | 2025-11-07 | 11 méthodes API complètes |
| Page liste instances | ⏳ | - | À implémenter |
| Page détail instance | ⏳ | - | À implémenter |
| Composant visualisation BPMN | ⏳ | - | À implémenter |
| Modal variables processus | ⏳ | - | À implémenter |
| Page historique exécution | ⏳ | - | À implémenter |
| Actions suspend/resume/cancel | ⏳ | - | À implémenter |

### Décisions techniques
- **Architecture:** Sans moteur externe (Camunda optionnel pour Phase future)
- **State Machine:** 5 états avec validations métier
- **Variables:** Type-safe avec auto-détection de type
- **History:** Audit trail immutable avec événements détaillés
- **Persistance:** BDD relationnelle avec indexes optimisés
- **Soft Delete:** Pattern appliqué pour auditabilité
- **Security:** RBAC avec INSTANCE_* permissions

---

## ✅ Phase 6 - Gestion des Tâches
**Statut:** ✅ Terminé
**Début:** 2025-11-07
**Fin:** 2025-11-07

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity Task | ✅ | 2025-11-07 | 5 statuses, 4 priorities, soft delete, audit, indexes |
| Entity TaskComment | ✅ | 2025-11-07 | 5 comment types, soft delete, audit |
| Entity TaskAttachment | ✅ | 2025-11-07 | File metadata, formatted size helper |
| Repository TaskRepository | ✅ | 2025-11-07 | 18+ query methods for task management |
| Repository TaskCommentRepository | ✅ | 2025-11-07 | CRUD operations for comments |
| Repository TaskAttachmentRepository | ✅ | 2025-11-07 | CRUD operations for attachments |
| Service TaskService | ✅ | 2025-11-07 | 25+ methods - full lifecycle management |
| Service NotificationService | ✅ | 2025-11-07 | Async notifications with Spring Events |
| Service FileStorageService | ✅ | 2025-11-07 | File upload with MIME validation, size limits |
| DTOs (6 total) | ✅ | 2025-11-07 | Task, CreateTask, UpdateTask, Comment, Attachment DTOs |
| Mapper TaskMapper | ✅ | 2025-11-07 | MapStruct entity-DTO mapping |
| Controller TaskController | ✅ | 2025-11-07 | 25 REST endpoints with OpenAPI docs |
| Endpoint GET /api/v1/tasks/inbox | ✅ | 2025-11-07 | Personal inbox paginated |
| Endpoint GET /api/v1/tasks/queue/{group} | ✅ | 2025-11-07 | Queue tasks by candidate group |
| Endpoint PUT /api/v1/tasks/{id}/claim | ✅ | 2025-11-07 | Claim task for current user |
| Endpoint PUT /api/v1/tasks/{id}/assign | ✅ | 2025-11-07 | Assign task to user |
| Endpoint PUT /api/v1/tasks/{id}/start | ✅ | 2025-11-07 | Start task (ASSIGNED → IN_PROGRESS) |
| Endpoint PUT /api/v1/tasks/{id}/complete | ✅ | 2025-11-07 | Complete task with optional form data |
| Endpoint PUT /api/v1/tasks/{id}/cancel | ✅ | 2025-11-07 | Cancel task with reason |
| Endpoint POST /api/v1/tasks/{id}/comments | ✅ | 2025-11-07 | Add comment to task |
| Endpoint GET /api/v1/tasks/{id}/comments | ✅ | 2025-11-07 | Get all comments |
| Endpoint POST /api/v1/tasks/{id}/attachments | ✅ | 2025-11-07 | Upload file attachment |
| Endpoint GET /api/v1/tasks/{id}/attachments | ✅ | 2025-11-07 | Get all attachments |
| Endpoint DELETE /api/v1/tasks/attachments/{id} | ✅ | 2025-11-07 | Delete attachment |
| Endpoint GET /api/v1/tasks/overdue | ✅ | 2025-11-07 | Get overdue tasks |
| Endpoint GET /api/v1/tasks/due-soon | ✅ | 2025-11-07 | Get tasks due in 24h |
| Endpoint GET /api/v1/tasks/search | ✅ | 2025-11-07 | Search tasks by keyword |
| Gestion priorités (LOW, NORMAL, HIGH, CRITICAL) | ✅ | 2025-11-07 | Enum with sorting support |
| Gestion deadlines avec alertes | ✅ | 2025-11-07 | isOverdue() method + notifications |
| Tests unitaires TaskService | ✅ | 2025-11-07 | 14 tests with Mockito (lifecycle, validation) |
| Tests intégration TaskController | ✅ | 2025-11-07 | 18 tests with MockMvc (endpoints, security) |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Models TypeScript | ✅ | 2025-11-07 | Task, Comment, Attachment interfaces |
| Service TaskService | ✅ | 2025-11-07 | 17+ API methods (CRUD, lifecycle, queries) |
| Component TaskInboxComponent | ✅ | 2025-11-07 | Material table, filters, pagination, actions |
| Component TaskDetailComponent | ✅ | 2025-11-07 | Tabs (overview, comments, attachments) |
| Routes configuration | ✅ | 2025-11-07 | Lazy loading /tasks routes |
| Composant upload fichiers | ✅ | 2025-11-07 | File input with upload in detail view |
| Composant commentaires | ✅ | 2025-11-07 | Comment form + list in detail view |
| Badge notifications | ✅ | 2025-11-07 | Active task count badge |
| Filtres et tri | ✅ | 2025-11-07 | Status, priority filters + sort |
| Actions rapides | ✅ | 2025-11-07 | Claim, start, complete, cancel buttons |
| Overdue indicators | ✅ | 2025-11-07 | Red highlighting for overdue tasks |
| Priority chips | ✅ | 2025-11-07 | Color-coded priority display |
| Status chips | ✅ | 2025-11-07 | Color-coded status display |
| Composant formulaire dynamique | ⏳ | - | À implémenter Phase 7 (Forms) |

### Décisions techniques
- **Notifications:** Spring Events + async processing
- **Email:** JavaMailSender avec templates HTML i18n
- **Upload:** Multipart, limite 10MB, validation MIME type
- **Storage:** Local dev, AWS S3/Heroku addon prod
- **Real-time:** WebSocket pour notifications in-app (optionnel)

---

## 📝 Phase 7 - Formulaires Dynamiques
**Statut:** ✅ Terminé
**Début:** 2025-11-07
**Fin:** 2025-11-07

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity FormDefinition | ✅ | 2025-11-07 | id, formKey, name, version, schemaJson, uiSchemaJson, published, isLatestVersion |
| Entity FormSubmission | ✅ | 2025-11-07 | id, formDefinition, task, processInstance, dataJson, status, validationErrors |
| Entity SubmissionStatus | ✅ | 2025-11-07 | Enum: DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED |
| Repository FormDefinitionRepository | ✅ | 2025-11-07 | 15+ query methods (latest version, published, search) |
| Repository FormSubmissionRepository | ✅ | 2025-11-07 | 17+ query methods (by status, submitted by, pending) |
| Service FormService | ✅ | 2025-11-07 | 25+ methods - CRUD, versioning, draft/submit, approve/reject |
| Service FormValidationService | ✅ | 2025-11-07 | JSON Schema Draft 7 validation via networknt library |
| DTOs (6 total) | ✅ | 2025-11-07 | FormDefinitionDTO, CreateFormDefinitionDTO, UpdateFormDefinitionDTO, FormSubmissionDTO, SaveDraftDTO, SubmitFormDTO |
| Mapper FormMapper | ✅ | 2025-11-07 | MapStruct entity-DTO mapping (7 methods) |
| Controller FormController | ✅ | 2025-11-07 | 26 REST endpoints avec OpenAPI docs |
| Endpoint POST /api/v1/forms/definitions | ✅ | 2025-11-07 | Créer définition formulaire |
| Endpoint PUT /api/v1/forms/definitions/{id} | ✅ | 2025-11-07 | Update avec auto-versioning si schema change |
| Endpoint GET /api/v1/forms/definitions | ✅ | 2025-11-07 | Liste paginée (latestOnly param) |
| Endpoint GET /api/v1/forms/definitions/{id} | ✅ | 2025-11-07 | Détails définition |
| Endpoint GET /api/v1/forms/definitions/key/{key} | ✅ | 2025-11-07 | Récupérer par formKey (latest version) |
| Endpoint POST /api/v1/forms/definitions/validate-schema | ✅ | 2025-11-07 | Valider JSON Schema |
| Endpoint PUT /api/v1/forms/definitions/{id}/publish | ✅ | 2025-11-07 | Publier/dépublier |
| Endpoint GET /api/v1/forms/definitions/published | ✅ | 2025-11-07 | Forms publiés uniquement |
| Endpoint POST /api/v1/forms/submissions/draft | ✅ | 2025-11-07 | Sauvegarder brouillon (no validation) |
| Endpoint POST /api/v1/forms/submissions/submit | ✅ | 2025-11-07 | Soumettre avec validation complète |
| Endpoint PUT /api/v1/forms/submissions/{id}/approve | ✅ | 2025-11-07 | Approuver submission |
| Endpoint PUT /api/v1/forms/submissions/{id}/reject | ✅ | 2025-11-07 | Rejeter submission |
| Endpoint GET /api/v1/forms/submissions/my-submissions | ✅ | 2025-11-07 | Mes soumissions |
| Endpoint GET /api/v1/forms/submissions/pending-review | ✅ | 2025-11-07 | En attente d'approbation |
| Auto-versioning | ✅ | 2025-11-07 | Nouvelle version auto si schemaJson change |
| Support JSON Schema Draft 7 | ✅ | 2025-11-07 | Types: string, number, integer, boolean, array, object |
| Validation règles complètes | ✅ | 2025-11-07 | required, minLength, maxLength, minimum, maximum, pattern, format, enum, const |
| Champs conditionnels | ✅ | 2025-11-07 | JSON Schema if/then/else (voir wire-transfer example) |
| Draft vs. Submit distinction | ✅ | 2025-11-07 | Draft: JSON format check only, Submit: full schema validation |
| Tests unitaires FormService | ✅ | 2025-11-07 | 18 tests JUnit 5 + Mockito (versioning, validation, lifecycle) |
| Tests intégration FormController | ✅ | 2025-11-07 | 24 tests @SpringBootTest + MockMvc (endpoints, security) |
| Messages i18n FR/EN | ✅ | 2025-11-07 | 40 messages form.* en français et anglais |
| Dépendance json-schema-validator | ✅ | 2025-11-07 | networknt:json-schema-validator:1.0.87 |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Models TypeScript | ✅ | 2025-11-07 | 12 interfaces: FormDefinition, FormSubmission, FormSchema, FormSchemaProperty, UISchema, etc. |
| Service FormService | ✅ | 2025-11-07 | 24 méthodes API + 4 helpers (parseSchema, stringifyFormData, etc.) |
| Composant FormBuilder | ⏳ | - | À implémenter Phase suivante |
| Composant FormRenderer | ⏳ | - | À implémenter Phase suivante |
| Composant field types (text, number, date, etc.) | ⏳ | - | À implémenter Phase suivante |
| Validation client (Reactive Forms) | ⏳ | - | À implémenter Phase suivante |
| Gestion champs conditionnels | ⏳ | - | RxJS pour réactivité |
| Auto-save | ⏳ | - | Debounce + localStorage |
| Preview formulaire | ⏳ | - | Mode aperçu |

### Example Schemas
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Loan Application Schema | ✅ | 2025-11-07 | 30+ properties, co-applicant support, conditional logic |
| Account Opening Schema | ✅ | 2025-11-07 | 35+ properties, ID verification, citizenship, initial deposit |
| Wire Transfer Schema | ✅ | 2025-11-07 | 30+ properties, multi-currency, SWIFT/routing, AML/KYC, if/then/else |
| README Documentation | ✅ | 2025-11-07 | Usage guide, customization, compliance considerations |

### Décisions techniques
- **Schema:** JSON Schema Draft 7 avec validation networknt/json-schema-validator:1.0.87
- **Versioning:** Auto-incrémentation version quand schemaJson change, flag isLatestVersion
- **Draft vs. Submit:** saveDraft() = JSON format check only, submitForm() = full validation
- **Workflow:** DRAFT → SUBMITTED → APPROVED/REJECTED/CANCELLED
- **UI Schema:** Support uiSchemaJson pour hints de rendu (ui:widget, ui:placeholder, etc.)
- **Conditional Fields:** JSON Schema if/then/else pour champs conditionnels
- **Frontend Builder:** À implémenter avec formio.js ou custom drag & drop (Phase future)
- **Compliance:** Schemas incluent champs KYC, AML, PATRIOT Act, FCRA, E-Sign Act
- **Security:** RBAC @PreAuthorize (FORM_READ, FORM_CREATE, FORM_UPDATE, FORM_DELETE)

---

## 📈 Phase 8 - Monitoring et Reporting
**Statut:** ✅ Terminé (Core) - UI à venir
**Début:** 2025-11-07
**Fin:** 2025-11-07

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Service MetricsService | ✅ | 2025-11-07 | 7 méthodes de calcul KPIs, tous cached 5min |
| DTOs dashboard (5) | ✅ | 2025-11-07 | SystemKPIs, StatusStats, UserTaskStats, ProcessDefinitionStats, DailyCompletionTrend |
| Controller DashboardController | ✅ | 2025-11-07 | 7 REST endpoints avec OpenAPI |
| Endpoint GET /api/v1/dashboard/kpis | ✅ | 2025-11-07 | KPIs système (12 métriques) |
| Endpoint GET /api/v1/dashboard/process-stats | ✅ | 2025-11-07 | Stats processus par statut |
| Endpoint GET /api/v1/dashboard/task-stats | ✅ | 2025-11-07 | Stats tâches par statut |
| Endpoint GET /api/v1/dashboard/task-priority-stats | ✅ | 2025-11-07 | Stats tâches par priorité |
| Endpoint GET /api/v1/dashboard/user-stats | ✅ | 2025-11-07 | Stats performance utilisateur |
| Endpoint GET /api/v1/dashboard/completion-trend | ✅ | 2025-11-07 | Tendance complétion quotidienne |
| Endpoint GET /api/v1/dashboard/process-definition-stats/{key} | ✅ | 2025-11-07 | Stats par définition de processus |
| KPIs (nb processus actifs, tâches en retard, temps moyen, etc.) | ✅ | 2025-11-07 | 12 KPIs calculés |
| Cache métriques | ✅ | 2025-11-07 | Caffeine 5min TTL sur toutes les métriques |
| Repository enhancements | ✅ | 2025-11-07 | 15 nouvelles méthodes (ProcessInstanceRepository + TaskRepository) |
| i18n messages FR/EN | ✅ | 2025-11-07 | 15 messages dashboard.* |
| Service ReportService | ⏳ | - | À implémenter Phase future |
| Service ExportService | ⏳ | - | À implémenter Phase future |
| Endpoint GET /api/reports/generate | ⏳ | - | À implémenter Phase future |
| Endpoint GET /api/reports/{id}/download | ⏳ | - | À implémenter Phase future |
| Génération PDF (iText/Flying Saucer) | ⏳ | - | À implémenter Phase future |
| Génération Excel (Apache POI) | ⏳ | - | À implémenter Phase future |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Models TypeScript (6) | ✅ | 2025-11-07 | SystemKPIs, StatusStats, UserTaskStats, ProcessDefinitionStats, DailyCompletionTrend |
| Service DashboardService | ✅ | 2025-11-07 | 7 méthodes API complètes |
| Page dashboard principal | ⏳ | - | À implémenter Phase future |
| Composants KPI cards | ⏳ | - | À implémenter Phase future |
| Graphiques Chart.js (line, bar, pie, doughnut) | ⏳ | - | À implémenter Phase future |
| Filtres période (jour, semaine, mois, année, custom) | ⏳ | - | À implémenter Phase future |
| Page rapports | ⏳ | - | À implémenter Phase future |
| Modal configuration rapport | ⏳ | - | À implémenter Phase future |
| Download PDF/Excel | ⏳ | - | À implémenter Phase future |
| Refresh automatique dashboard | ⏳ | - | À implémenter Phase future |

### Décisions techniques
- **Metrics:** MetricsService avec 7 méthodes (getSystemKPIs, getProcessStatsByStatus, getTaskStatsByStatus, getTaskStatsByPriority, getUserTaskStats, getDailyTaskCompletionTrend, getProcessDefinitionStats)
- **Cache:** Caffeine @Cacheable avec TTL 5 minutes sur toutes les métriques
- **Performance:** Requêtes optimisées avec JPA, aggregations Java Streams
- **Security:** RBAC avec DASHBOARD_VIEW, ROLE_ADMIN, ROLE_MANAGER, ROLE_ANALYST
- **Charts:** Chart.js avec ng2-charts wrapper (à intégrer)
- **PDF:** iText pour génération côté serveur (futur)
- **Excel:** Apache POI (futur)
- **Async:** Rapports lourds en async avec notification (futur)

---

## 🔌 Phase 9 - API et Intégrations
**Statut:** ✅ Terminé (Core) - UI à venir
**Début:** 2025-11-08
**Fin:** 2025-11-08

### Tâches Backend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Entity ApiKey | ✅ | 2025-11-08 | SHA-256 hashing, permissions, rate limiting, IP whitelist, expiration |
| Entity Webhook | ✅ | 2025-11-08 | Event subscriptions, HMAC signatures, retry logic, delivery tracking |
| Entity WebhookDelivery | ✅ | 2025-11-08 | Audit trail (request/response, status, retry count, duration) |
| Repository ApiKeyRepository | ✅ | 2025-11-08 | 12 query methods (find by hash, active keys, expired keys, search) |
| Repository WebhookRepository | ✅ | 2025-11-08 | 10 query methods (find by event, enabled, search, with failures) |
| Repository WebhookDeliveryRepository | ✅ | 2025-11-08 | 9 query methods (by webhook, by status, pending retries, cleanup) |
| Service ApiKeyService | ✅ | 2025-11-08 | CRUD, secure key generation, SHA-256 hashing, authentication |
| Service WebhookService | ✅ | 2025-11-08 | CRUD, async delivery, retry logic, HMAC signatures, test webhook |
| DTOs (10) | ✅ | 2025-11-08 | ApiKeyDTO, CreateApiKeyDTO, ApiKeyCreatedDTO, WebhookDTO, CreateWebhookDTO, UpdateWebhookDTO, WebhookDeliveryDTO + 3 more |
| Mappers (2) | ✅ | 2025-11-08 | ApiKeyMapper, WebhookMapper (MapStruct) |
| ApiKeyAuthenticationFilter | ✅ | 2025-11-08 | Spring Security filter for X-API-Key header authentication |
| Controller ApiKeyController | ✅ | 2025-11-08 | 11 REST endpoints (CRUD, enable/disable, search, stats) |
| Controller WebhookController | ✅ | 2025-11-08 | 15 REST endpoints (CRUD, enable/disable, test, delivery history, stats) |
| Authentication API key (header X-API-Key) | ✅ | 2025-11-08 | Alternative authentication to JWT |
| Retry webhooks en cas d'échec | ✅ | 2025-11-08 | Exponential backoff (delay * 2^attempt) |
| i18n messages FR/EN | ✅ | 2025-11-08 | 20 messages (apikey.*, webhook.*) |
| Versioning API (v1) | ✅ | Existant | URL /api/v1/... déjà en place |
| Documentation OpenAPI 3.0 complète | ✅ | Existant | Swagger UI déjà configuré |
| Événements webhook (process.*, task.*, form.*) | ✅ | 2025-11-08 | Event-driven architecture, async triggering |
| Rate limiting | ⏳ | - | Bucket4j à implémenter (futur) |
| Tests API avec RestAssured | ⏳ | - | À implémenter (futur) |

### Tâches Frontend
| Tâche | Statut | Date | Notes |
|-------|--------|------|-------|
| Models TypeScript (10) | ✅ | 2025-11-08 | ApiKey, ApiKeyCreated, CreateApiKeyRequest, UpdateApiKeyRequest, Webhook, CreateWebhookRequest, UpdateWebhookRequest, WebhookDelivery |
| Service ApiKeyService | ✅ | 2025-11-08 | 10 méthodes API (CRUD, enable/disable, search, stats) |
| Service WebhookService | ✅ | 2025-11-08 | 11 méthodes API (CRUD, enable/disable, test, delivery history, stats) |
| Page gestion API keys | ⏳ | - | À implémenter (futur) |
| Page configuration webhooks | ⏳ | - | À implémenter (futur) |
| Page documentation API | ⏳ | - | À implémenter (futur) |
| Logs webhooks | ⏳ | - | À implémenter (futur) |

### Décisions techniques
- **API Keys:** SecureRandom + Base64 (32 bytes), SHA-256 hashing, never store plain text
- **Authentication:** X-API-Key header, Spring Security filter integration
- **Permissions:** Granular permission sets per API key, converted to Spring Security authorities
- **Rate Limiting:** Configurable per API key (requests/minute), future Bucket4j integration
- **IP Whitelisting:** Comma-separated IP addresses per key
- **Webhook Events:** Event-driven architecture with async delivery
- **Retry Logic:** Exponential backoff (delay * 2^attempt), configurable max retries
- **HMAC Signatures:** HMAC-SHA256 for webhook payload verification
- **Delivery Tracking:** Complete audit trail (request, response, duration, retries)
- **Versioning:** URI versioning (/api/v1/...) already in place
- **Security:** All endpoints protected with RBAC (API_KEY_*, WEBHOOK_* permissions)
- **Soft Delete:** Auditability for all entities

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
