package com.rdvmindset.service;

import com.rdvmindset.dto.AgentConfigUpdateRequest;
import com.rdvmindset.dto.AgentCreateRequest;
import com.rdvmindset.dto.AgentResponse;
import com.rdvmindset.entity.Agent;
import com.rdvmindset.entity.AgentConfig;
import com.rdvmindset.entity.Company;
import com.rdvmindset.repository.AgentConfigRepository;
import com.rdvmindset.repository.AgentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AgentService {

    private final AgentRepository agentRepository;
    private final AgentConfigRepository agentConfigRepository;
    private final UserService userService;

    public List<AgentResponse> getAgents() {
        Company company = getCompanyOfCurrentUser();
        return agentRepository.findByCompanyId(company.getId()).stream()
                .map(AgentResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public AgentResponse createAgent(AgentCreateRequest request) {
        Company company = getCompanyOfCurrentUser();

        Agent agent = new Agent();
        agent.setCompany(company);
        agent.setName(request.getName());
        agent.setType(request.getType());
        agent.setPhoneNumber(request.getPhoneNumber());
        agent.setActive(true);

        AgentConfig config = new AgentConfig();
        config.setAgent(agent);
        config.setModelIndustry(request.getModelIndustry());
        config.setTone("PROFESSIONAL");
        config.setAppointmentDurationMinutes(30);

        // Initialisation d'un prompt de base
        agent.setSystemPrompt(generateDefaultPrompt(company.getName(), request.getModelIndustry(), config));
        
        agent.setConfig(config);

        agent = agentRepository.save(agent);
        
        log.info("Agent '{}' créé pour l'entreprise '{}'", agent.getName(), company.getName());
        
        return AgentResponse.fromEntity(agent);
    }

    @Transactional
    public AgentResponse updateAgentConfig(UUID agentId, AgentConfigUpdateRequest request) {
        Company company = getCompanyOfCurrentUser();
        
        Agent agent = agentRepository.findById(agentId)
                .orElseThrow(() -> new IllegalArgumentException("Agent introuvable"));
                
        if (!agent.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("Cet agent n'appartient pas à votre entreprise");
        }

        AgentConfig config = agent.getConfig();
        if (config == null) {
            config = new AgentConfig();
            config.setAgent(agent);
            agent.setConfig(config);
        }

        if (request.getTone() != null) config.setTone(request.getTone());
        if (request.getFaq() != null) config.setFaq(request.getFaq());
        if (request.getPricing() != null) config.setPricing(request.getPricing());
        if (request.getAppointmentDurationMinutes() != null) config.setAppointmentDurationMinutes(request.getAppointmentDurationMinutes());

        // Si l'utilisateur fournit explicitement un System Prompt, on l'utilise
        // Sinon, on le regénère avec la nouvelle FAQ et les Prix
        if (request.getSystemPrompt() != null && !request.getSystemPrompt().trim().isEmpty()) {
            agent.setSystemPrompt(request.getSystemPrompt());
        } else {
            agent.setSystemPrompt(generateDefaultPrompt(company.getName(), config.getModelIndustry(), config));
        }

        agentConfigRepository.save(config);
        agent = agentRepository.save(agent);

        log.info("Configuration de l'agent '{}' mise à jour", agent.getName());
        
        return AgentResponse.fromEntity(agent);
    }

    private String generateDefaultPrompt(String companyName, String industry, AgentConfig config) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Tu es un assistant ").append(config.getTone()).append(" pour l'entreprise ").append(companyName).append(".\n");
        if (industry != null && !industry.isEmpty()) {
            prompt.append("Secteur d'activité : ").append(industry).append(".\n");
        }
        prompt.append("Ton rôle est d'accueillir les clients et de répondre à leurs questions.\n");
        
        if (config.getFaq() != null && !config.getFaq().isEmpty()) {
            prompt.append("\n=== FAQ ===\n").append(config.getFaq()).append("\n");
        }
        
        if (config.getPricing() != null && !config.getPricing().isEmpty()) {
            prompt.append("\n=== TARIFS ===\n").append(config.getPricing()).append("\n");
        }
        
        return prompt.toString();
    }

    private Company getCompanyOfCurrentUser() {
        Company company = userService.getCurrentUser().getCompany();
        if (company == null) {
            throw new IllegalStateException("L'utilisateur n'est rattaché à aucune entreprise");
        }
        return company;
    }
}
