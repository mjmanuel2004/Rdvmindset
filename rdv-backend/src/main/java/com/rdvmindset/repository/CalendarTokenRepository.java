package com.rdvmindset.repository;

import com.rdvmindset.entity.CalendarToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CalendarTokenRepository extends JpaRepository<CalendarToken, UUID> {
    Optional<CalendarToken> findByCompanyId(UUID companyId);
}
