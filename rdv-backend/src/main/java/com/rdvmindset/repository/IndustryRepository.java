package com.rdvmindset.repository;

import com.rdvmindset.entity.Industry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IndustryRepository extends JpaRepository<Industry, UUID> {
    Optional<Industry> findByName(String name);
}
