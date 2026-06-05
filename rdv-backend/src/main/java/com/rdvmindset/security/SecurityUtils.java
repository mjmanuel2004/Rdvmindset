package com.rdvmindset.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public class SecurityUtils {

    /**
     * Extrait l'ID Keycloak (le claim 'sub') du token JWT de l'utilisateur actuellement connecté.
     * 
     * @return Le UUID correspondant au keycloak_id de l'utilisateur, ou null si non authentifié.
     */
    public static UUID getCurrentUserKeycloakId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication != null && authentication.getPrincipal() instanceof Jwt) {
            Jwt jwt = (Jwt) authentication.getPrincipal();
            String sub = jwt.getClaimAsString("sub");
            if (sub != null) {
                return UUID.fromString(sub);
            }
        }
        
        return null;
    }
}
