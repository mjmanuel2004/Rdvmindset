package com.rdvmindset.controller;

import com.rdvmindset.dto.AgentConfigUpdateRequest;
import com.rdvmindset.dto.AgentCreateRequest;
import com.rdvmindset.dto.AgentResponse;
import com.rdvmindset.service.AgentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agents")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    @GetMapping
    public ResponseEntity<List<AgentResponse>> getAgents() {
        return ResponseEntity.ok(agentService.getAgents());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<AgentResponse> createAgent(@Valid @RequestBody AgentCreateRequest request) {
        AgentResponse response = agentService.createAgent(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/config")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<AgentResponse> updateAgentConfig(
            @PathVariable UUID id,
            @Valid @RequestBody AgentConfigUpdateRequest request) {
        AgentResponse response = agentService.updateAgentConfig(id, request);
        return ResponseEntity.ok(response);
    }
}
