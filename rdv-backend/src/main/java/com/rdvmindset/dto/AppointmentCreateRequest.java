package com.rdvmindset.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentCreateRequest {
    
    // Informations du client
    @NotBlank(message = "Le prénom du client est obligatoire")
    private String clientFirstName;

    @NotBlank(message = "Le nom du client est obligatoire")
    private String clientLastName;

    @NotBlank(message = "Le numéro de téléphone du client est obligatoire")
    private String clientPhone;

    private String clientEmail;

    // Informations du rendez-vous
    @NotNull(message = "La date et l'heure du rendez-vous sont obligatoires")
    @Future(message = "Le rendez-vous doit être dans le futur")
    private LocalDateTime dateTime;

    private int durationMinutes = 30; // Durée par défaut si non précisée par l'agent/IA

    private String reason;
    private String notes;

    // L'agent qui a pris le rendez-vous (optionnel, null si pris par le site web directement)
    private UUID agentId;
}
