package com.rdvmindset.service;

import com.rdvmindset.dto.AvailabilityBulkRequest;
import com.rdvmindset.dto.AvailabilityRequest;
import com.rdvmindset.dto.AvailabilityResponse;
import com.rdvmindset.entity.Availability;
import com.rdvmindset.entity.Company;
import com.rdvmindset.entity.User;
import com.rdvmindset.repository.AvailabilityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final UserService userService;

    /**
     * Récupère toutes les disponibilités de l'entreprise de l'utilisateur connecté.
     */
    public List<AvailabilityResponse> getAvailabilities() {
        User currentUser = userService.getCurrentUser();
        Company company = currentUser.getCompany();

        if (company == null) {
            throw new IllegalStateException("L'utilisateur n'est rattaché à aucune entreprise");
        }

        return availabilityRepository.findByCompanyId(company.getId())
                .stream()
                .map(AvailabilityResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Remplace intégralement les disponibilités de l'entreprise par la nouvelle liste.
     */
    @Transactional
    public List<AvailabilityResponse> saveBulkAvailabilities(AvailabilityBulkRequest request) {
        User currentUser = userService.getCurrentUser();
        Company company = currentUser.getCompany();

        if (company == null) {
            throw new IllegalStateException("L'utilisateur n'est rattaché à aucune entreprise");
        }

        log.info("Remplacement des disponibilités pour l'entreprise: {}", company.getName());

        // 1. Supprimer toutes les anciennes plages horaires
        availabilityRepository.deleteByCompanyId(company.getId());

        // 2. Créer les nouvelles plages horaires
        List<Availability> newAvailabilities = new ArrayList<>();
        
        for (AvailabilityRequest req : request.getAvailabilities()) {
            if (req.getStartTime().isAfter(req.getEndTime()) || req.getStartTime().equals(req.getEndTime())) {
                throw new IllegalArgumentException("L'heure de début doit être antérieure à l'heure de fin");
            }

            Availability availability = new Availability();
            availability.setCompany(company);
            availability.setDayOfWeek(req.getDayOfWeek());
            availability.setStartTime(req.getStartTime());
            availability.setEndTime(req.getEndTime());
            availability.setMaxCapacity(req.getMaxCapacity());
            
            newAvailabilities.add(availability);
        }

        // 3. Sauvegarder en bloc
        List<Availability> saved = availabilityRepository.saveAll(newAvailabilities);

        return saved.stream()
                .map(AvailabilityResponse::fromEntity)
                .collect(Collectors.toList());
    }
}
