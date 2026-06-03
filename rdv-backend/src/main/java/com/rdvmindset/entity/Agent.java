package com.rdvmindset.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "agents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type; // VOCAL or CHATBOT

    @Column(name = "vapi_assistant_id")
    private String vapiAssistantId;

    @Column(name = "botpress_bot_id")
    private String botpressBotId;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "system_prompt", columnDefinition = "TEXT")
    private String systemPrompt;

    private boolean active = true;

    @OneToOne(mappedBy = "agent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AgentConfig config;

    @OneToMany(mappedBy = "agent")
    private List<Appointment> appointments;
}
