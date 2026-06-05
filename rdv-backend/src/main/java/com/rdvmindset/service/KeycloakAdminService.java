package com.rdvmindset.service;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.UUID;

@Service
@Slf4j
public class KeycloakAdminService {

    @Value("${app.keycloak.server-url}")
    private String serverUrl;

    @Value("${app.keycloak.realm}")
    private String realm;

    @Value("${app.keycloak.admin-client-id}")
    private String adminClientId;

    @Value("${app.keycloak.admin-username}")
    private String adminUsername;

    @Value("${app.keycloak.admin-password}")
    private String adminPassword;

    private Keycloak getKeycloakInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master") // L'admin par défaut est dans le realm master
                .clientId(adminClientId)
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }

    public UUID createUser(String email, String firstName, String lastName, String password) {
        try (Keycloak keycloak = getKeycloakInstance()) {
            RealmResource realmResource = keycloak.realm(realm);
            UsersResource usersResource = realmResource.users();

            UserRepresentation user = new UserRepresentation();
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            Response response = usersResource.create(user);
            
            if (response.getStatus() == 201) {
                // Utilisateur créé avec succès
                String path = response.getLocation().getPath();
                String userId = path.substring(path.lastIndexOf('/') + 1);
                
                // Assigner le mot de passe
                CredentialRepresentation passwordCred = new CredentialRepresentation();
                passwordCred.setTemporary(false);
                passwordCred.setType(CredentialRepresentation.PASSWORD);
                passwordCred.setValue(password);
                
                usersResource.get(userId).resetPassword(passwordCred);
                
                log.info("Utilisateur créé dans Keycloak avec l'ID: {}", userId);
                return UUID.fromString(userId);
            } else {
                log.error("Erreur lors de la création de l'utilisateur Keycloak. Status: {}, Erreur: {}", 
                        response.getStatus(), response.getStatusInfo().getReasonPhrase());
                throw new RuntimeException("Erreur Keycloak: " + response.getStatusInfo().getReasonPhrase());
            }
        } catch (Exception e) {
            log.error("Exception lors de l'appel à Keycloak", e);
            throw new RuntimeException("Erreur de connexion à Keycloak", e);
        }
    }
}
