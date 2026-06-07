package com.rdvmindset.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;
import java.util.UUID;

@Data
public class AiWebhookRequest {
    
    @NotNull(message = "L'ID de l'agent (agentId) est obligatoire")
    private UUID agentId;
    
    @NotBlank(message = "L'action est obligatoire (ex: check_availability, book_appointment)")
    private String action;
    
    @NotNull(message = "Le payload ne peut pas être null")
    private Map<String, Object> payload;
}
