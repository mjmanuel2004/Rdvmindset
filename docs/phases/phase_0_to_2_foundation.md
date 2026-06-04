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
Placez ce fichier à la racine du projet pour orchestrer la base de données PostgreSQL, le cache/broker Redis et l'orchestrateur n8n en local.

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: rdv_postgres
    ports:
      - "5432:5432"
    environment:
      POSTGRES_DB: rdvmindset
      POSTGRES_USER: dev_user
      POSTGRES_PASSWORD: dev_password_123
    volumes:
      - postgres_data:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: rdv_redis
    ports:
      - "6379:6379"
    command: redis-server --appendonly yes
    volumes:
      - redis_data:/data

  n8n:
    image: docker.n8n.io/n8nio/n8n:latest
    container_name: rdv_n8n
    ports:
      - "5678:5678"
    environment:
      - N8N_HOST=localhost
      - N8N_PORT=5678
      - N8N_PROTOCOL=http
      - WEBHOOK_URL=http://localhost:5678/
    volumes:
      - n8n_data:/home/node/.n8n

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

#### `V1__init.sql` (Structure de base et Auth)
```sql
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE entreprises (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nom VARCHAR(255) NOT NULL,
    secteur VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    telephone VARCHAR(20),
    plan_abonnement VARCHAR(50) DEFAULT 'STANDARD',
    stripe_customer_id VARCHAR(255),
    actif BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entreprise_id UUID REFERENCES entreprises(id) ON DELETE CASCADE,
    email VARCHAR(255) NOT NULL UNIQUE,
    keycloak_id UUID UNIQUE,
    role VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### `V2__agents.sql` (Configuration Agents IA)
```sql
CREATE TABLE agents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entreprise_id UUID REFERENCES entreprises(id) ON DELETE CASCADE,
    nom VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL, -- VOCAL, CHATBOT
    vapi_assistant_id VARCHAR(255),
    botpress_bot_id VARCHAR(255),
    numero_telephone VARCHAR(20),
    system_prompt TEXT,
    actif BOOLEAN DEFAULT TRUE
);

CREATE TABLE agent_configs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    agent_id UUID UNIQUE REFERENCES agents(id) ON DELETE CASCADE,
    ton VARCHAR(50) DEFAULT 'PROFESSIONNEL',
    faq TEXT,
    tarifs TEXT,
    duree_rdv_minutes INT DEFAULT 30,
    secteur_modele VARCHAR(100),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### `V3__rdv.sql` (Modélisation des Rendez-vous & Calendrier)
```sql
CREATE TABLE clients (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    prenom VARCHAR(100) NOT NULL,
    nom VARCHAR(100) NOT NULL,
    email VARCHAR(255) UNIQUE,
    telephone VARCHAR(20) UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE disponibilites (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entreprise_id UUID REFERENCES entreprises(id) ON DELETE CASCADE,
    jour_semaine INT NOT NULL, -- 1=Lundi, ..., 7=Dimanche
    heure_debut TIME NOT NULL,
    heure_fin TIME NOT NULL,
    capacite_max INT DEFAULT 1
);

CREATE TABLE calendar_tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entreprise_id UUID UNIQUE REFERENCES entreprises(id) ON DELETE CASCADE,
    provider VARCHAR(50) NOT NULL, -- GOOGLE, OUTLOOK
    access_token_chiffre TEXT NOT NULL,
    refresh_token_chiffre TEXT NOT NULL,
    expires_at TIMESTAMP NOT NULL
);

CREATE TABLE rendez_vous (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entreprise_id UUID REFERENCES entreprises(id) ON DELETE CASCADE,
    client_id UUID REFERENCES clients(id),
    agent_id UUID REFERENCES agents(id),
    date_heure TIMESTAMP NOT NULL,
    duree_minutes INT NOT NULL,
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE',
    motif VARCHAR(255),
    notes TEXT,
    google_event_id VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### `V4__logs.sql` (Logs des canaux & Audit)
```sql
CREATE TABLE call_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rdv_id UUID REFERENCES rendez_vous(id) ON DELETE SET NULL,
    vapi_call_id VARCHAR(255) NOT NULL,
    date_appel TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    duree_secondes INT,
    transcription TEXT,
    statut VARCHAR(50)
);

CREATE TABLE chat_logs (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rdv_id UUID REFERENCES rendez_vous(id) ON DELETE SET NULL,
    botpress_session_id VARCHAR(255) NOT NULL,
    date_debut TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    messages_json JSONB,
    statut VARCHAR(50)
);

CREATE TABLE notifications (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    rdv_id UUID REFERENCES rendez_vous(id) ON DELETE CASCADE,
    canal VARCHAR(50) NOT NULL, -- EMAIL, SMS
    destinataire VARCHAR(255) NOT NULL,
    contenu TEXT NOT NULL,
    statut VARCHAR(50) DEFAULT 'EN_COURS',
    envoye_at TIMESTAMP
);
```

### 2. Exemple de Mapping JPA Bidirectionnel (`Entreprise.java`)
```java
package com.rdvmindset.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "entreprises")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Entreprise {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String secteur;

    @Column(nullable = false, unique = true)
    private String email;

    private String telephone;

    @Column(name = "plan_abonnement")
    private String planAbonnement;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    private boolean actif = true;

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
    private List<Agent> agents;

    @OneToMany(mappedBy = "entreprise", cascade = CascadeType.ALL)
    private List<Disponibilite> disponibilites;
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
    
    // Convertisseurs CORS et JWT/Roles définis ici...
}
```

### 2. Chiffrement AES-256 pour les API Tokens Calendrier
Pour sécuriser les jetons OAuth2 (Google/Outlook Calendar) stockés dans PostgreSQL, nous implémentons un convertisseur JPA de chiffrement transparent.

```java
package com.rdvmindset.security;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Converter
public class EncryptionConverter implements AttributeConverter<String, String> {
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final byte[] KEY = "CleSecreteSuperLonguePourAES256!".getBytes(); // Stocker dans .env

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du chiffrement du token", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(KEY, "AES");
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)));
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors du déchiffrement du token", e);
        }
    }
}
```

Dans l'entité `CalendarToken`, appliquez l'annotation :
```java
@Convert(converter = EncryptionConverter.class)
@Column(name = "access_token_chiffre", nullable = false)
private String accessToken;
```
