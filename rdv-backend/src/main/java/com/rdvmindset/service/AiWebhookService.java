package com.rdvmindset.service;

import com.rdvmindset.dto.AiWebhookRequest;
import com.rdvmindset.dto.AiWebhookResponse;
import com.rdvmindset.dto.AppointmentCreateRequest;
import com.rdvmindset.entity.Agent;
import com.rdvmindset.entity.Company;
import com.rdvmindset.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiWebhookService {

    private final AgentRepository agentRepository;
    private final AppointmentService appointmentService;

    public AiWebhookResponse handleWebhook(AiWebhookRequest request) {
        log.info("Réception Webhook IA pour Agent ID: {} | Action: {}", request.getAgentId(), request.getAction());

        Agent agent = agentRepository.findById(request.getAgentId())
                .orElseThrow(() -> new IllegalArgumentException("Agent IA introuvable."));
        
        Company company = agent.getCompany();
        int defaultDuration = agent.getConfig() != null ? agent.getConfig().getAppointmentDurationMinutes() : 30;

        try {
            switch (request.getAction()) {
                case "check_availability":
                    return handleCheckAvailability(request.getPayload(), company.getId(), defaultDuration);
                
                case "book_appointment":
                    return handleBookAppointment(request.getPayload(), company, agent.getId(), defaultDuration);
                
                default:
                    return AiWebhookResponse.error("Action inconnue : " + request.getAction());
            }
        } catch (Exception e) {
            log.error("Erreur d'exécution du Webhook IA", e);
            return AiWebhookResponse.error("Erreur lors de l'exécution: " + e.getMessage());
        }
    }

    private AiWebhookResponse handleCheckAvailability(Map<String, Object> payload, java.util.UUID companyId, int defaultDuration) {
        if (!payload.containsKey("date")) {
            return AiWebhookResponse.error("Le paramètre 'date' (YYYY-MM-DD) est requis pour check_availability.");
        }

        LocalDate date = LocalDate.parse(payload.get("date").toString());
        int duration = payload.containsKey("durationMinutes") ? 
                Integer.parseInt(payload.get("durationMinutes").toString()) : defaultDuration;

        List<LocalTime> availableSlots = appointmentService.getAvailableSlots(companyId, date, duration);

        return AiWebhookResponse.ok("Disponibilités récupérées", Map.of("availableSlots", availableSlots));
    }

    private AiWebhookResponse handleBookAppointment(Map<String, Object> payload, Company company, java.util.UUID agentId, int defaultDuration) {
        // Validation des paramètres
        String[] requiredKeys = {"dateTime", "clientFirstName", "clientPhone"};
        for (String key : requiredKeys) {
            if (!payload.containsKey(key)) {
                return AiWebhookResponse.error("Le paramètre '" + key + "' est requis pour book_appointment.");
            }
        }

        AppointmentCreateRequest createReq = new AppointmentCreateRequest();
        createReq.setAgentId(agentId);
        createReq.setDateTime(LocalDateTime.parse(payload.get("dateTime").toString()));
        createReq.setClientFirstName(payload.get("clientFirstName").toString());
        
        if (payload.containsKey("clientLastName")) {
            createReq.setClientLastName(payload.get("clientLastName").toString());
        }
        
        createReq.setClientPhone(payload.get("clientPhone").toString());
        
        if (payload.containsKey("clientEmail")) {
            createReq.setClientEmail(payload.get("clientEmail").toString());
        }
        
        if (payload.containsKey("reason")) {
            createReq.setReason(payload.get("reason").toString());
        }

        createReq.setDurationMinutes(payload.containsKey("durationMinutes") ? 
                Integer.parseInt(payload.get("durationMinutes").toString()) : defaultDuration);

        // Appel de la méthode interne sans JWT
        var response = appointmentService.createAppointmentForCompany(createReq, company);

        return AiWebhookResponse.ok("Rendez-vous confirmé", response);
    }
}
