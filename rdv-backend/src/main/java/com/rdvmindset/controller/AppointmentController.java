package com.rdvmindset.controller;

import com.rdvmindset.dto.AppointmentCreateRequest;
import com.rdvmindset.dto.AppointmentResponse;
import com.rdvmindset.entity.Company;
import com.rdvmindset.service.AppointmentService;
import com.rdvmindset.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final UserService userService; // Utilisé ici juste pour récupérer l'ID de l'entreprise facilement dans le GET

    /**
     * Crée un nouveau rendez-vous.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<AppointmentResponse> createAppointment(@Valid @RequestBody AppointmentCreateRequest request) {
        AppointmentResponse response = appointmentService.createAppointment(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Récupère les créneaux horaires disponibles pour une date donnée.
     * Accessible à tous les employés de l'entreprise.
     */
    @GetMapping("/available-slots")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'MANAGER', 'AGENT')")
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @RequestParam("date") String dateString,
            @RequestParam(value = "duration", defaultValue = "30") int durationMinutes) {
        
        LocalDate date = LocalDate.parse(dateString);
        Company company = userService.getCurrentUser().getCompany();

        if (company == null) {
            return ResponseEntity.badRequest().build();
        }

        List<LocalTime> availableSlots = appointmentService.getAvailableSlots(company.getId(), date, durationMinutes);
        return ResponseEntity.ok(availableSlots);
    }
}
