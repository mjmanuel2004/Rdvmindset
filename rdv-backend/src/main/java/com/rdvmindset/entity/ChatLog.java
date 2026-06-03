package com.rdvmindset.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "chat_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "appointment_id")
    private Appointment appointment;

    @Column(name = "botpress_session_id", nullable = false)
    private String botpressSessionId;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "messages_json", columnDefinition = "JSONB")
    private String messagesJson;

    private String status;

    @PrePersist
    protected void onCreate() {
        if (startDate == null) startDate = LocalDateTime.now();
    }
}
