# RdvMindset — Documentation Technique & Diagrammes UML

Ce dossier contient l'ensemble des diagrammes UML modélisant l'architecture, le cycle de vie et le modèle de données de la plateforme **RdvMindset**.

Chaque diagramme est disponible sous format source PlantUML (`.puml`) dans le sous-dossier `diagrams/`.

---

## Index des Diagrammes

| # | Nom du Diagramme | Fichier Source | Description |
|---|------------------|----------------|-------------|
| 1 | **Cas d'Utilisation** | [UseCase_RdvMindset.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/UseCase_RdvMindset.puml) | Rôles et actions des différents acteurs (Super Admin, Entreprise, Client B2C, Agents IA). |
| 2 | **Classes (Spring Boot)** | [Classes_SpringBoot.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/Classes_SpringBoot.puml) | Structure des entités JPA, des services de l'application et des API Rest Controllers. |
| 3 | **Séquence : Appel IA** | [Sequence_AppelVapi.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/Sequence_AppelVapi.puml) | Cinématique détaillée d'un appel téléphonique géré par l'agent IA vocal (Vapi) synchronisé avec Spring Boot via n8n. |
| 4 | **Séquence : Onboarding** | [Sequence_Onboarding.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/Sequence_Onboarding.puml) | Flux d'étapes pour l'inscription et la configuration d'une entreprise (OAuth2 Calendar, génération d'agents). |
| 5 | **Composants : Architecture** | [Components_Global.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/Components_Global.puml) | Vue d'ensemble des modules (React, Gateway, n8n, Spring Boot, Redis Message Queue, etc.). |
| 6 | **États : Cycle de vie RDV** | [State_RendezVous.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/State_RendezVous.puml) | Différents statuts d'un rendez-vous (`EN_ATTENTE`, `CONFIRME`, `REPORTE`, `NO_SHOW`, `COMPLETE`, `ANNULE`). |
| 7 | **Entité-Relation (ERD)** | [ERD_RdvMindset.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/ERD_RdvMindset.puml) | Modèle de données physique pour la base PostgreSQL. |
| 8 | **Déploiement (K8s/Docker)** | [Deployment_RdvMindset.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/Deployment_RdvMindset.puml) | Topologie de production (Pods Kubernetes, bases de données répliquées, CloudFlare, services tiers). |
| 9 | **Phases de Développement** | [RdvMindset_Phases_Developpement.puml](file:///Users/emmanuelmonsan/Documents/rdvmindset/docs/diagrams/RdvMindset_Phases_Developpement.puml) | Plan détaillé des phases de développement (de l'environnement à la mise en production). |

---

## Comment visualiser et modifier les diagrammes

1. **En ligne (Instantané) :**
   - Ouvrez le fichier `.puml` de votre choix.
   - Copiez l'intégralité du code.
   - Allez sur [PlantUML Live Editor](https://www.plantuml.com/plantuml/uml).
   - Collez le code pour afficher et éditer le diagramme en direct.

2. **Dans VS Code :**
   - Installez l'extension **"PlantUML"** par *jebbs*.
   - Ouvrez un fichier `.puml`.
   - Utilisez le raccourci `Alt + D` (Option + D sur Mac) pour afficher l'aperçu en direct.
