package com.rdvmindset.repository;

import com.rdvmindset.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByCompanyId(UUID companyId);
    List<Appointment> findByClientId(UUID clientId);
    
    // Utilisé pour vérifier les disponibilités d'une journée
    List<Appointment> findByCompanyIdAndDateTimeBetween(UUID companyId, java.time.LocalDateTime start, java.time.LocalDateTime end);

    // Utilisé pour récupérer l'agenda à venir
    List<Appointment> findByCompanyIdAndDateTimeAfterOrderByDateTimeAsc(UUID companyId, java.time.LocalDateTime date);

    // Statistiques analytiques
    long countByCompanyId(UUID companyId);
    long countByCompanyIdAndStatus(UUID companyId, String status);
    
    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT a.client.id) FROM Appointment a WHERE a.company.id = :companyId")
    long countDistinctClientsByCompanyId(@org.springframework.data.repository.query.Param("companyId") UUID companyId);
}
