package com.rdvmindset.service;

import com.rdvmindset.dto.AnalyticsResponse;
import com.rdvmindset.entity.Company;
import com.rdvmindset.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final AppointmentRepository appointmentRepository;
    private final UserService userService;

    public AnalyticsResponse getDashboardAnalytics() {
        Company company = userService.getCurrentUser().getCompany();
        if (company == null) {
            throw new IllegalStateException("L'utilisateur n'est rattaché à aucune entreprise");
        }

        UUID companyId = company.getId();
        
        long totalAppointments = appointmentRepository.countByCompanyId(companyId);
        long confirmedAppointments = appointmentRepository.countByCompanyIdAndStatus(companyId, "CONFIRMED");
        long cancelledAppointments = appointmentRepository.countByCompanyIdAndStatus(companyId, "CANCELED");
        long totalClients = appointmentRepository.countDistinctClientsByCompanyId(companyId);

        log.info("Récupération des statistiques pour l'entreprise {}", company.getName());

        return new AnalyticsResponse(
                totalAppointments,
                confirmedAppointments,
                cancelledAppointments,
                totalClients
        );
    }
}
