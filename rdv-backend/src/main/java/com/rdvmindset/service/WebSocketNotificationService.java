package com.rdvmindset.service;

import com.rdvmindset.dto.AppointmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Pousse un nouveau rendez-vous sur le canal d'une entreprise spécifique.
     * Le frontend abonné à /topic/entreprise/{companyId}/rdv le recevra instantanément.
     */
    public void notifyNewAppointment(UUID companyId, AppointmentResponse appointment) {
        String destination = "/topic/entreprise/" + companyId + "/rdv";
        log.info("Envoi d'une notification WebSocket vers: {}", destination);
        messagingTemplate.convertAndSend(destination, appointment);
    }
}
