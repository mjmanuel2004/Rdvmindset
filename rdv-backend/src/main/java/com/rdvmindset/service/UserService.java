package com.rdvmindset.service;

import com.rdvmindset.dto.EmployeeInviteRequest;
import com.rdvmindset.dto.UserResponse;
import com.rdvmindset.entity.User;
import com.rdvmindset.repository.UserRepository;
import com.rdvmindset.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

    /**
     * Récupère l'utilisateur actuellement connecté via son token JWT.
     */
    public User getCurrentUser() {
        UUID keycloakId = SecurityUtils.getCurrentUserKeycloakId();
        if (keycloakId == null) {
            throw new IllegalStateException("Utilisateur non authentifié (Token JWT manquant ou invalide)");
        }
        
        return userRepository.findByKeycloakId(keycloakId)
                .orElseThrow(() -> new IllegalStateException("Utilisateur introuvable en base de données pour le keycloakId: " + keycloakId));
    }

    /**
     * Invite un nouvel employé (MANAGER, AGENT) et le rattache à l'entreprise de l'utilisateur connecté (OWNER).
     */
    @Transactional
    public UserResponse inviteEmployee(EmployeeInviteRequest request) {
        User currentUser = getCurrentUser();
        
        // Seuls le OWNER ou un ADMIN peuvent inviter
        if (currentUser.getCompany() == null) {
            throw new IllegalStateException("L'utilisateur actuel n'est rattaché à aucune entreprise");
        }

        // 1. Vérifier que l'email n'existe pas déjà
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        // 2. Créer l'utilisateur dans Keycloak sans mot de passe (Envoi d'email automatique)
        log.info("Invitation Keycloak pour {}", request.getEmail());
        UUID keycloakId = keycloakAdminService.inviteUser(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName()
        );

        // 3. Créer l'utilisateur en base de données
        User newEmployee = new User();
        newEmployee.setCompany(currentUser.getCompany());
        newEmployee.setEmail(request.getEmail());
        newEmployee.setKeycloakId(keycloakId);
        newEmployee.setRole(request.getRole());
        newEmployee.setCreatedAt(LocalDateTime.now());

        newEmployee = userRepository.save(newEmployee);
        
        log.info("Employé {} invité avec succès pour l'entreprise {}", newEmployee.getEmail(), currentUser.getCompany().getName());

        return UserResponse.fromEntity(newEmployee);
    }
}
