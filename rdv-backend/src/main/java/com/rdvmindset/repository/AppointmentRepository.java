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
}
