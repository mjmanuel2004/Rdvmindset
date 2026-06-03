# RdvMindset

Plateforme intelligente de gestion et de prise de rendez-vous automatisée par IA (vocal & chat).

## 🚀 Quick Start

### Prérequis
- **Java 21** — `java --version`
- **Maven 3.9+** — `mvn --version`
- **Node.js 20+** — `node --version`
- **pnpm** — `pnpm --version`
- **Docker & Docker Compose** — `docker --version`

### 1. Infrastructure locale
```bash
# Démarrer PostgreSQL, Redis et n8n
docker compose -f docker-compose.local.yml up -d
```

### 2. Backend Spring Boot
```bash
cd rdv-backend
export JAVA_HOME=/opt/homebrew/opt/openjdk@21
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```
→ API disponible sur `http://localhost:8080`

### 3. Frontend Next.js
```bash
cd rdv-frontend
pnpm dev
```
→ Interface disponible sur `http://localhost:3000`

### 4. n8n (Workflows IA)
→ Dashboard n8n sur `http://localhost:5678`

---

## 📁 Structure du projet

```
rdvmindset/
├── docker-compose.local.yml  # Infrastructure Docker locale
├── .env                      # Variables d'environnement (non commité)
├── rdv-backend/              # API Spring Boot 3.x (Java 21)
│   ├── pom.xml
│   └── src/main/java/com/rdvmindset/
├── rdv-frontend/             # Interface Next.js 14 (TypeScript + Tailwind)
│   ├── package.json
│   └── src/app/
└── docs/                     # Documentation UML & guides de développement
    ├── diagrams/             # Diagrammes PlantUML
    └── phases/               # Roadmap de développement
```

## 📖 Documentation

- [Diagrammes UML PlantUML](docs/diagrams/)
- [Roadmap de développement (Phases 0 à 10)](docs/phases/README.md)
- [Index des diagrammes](docs/README.md)
