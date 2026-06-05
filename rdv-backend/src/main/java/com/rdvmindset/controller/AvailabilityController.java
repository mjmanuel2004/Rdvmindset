package com.rdvmindset.controller;

import com.rdvmindset.dto.AvailabilityBulkRequest;
import com.rdvmindset.dto.AvailabilityResponse;
import com.rdvmindset.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/availabilities")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    /**
     * Récupère la liste des disponibilités de l'entreprise.
     * Tout utilisateur connecté (OWNER, ADMIN, MANAGER, AGENT) peut voir les horaires.
     */
    @GetMapping
    public ResponseEntity<List<AvailabilityResponse>> getAvailabilities() {
        return ResponseEntity.ok(availabilityService.getAvailabilities());
    }

    /**
     * Remplace toutes les disponibilités de l'entreprise par la nouvelle configuration.
     * Seuls les rôles d'administration (OWNER, ADMIN, MANAGER) peuvent modifier.
     */
    @PutMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER')")
    public ResponseEntity<List<AvailabilityResponse>> updateAvailabilities(
            @Valid @RequestBody AvailabilityBulkRequest request) {
        
        List<AvailabilityResponse> responses = availabilityService.saveBulkAvailabilities(request);
        return ResponseEntity.ok(responses);
    }
}
