package com.rdvmindset.repository;

import com.rdvmindset.entity.ChatLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ChatLogRepository extends JpaRepository<ChatLog, UUID> {
}
