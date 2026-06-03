# Guide Production et Qualité : Phases 8 à 10

Ce guide couvre la mise en place de la résilience du système, la qualité logicielle (tests) et la préparation de l'infrastructure de production.

---

## Phase 8 — Workflows n8n avancés

### 1. Gestion des rappels automatisés (Cron J-1 / J-7)
Pour éviter les oublis de rendez-vous, un workflow n8n s'exécute de manière planifiée à intervalles réguliers (tous les jours à 8h00).

```text
[Cron Node (8h00)] 
       │
       ▼
[HTTP Request Node (GET /api/v1/rdv/reminders?days=1)] 
       │
       ▼
[Split In Batches (Traitement par lot de clients)]
       │
       ├─► [Canal SMS (Twilio)] ──► "Rappel : Votre RDV est demain à {{ heure }}."
       └─► [Canal Mail (Sendgrid)] ──► HTML personnalisé avec plan d'accès.
```

### 2. Détection automatique des No-Shows (Absences)
Un workflow s'exécute 30 minutes après la fin théorique de chaque rendez-vous. Si le statut est resté `CONFIRME` sans mise à jour, le système change automatiquement l'état en `NO_SHOW` et envoie un e-mail de rattrapage automatique pour caler un nouveau rendez-vous.

---

## Phase 9 — Tests & Qualité

### 1. Test unitaire Spring Boot (`RdvServiceTest.java`)
Vérification de la logique de réservation et de calcul des disponibilités.

```java
package com.rdvmindset.service;

import com.rdvmindset.entity.RendezVous;
import com.rdvmindset.repository.RendezVousRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RdvServiceTest {

    @Mock
    private RendezVousRepository rdvRepo;

    @InjectMocks
    private RdvService rdvService;

    private UUID rdvId;
    private RendezVous rdv;

    @BeforeEach
    void setUp() {
        rdvId = UUID.randomUUID();
        rdv = new RendezVous();
        rdv.setId(rdvId);
        rdv.setStatut(StatutRdv.CONFIRME);
    }

    @Test
    void testAnnulerRdv_Success() {
        when(rdvRepo.findById(rdvId)).thenReturn(Optional.of(rdv));

        rdvService.annulerRdv(rdvId);

        assertEquals(StatutRdv.ANNULE, rdv.getStatut());
        verify(rdvRepo, times(1)).save(rdv);
    }

    @Test
    void testAnnulerRdv_NotFound() {
        when(rdvRepo.findById(rdvId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            rdvService.annulerRdv(rdvId);
        });

        verify(rdvRepo, never()).save(any());
    }
}
```

### 2. Test E2E de l'Onboarding client avec Playwright
Validation de l'interface graphique et de la soumission de formulaires réactifs Next.js.

```typescript
import { test, expect } from '@playwright/test';

test('Devrait permettre à une entreprise de s\'inscrire et de compléter l\'onboarding', async ({ page }) => {
  await page.goto('http://localhost:3000/register');

  // Remplir le formulaire
  await page.fill('input[name="nomEntreprise"]', 'Cabinet Médical Dupont');
  await page.fill('input[name="email"]', 'dupont@medical.fr');
  await page.fill('input[name="motDePasse"]', 'Password123!');
  await page.click('button[type="submit"]');

  // Vérification de la redirection vers l'onboarding
  await expect(page).toHaveURL(/.*onboarding/);

  // Étape 2 : Configuration du secteur
  await page.selectOption('select[name="secteur"]', 'SANTE');
  await page.click('button:has-text("Suivant")');

  // Étape 3 : Vérification du stepper
  await expect(page.locator('text=Étape 3 : Connecter son Calendrier')).toBeVisible();
});
```

---

## Phase 10 — DevOps & Déploiement

### 1. Dockerfile Multi-stage pour Spring Boot (`rdv-backend/Dockerfile`)
Réduit considérablement la taille de l'image finale de production en séparant la phase de build de celle d'exécution.

```dockerfile
# 1. Étape de compilation
FROM maven:3.9-eclipse-temurin-21-alpine AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Étape d'exécution
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/rdvmindset-backend-*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2. Dockerfile Multi-stage pour Next.js (`rdv-frontend/Dockerfile`)
```dockerfile
# 1. Étape de compilation
FROM node:20-alpine AS builder
WORKDIR /app
RUN npm install -g pnpm
COPY package.json pnpm-lock.yaml ./
RUN pnpm install --frozen-lockfile
COPY . .
RUN pnpm build

# 2. Étape d'exécution
FROM node:20-alpine
WORKDIR /app
RUN npm install -g pnpm
COPY --from=builder /app/package.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/public ./public
EXPOSE 3000
CMD ["pnpm", "start"]
```

### 3. Pipeline CI/CD GitHub Actions (`.github/workflows/deploy.yml`)
Déclenche la suite de tests à chaque push puis compile et déploie automatiquement sur le serveur de staging.

```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [ main, develop ]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16
        env:
          POSTGRES_DB: rdvmindset_test
          POSTGRES_USER: test_user
          POSTGRES_PASSWORD: test_password
        ports:
          - 5432:5432
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 21
        uses: actions/setup-java@v3
        with:
          java-version: '21'
          distribution: 'temurin'
      - name: Run Backend Unit Tests
        run: |
          cd rdv-backend
          mvn clean test

  build-and-deploy:
    needs: test
    if: github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      - name: Login to GitHub Container Registry
        uses: docker/login-action@v2
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      - name: Build and Push Backend Image
        uses: docker/build-push-action@v4
        with:
          context: ./rdv-backend
          push: true
          tags: ghcr.io/${{ github.repository }}/backend:latest
      - name: Deploy via SSH to Production Server
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.SERVER_HOST }}
          username: ${{ secrets.SERVER_USER }}
          key: ${{ secrets.SSH_PRIVATE_KEY }}
          script: |
            cd /opt/rdvmindset
            docker compose pull
            docker compose up -d
```
