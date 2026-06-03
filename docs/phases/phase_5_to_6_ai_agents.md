# Guide Agents IA : Phases 5 et 6

Ce guide détaille l'intégration de la couche IA conversationnelle : la création dynamique d'agents vocaux via Vapi, la mise en place de chatbots Botpress et la sécurisation par signature HMAC.

---

## Phase 5 — Intégration Agent Vocal Vapi

### 1. Création dynamique d'assistants vocaux (Java Client)
Spring Boot utilise un client HTTP non-bloquant (`WebClient`) pour provisionner un assistant sur le cloud de Vapi lors de l'onboarding de l'entreprise.

```java
package com.rdvmindset.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VapiClient {
    private final WebClient.Builder webClientBuilder;
    private static final String VAPI_API_URL = "https://api.vapi.ai";
    private static final String API_KEY = "vapi-key-ici"; // Configurer via application.yml

    public Map<String, Object> creerAssistantVapi(String nomAgent, String promptSectoriel) {
        WebClient client = webClientBuilder.baseUrl(VAPI_API_URL).build();

        Map<String, Object> body = Map.of(
            "name", nomAgent,
            "transcriber", Map.of(
                "provider", "deepgram",
                "model", "nova-2",
                "language", "fr"
            ),
            "voice", Map.of(
                "provider", "elevenlabs",
                "voiceId", "rachel" // Voix française chaleureuse
            ),
            "model", Map.of(
                "provider", "openai",
                "model", "gpt-4o",
                "messages", List.of(
                    Map.of("role", "system", "content", promptSectoriel)
                )
            ),
            "firstMessage", "Bonjour, je suis l'assistant d'accueil de " + nomAgent + ". Comment puis-je vous aider ?"
        );

        return client.post()
                .uri("/assistant")
                .header("Authorization", "Bearer " + API_KEY)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block(); // Bloquant uniquement durant la phase d'onboarding asynchrone
    }
}
```

### 2. Prompt System de Base sectoriel (Exemple : Cabinet de Santé)
```text
Tu es l'assistant vocal officiel de {nom_entreprise}. Ton objectif unique est d'accompagner le client pour fixer, modifier ou annuler un rendez-vous médical.
Tu dois :
- Être courtois, rassurant et professionnel.
- Ne pas inventer d'horaires. Tu dois impérativement demander les disponibilités à l'API.
- Extraire : le prénom du client, son numéro de téléphone, le motif de consultation et le jour souhaité.
- Dès que ces informations sont captées, propose les créneaux suivants : {creneaux_disponibles}.
- Une fois le choix arrêté, valide oralement en résumant le rendez-vous.
```

---

## Phase 6 — Intégration Chatbot Botpress

### 1. Script d'intégration Web (Génération dynamique)
Pour chaque entreprise, un widget de chat unique peut être injecté sur leur site web. Spring Boot génère le tag Javascript dynamiquement avec les paramètres de configuration de l'agent.

```java
package com.rdvmindset.service;

import com.rdvmindset.entity.Agent;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
public class AgentService {
    
    public String genererScriptWidget(Agent agent) {
        return """
        <!-- Widget Botpress pour %s -->
        <script src="https://cdn.botpress.cloud/webchat/v1/inject.js"></script>
        <script src="https://mediafiles.botpress.cloud/%s/webchat/config.js" defer></script>
        <script>
          window.botpressWebChat.onEvent(function(event) {
            if (event.type === 'LIFECYCLE.LOADED') {
              window.botpressWebChat.sendEvent({ type: 'show' });
            }
          }, ['LIFECYCLE.LOADED']);
        </script>
        """.formatted(agent.getNom(), agent.getBotpressBotId());
    }
}
```

### 2. Sécurisation par Signature HMAC-SHA256 (`WebhookController.java`)
Il est crucial de sécuriser les endpoints exposés à n8n/Vapi/Botpress pour empêcher la création malveillante de faux rendez-vous.

```java
package com.rdvmindset.controller;

import com.rdvmindset.service.WebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
public class WebhookController {
    private final WebhookService webhookService;
    private static final String SIGNING_SECRET = "MonSecretSignn8n!"; // Configurer via application.yml

    @PostMapping("/n8n")
    public ResponseEntity<Void> receiveN8nPayload(
            @RequestBody String rawBody,
            @RequestHeader("X-Signature-256") String signature) {

        if (!validerSignatureHmac(rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        webhookService.traiterEvenement(rawBody);
        return ResponseEntity.ok().build();
    }

    private boolean validerSignatureHmac(String payload, String signatureHeader) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(SIGNING_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] hash = sha256Hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            
            // Encodage hexadécimal
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString().equals(signatureHeader);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return false;
        }
    }
}
```
Dans n8n, rajoutez un header `X-Signature-256` généré à partir de la payload et du secret partagé.
