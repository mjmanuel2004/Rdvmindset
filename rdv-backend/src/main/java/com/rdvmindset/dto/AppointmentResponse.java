package com.rdvmindset.dto;

import com.rdvmindset.entity.Appointment;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class AppointmentResponse {
    
    private UUID id;
    private LocalDateTime dateTime;
    private int durationMinutes;
    private String status;
    private String reason;
    private String notes;
    private LocalDateTime createdAt;
    
    // Client info
    private UUID clientId;
    private String clientFirstName;
    private String clientLastName;
    private String clientPhone;
    
    // Agent info
    private UUID agentId;
    private String agentName;

    public static AppointmentResponse fromEntity(Appointment appointment) {
        AppointmentResponse response = new AppointmentResponse();
        response.setId(appointment.getId());
        response.setDateTime(appointment.getDateTime());
        response.setDurationMinutes(appointment.getDurationMinutes());
        response.setStatus(appointment.getStatus());
        response.setReason(appointment.getReason());
        response.setNotes(appointment.getNotes());
        response.setCreatedAt(appointment.getCreatedAt());

        if (appointment.getClient() != null) {
            response.setClientId(appointment.getClient().getId());
            response.setClientFirstName(appointment.getClient().getFirstName());
            response.setClientLastName(appointment.getClient().getLastName());
            response.setClientPhone(appointment.getClient().getPhone());
        }

        if (appointment.getAgent() != null) {
            response.setAgentId(appointment.getAgent().getId());
            response.setAgentName(appointment.getAgent().getName());
        }

        return response;
    }
}
