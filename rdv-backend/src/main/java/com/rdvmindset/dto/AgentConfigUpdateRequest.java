package com.rdvmindset.dto;

import lombok.Data;

@Data
public class AgentConfigUpdateRequest {
    private String tone;
    private String faq;
    private String pricing;
    private Integer appointmentDurationMinutes;
    private String systemPrompt; // Permet à l'utilisateur d'écraser le prompt généré manuellement
}
