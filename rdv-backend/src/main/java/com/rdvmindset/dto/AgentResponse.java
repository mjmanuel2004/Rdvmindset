package com.rdvmindset.dto;

import com.rdvmindset.entity.Agent;
import lombok.Data;

import java.util.UUID;

@Data
public class AgentResponse {
    private UUID id;
    private String name;
    private String type;
    private String vapiAssistantId;
    private String botpressBotId;
    private String phoneNumber;
    private String systemPrompt;
    private boolean active;
    
    // Config fields
    private String tone;
    private String faq;
    private String pricing;
    private int appointmentDurationMinutes;
    private String modelIndustry;

    public static AgentResponse fromEntity(Agent agent) {
        AgentResponse response = new AgentResponse();
        response.setId(agent.getId());
        response.setName(agent.getName());
        response.setType(agent.getType());
        response.setVapiAssistantId(agent.getVapiAssistantId());
        response.setBotpressBotId(agent.getBotpressBotId());
        response.setPhoneNumber(agent.getPhoneNumber());
        response.setSystemPrompt(agent.getSystemPrompt());
        response.setActive(agent.isActive());

        if (agent.getConfig() != null) {
            response.setTone(agent.getConfig().getTone());
            response.setFaq(agent.getConfig().getFaq());
            response.setPricing(agent.getConfig().getPricing());
            response.setAppointmentDurationMinutes(agent.getConfig().getAppointmentDurationMinutes());
            response.setModelIndustry(agent.getConfig().getModelIndustry());
        }

        return response;
    }
}
