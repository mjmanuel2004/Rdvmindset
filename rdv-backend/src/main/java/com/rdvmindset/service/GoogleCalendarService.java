package com.rdvmindset.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.rdvmindset.entity.Appointment;
import com.rdvmindset.entity.CalendarToken;
import com.rdvmindset.repository.CalendarTokenRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class GoogleCalendarService {

    private final CalendarTokenRepository calendarTokenRepository;
    private final WebClient webClient = WebClient.create();

    @Value("${app.google.client-id}")
    private String clientId;

    @Value("${app.google.client-secret}")
    private String clientSecret;

    @Value("${app.google.redirect-uri}")
    private String redirectUri;

    private static final String GOOGLE_TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String GOOGLE_CALENDAR_API_URL = "https://www.googleapis.com/calendar/v3/calendars/primary/events";

    /**
     * Génère l'URL d'autorisation Google.
     */
    public String getAuthorizationUrl() {
        return "https://accounts.google.com/o/oauth2/v2/auth?" +
                "client_id=" + clientId +
                "&redirect_uri=" + redirectUri +
                "&response_type=code" +
                "&scope=https://www.googleapis.com/auth/calendar.events" +
                "&access_type=offline" +
                "&prompt=consent";
    }

    /**
     * Échange le code d'autorisation contre un token d'accès et de rafraîchissement.
     */
    public GoogleTokenResponse exchangeCodeForToken(String code) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("code", code);
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("redirect_uri", redirectUri);
        body.add("grant_type", "authorization_code");

        return webClient.post()
                .uri(GOOGLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();
    }

    /**
     * Rafraîchit le token d'accès s'il est expiré.
     */
    public String refreshAccessToken(CalendarToken tokenEntity) {
        if (tokenEntity.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(5))) {
            return tokenEntity.getAccessToken(); // Token toujours valide
        }

        log.info("Le token Google est expiré, rafraîchissement en cours...");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("client_id", clientId);
        body.add("client_secret", clientSecret);
        body.add("refresh_token", tokenEntity.getRefreshToken());
        body.add("grant_type", "refresh_token");

        GoogleTokenResponse response = webClient.post()
                .uri(GOOGLE_TOKEN_URL)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(body))
                .retrieve()
                .bodyToMono(GoogleTokenResponse.class)
                .block();

        if (response != null && response.getAccessToken() != null) {
            tokenEntity.setAccessToken(response.getAccessToken());
            tokenEntity.setExpiresAt(LocalDateTime.now().plusSeconds(response.getExpiresIn()));
            calendarTokenRepository.save(tokenEntity);
            return response.getAccessToken();
        }

        throw new RuntimeException("Impossible de rafraîchir le token Google.");
    }

    /**
     * Crée un événement dans le calendrier Google.
     */
    public void createEvent(Appointment appointment) {
        Optional<CalendarToken> tokenOpt = calendarTokenRepository.findByCompanyId(appointment.getCompany().getId());
        if (tokenOpt.isEmpty()) {
            log.info("L'entreprise {} n'a pas connecté son Google Calendar. Ignoré.", appointment.getCompany().getName());
            return;
        }

        CalendarToken tokenEntity = tokenOpt.get();
        String validAccessToken = refreshAccessToken(tokenEntity);

        LocalDateTime endDateTime = appointment.getDateTime().plusMinutes(appointment.getDurationMinutes());
        String isoStart = appointment.getDateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "+02:00"; // Assuming Europe/Paris for now
        String isoEnd = endDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) + "+02:00";

        Map<String, Object> eventBody = Map.of(
                "summary", "RDV avec " + appointment.getClient().getFirstName() + " " + appointment.getClient().getLastName(),
                "description", "Motif: " + (appointment.getReason() != null ? appointment.getReason() : "Non précisé") + 
                        "\nTéléphone: " + appointment.getClient().getPhone(),
                "start", Map.of("dateTime", isoStart, "timeZone", "Europe/Paris"),
                "end", Map.of("dateTime", isoEnd, "timeZone", "Europe/Paris")
        );

        try {
            Map response = webClient.post()
                    .uri(GOOGLE_CALENDAR_API_URL)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + validAccessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(eventBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            if (response != null && response.get("id") != null) {
                String eventId = (String) response.get("id");
                appointment.setGoogleEventId(eventId);
                log.info("Événement Google Calendar créé avec succès: {}", eventId);
            }
        } catch (Exception e) {
            log.error("Erreur lors de la création de l'événement Google Calendar", e);
        }
    }

    @Data
    public static class GoogleTokenResponse {
        @JsonProperty("access_token")
        private String accessToken;

        @JsonProperty("refresh_token")
        private String refreshToken;

        @JsonProperty("expires_in")
        private int expiresIn;
    }
}
