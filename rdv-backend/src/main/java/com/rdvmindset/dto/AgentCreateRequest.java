package com.rdvmindset.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AgentCreateRequest {
    
    @NotBlank(message = "Le nom de l'agent est obligatoire")
    private String name;

    @NotBlank(message = "Le type est obligatoire (VOCAL ou CHATBOT)")
    private String type;

    private String phoneNumber;
    
    private String modelIndustry; // Ex: Dentiste, Coiffeur, Avocat
}
