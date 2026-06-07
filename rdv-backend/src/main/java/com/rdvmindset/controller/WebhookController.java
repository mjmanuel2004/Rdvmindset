package com.rdvmindset.controller;

import com.rdvmindset.dto.AiWebhookRequest;
import com.rdvmindset.dto.AiWebhookResponse;
import com.rdvmindset.service.AiWebhookService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/webhooks")
@RequiredArgsConstructor
@Slf4j
public class WebhookController {

    private final AiWebhookService aiWebhookService;

    @Value("${app.webhook.api-key}")
    private String expectedApiKey;

    @PostMapping("/ai")
    public ResponseEntity<AiWebhookResponse> handleAiWebhook(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody AiWebhookRequest request) {

        // Vérification de sécurité (Clé API globale)
        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            log.warn("Tentative d'accès non autorisée au Webhook IA (Clé API invalide ou absente).");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AiWebhookResponse.error("Accès non autorisé: X-API-Key invalide."));
        }

        AiWebhookResponse response = aiWebhookService.handleWebhook(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }
}
