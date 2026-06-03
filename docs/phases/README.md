# Roadmap de Développement — RdvMindset

Ce dossier contient les guides de mise en œuvre détaillés pour les 11 phases du projet, du démarrage de l'environnement jusqu'à la mise en production.

Consultez le diagramme des phases ici : [RdvMindset_Phases_Developpement.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/RdvMindset_Phases_Developpement.puml).

---

## 📅 Synthèse et Planification des Phases

| Phase | Description | Durée Estimée | Guide Détaillé |
|---|---|---|---|
| **PHASE 0** | **Mise en place de l'environnement** | 3 jours | [Guide Fondations (0 à 2)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_0_to_2_foundation.md#phase-0--mise-en-place-de-lenvironnement) |
| **PHASE 1** | **Base de données & modèle JPA** | 1 semaine | [Guide Fondations (0 à 2)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_0_to_2_foundation.md#phase-1--base-de-donnees--modele-de-donnees) |
| **PHASE 2** | **Authentification JWT & Sécurité** | 1 semaine | [Guide Fondations (0 à 2)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_0_to_2_foundation.md#phase-2--authentification--securite) |
| **PHASE 3** | **Core métier Spring Boot** | 2 semaines | [Guide Core & File d'attente (3 & 4)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_3_to_4_core_queue.md#phase-3--core-metier-spring-boot) |
| **PHASE 4** | **Message Queue & Orchestration n8n** | 1 semaine | [Guide Core & File d'attente (3 & 4)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_3_to_4_core_queue.md#phase-4--message-queue--n8n--spring-boot) |
| **PHASE 5** | **Intégration Agent Vocal Vapi** | 1 semaine | [Guide Agents IA (5 & 6)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_5_to_6_ai_agents.md#phase-5--integration-agent-vocal-vapi) |
| **PHASE 6** | **Intégration Chatbot Botpress** | 1 semaine | [Guide Agents IA (5 & 6)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_5_to_6_ai_agents.md#phase-6--integration-chatbot-botpress) |
| **PHASE 7** | **Frontend React / Next.js 14** | 2 semaines | [Guide Frontend (7)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_7_frontend.md) |
| **PHASE 8** | **Workflows Avancés & CRM** | 1 semaine | [Guide Production & Qualité (8 à 10)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_8_to_10_production.md#phase-8--workflows-n8n-avances) |
| **PHASE 9** | **Tests Unitaires, Intégration et E2E** | 1 semaine | [Guide Production & Qualité (8 à 10)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_8_to_10_production.md#phase-9--tests--qualite) |
| **PHASE 10** | **DevOps & Déploiement Cloud** | 1 semaine | [Guide Production & Qualité (8 à 10)](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/phases/phase_8_to_10_production.md#phase-10--devops--deploiement) |

---

## 🛠️ Stack Technique Retenue

### Backend Core
* **Java 21** + **Spring Boot 3.x**
* **Spring Security** (JWT Stateless) + **OAuth2 Client** (Google APIs)
* **Spring Data JPA** + **PostgreSQL 16**
* **Flyway** (Migrations de base de données)
* **Spring WebFlux** (WebClient réactif pour API Vapi & Botpress)
* **Spring WebSockets** (STOMP pour mises à jour dashboard en temps réel)

### Message Broker & Cache
* **Redis 7**
  * *Streams* : Gestion asynchrone des tâches (vérification & création de rendez-vous)
  * *Cache* : Cache des disponibilités (plages horaires calculées)

### Frontend
* **React 18** + **Next.js 14** (App Router, TypeScript)
* **Tailwind CSS** + **shadcn/ui** (Design moderne, harmonieux, responsive)
* **Zustand** (Gestion d'état globale et légère)
* **TanStack Query (v5)** (Gestion du cache serveur et requêtes HTTP)
* **FullCalendar / React-Big-Calendar** (Composant de gestion d'agenda)

### Orchestration IA & Plateformes
* **n8n Workflow Engine** (Auto-hébergé sous Docker, orchestration asynchrone)
* **Vapi Cloud** (Moteur de dialogue vocal interactif)
* **Botpress Cloud** (Widget de chat textuel contextuel)

### DevOps & Monitoring
* **Docker** & **Docker Compose** (Local et Staging)
* **GitHub Actions** (Pipelines CI/CD de validation et build)
* **Nginx** (Reverse Proxy & SSL Let's Encrypt)
* **Spring Actuator** + **Prometheus** + **Grafana** (Indicateurs de santé, CPU et latence)
