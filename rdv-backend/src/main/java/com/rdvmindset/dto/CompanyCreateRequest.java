package com.rdvmindset.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class CompanyCreateRequest {
    @NotBlank(message = "Le nom de l'entreprise est obligatoire")
    private String companyName;

    @Email(message = "L'email doit être valide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    private String phone;

    private List<UUID> industryIds; // Les secteurs sélectionnés par l'entreprise

    // Informations du compte propriétaire
    @NotBlank(message = "Le prénom est obligatoire")
    private String ownerFirstName;

    @NotBlank(message = "Le nom est obligatoire")
    private String ownerLastName;

    @NotBlank(message = "Le mot de passe est obligatoire")
    private String ownerPassword;
}
