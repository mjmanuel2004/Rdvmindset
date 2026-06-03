package com.rdvmindset.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "agent_configs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AgentConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false, unique = true)
    private Agent agent;

    private String tone = "PROFESSIONAL";

    @Column(columnDefinition = "TEXT")
    private String faq;

    @Column(columnDefinition = "TEXT")
    private String pricing;

    @Column(name = "appointment_duration_minutes")
    private int appointmentDurationMinutes = 30;

    @Column(name = "model_industry")
    private String modelIndustry;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PreUpdate
    @PrePersist
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
