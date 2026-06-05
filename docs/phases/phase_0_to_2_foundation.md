# Guide Fondations : Phases 0 à 2

Ce guide couvre la mise en place de l'environnement de développement, la création des bases de données avec leurs modèles JPA et l'implémentation de la couche de sécurité/OAuth2.

---

## Phase 0 — Mise en place de l'environnement

### 1. Installation des prérequis système
* **Java 21** : Utilisation recommandée de SDKMAN pour isoler le JDK.
  ```bash
  sdk install java 21-open
  ```
* **Node.js 20 & PNPM** : Utilisation de NVM et installation globale de pnpm.
  ```bash
  nvm install 20
  npm install -g pnpm
  ```
* **Docker Desktop** : Requis pour faire tourner l'environnement de persistance local.

### 2. Conteneurs Locaux (`docker-compose.local.yml`)
Placez ce fichier à la racine du projet pour orchestrer la base de données PostgreSQL, le cache/broker Redis, l'orchestrateur n8n et Keycloak.

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: rdv_postgres
    ports:
      - "5433:5432"
    environment:
      POSTGRES_DB: rdvmindset
      POSTGRES_USER: dev_user
      POSTGRES_PASSWORD: dev_password_123
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./docker/postgres/init-keycloak-schema.sql:/docker-entrypoint-initdb.d/init-keycloak-schema.sql
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U dev_user -d rdvmindset"]
      interval: 5s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: rdv_redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 5s
      timeout: 3s
      retries: 5

  n8n:
    image: docker.n8n.io/n8nio/n8n:latest
    container_name: rdv_n8n
    ports:
      - "${N8N_PORT:-5678}:5678"
    environment:
      - N8N_HOST=${N8N_HOST:-localhost}
      - N8N_PORT=5678
      - N8N_PROTOCOL=${N8N_PROTOCOL:-http}
      - WEBHOOK_URL=http://localhost:${N8N_PORT:-5678}/
      - N8N_DIAGNOSTICS_ENABLED=false
      - N8N_SECURE_COOKIE=false
    volumes:
      - n8n_data:/home/node/.n8n
    depends_on:
      redis:
        condition: service_healthy

  keycloak:
    image: quay.io/keycloak/keycloak:24.0
    container_name: rdv_keycloak
    command: start-dev
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/rdvmindset
      KC_DB_SCHEMA: keycloak
      KC_DB_USERNAME: dev_user
      KC_DB_PASSWORD: dev_password_123
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_HEALTH_ENABLED: "true"
    ports:
      - "9090:8080"
    depends_on:
      postgres:
        condition: service_healthy
    healthcheck:
      test: ["CMD-SHELL", "curl -f http://localhost:8080/health/ready || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 10
      start_period: 30s

volumes:
  postgres_data:
  redis_data:
  n8n_data:
```

### 3. Structure globale du projet multi-module
```text
rdvmindset/
├── docker-compose.local.yml
├── rdv-backend/            # Projet Maven Spring Boot
│   ├── pom.xml
│   └── src/main/
│       ├── java/com/rdvmindset/
│       └── resources/
│           ├── db/migration/  # Migrations Flyway
│           └── application.yml
├── rdv-frontend/           # Projet Next.js 14
│   ├── package.json
│   └── src/app/
└── docs/
```

---

## Phase 1 — Base de données & modèle de données

### 1. Structure des Migrations Flyway (`db/migration/`)
Chaque étape de la modélisation correspond à un fichier SQL versionné.

#### `V1__init_core.sql` (Structure de base et Auth)
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE companies (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20),
    subscription_plan VARCHAR(50) DEFAULT 'STANDARD',
    stripe_customer_id VARCHAR(255),
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL UNIQUE,
    keycloak_id UUID UNIQUE,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### `V5__add_industries.sql` (Secteurs d'activité Many-to-Many)
```sql
CREATE TABLE industries (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE company_industries (
    company_id UUID REFERENCES companies(id) ON DELETE CASCADE,
    industry_id UUID REFERENCES industries(id) ON DELETE CASCADE,
    PRIMARY KEY (company_id, industry_id)
);
```

#### `V2__agents.sql` à `V4__logs.sql` (Exemples pour les Agents, RDVs, Logs)
Ces scripts créent les tables `agents`, `agent_configs`, `clients`, `availabilities`, `appointments`, `calendar_tokens`, `call_logs`, `chat_logs`, et `notifications`.

### 2. Exemple de Mapping JPA Bidirectionnel (`Company.java`)
```java
package com.rdvmindset.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ManyToMany
    @JoinTable(
        name = "company_industries",
        joinColumns = @JoinColumn(name = "company_id"),
        inverseJoinColumns = @JoinColumn(name = "industry_id")
    )
    private List<Industry> industries;

    @Column(nullable = false, unique = true)
    private String email;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private List<User> users;

    // ... autres relations (agents, appointments, etc.)
}
```

---

## Phase 2 — Authentification & Sécurité

### 1. Configuration Keycloak (OAuth2 Resource Server)
Le backend Spring Boot agit en tant que "Resource Server" et valide les jetons JWT émis par Keycloak.

```java
package com.rdvmindset.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/api/public/**", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
            );

        return http.build();
    }
}
```

### 2. Chiffrement AES-256 pour les API Tokens Calendrier
Pour sécuriser les jetons OAuth2 (Google/Outlook Calendar) stockés dans PostgreSQL, nous implémentons un convertisseur JPA de chiffrement transparent.

```java
package com.rdvmindset.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
@Component
public class EncryptionConverter implements AttributeConverter<String, String> {
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static byte[] KEY;

    @Value("${app.encryption.key:CleSecreteSuperLonguePourAES256!}")
    public void setKey(String key) {
        KEY = key.getBytes();
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // Logique de chiffrement
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // Logique de déchiffrement
    }
}
```
