package com.rdvmindset.controller;

import com.rdvmindset.entity.CalendarToken;
import com.rdvmindset.entity.Company;
import com.rdvmindset.repository.CalendarTokenRepository;
import com.rdvmindset.service.GoogleCalendarService;
import com.rdvmindset.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/oauth2/google")
@RequiredArgsConstructor
@Slf4j
public class OAuth2Controller {

    private final GoogleCalendarService googleCalendarService;
    private final CalendarTokenRepository calendarTokenRepository;
    private final UserService userService;

    /**
     * Génère l'URL pour que l'utilisateur se connecte à Google.
     * Cette route est protégée : on ne laisse que le OWNER ou ADMIN lier le calendrier de la boîte.
     */
    @GetMapping("/url")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        return ResponseEntity.ok(Map.of("url", googleCalendarService.getAuthorizationUrl()));
    }

    /**
     * Callback appelé par Google après que l'utilisateur a donné son consentement.
     * Reçoit le "code" d'autorisation.
     * Attention : en vrai, il faudra gérer le state pour récupérer l'utilisateur,
     * ici on s'appuie sur le token JWT qui doit être passé dans la requête du frontend, 
     * ou bien on passera le JWT dans le "state" lors de l'appel à /url.
     * Pour cette implémentation, on part du principe que l'appel vient du frontend avec le JWT.
     */
    @GetMapping("/callback")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<String> handleGoogleCallback(@RequestParam("code") String code) {
        Company company = userService.getCurrentUser().getCompany();
        
        try {
            GoogleCalendarService.GoogleTokenResponse tokenResponse = googleCalendarService.exchangeCodeForToken(code);

            // Vérifier s'il y a déjà un token pour cette entreprise
            Optional<CalendarToken> existingToken = calendarTokenRepository.findByCompanyId(company.getId());
            CalendarToken tokenEntity = existingToken.orElseGet(CalendarToken::new);

            tokenEntity.setCompany(company);
            tokenEntity.setProvider("GOOGLE");
            tokenEntity.setAccessToken(tokenResponse.getAccessToken());
            
            // Le refresh token n'est envoyé qu'à la première connexion, il ne faut pas l'écraser s'il est null
            if (tokenResponse.getRefreshToken() != null) {
                tokenEntity.setRefreshToken(tokenResponse.getRefreshToken());
            }

            tokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(tokenResponse.getExpiresIn()));

            calendarTokenRepository.save(tokenEntity);
            log.info("Calendrier Google connecté avec succès pour l'entreprise {}", company.getName());

            return ResponseEntity.ok("Google Calendar connecté avec succès !");

        } catch (Exception e) {
            log.error("Erreur lors de l'échange de token Google", e);
            return ResponseEntity.badRequest().body("Erreur lors de la synchronisation avec Google.");
        }
    }
}
