package com.rdvmindset.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;
import com.rdvmindset.security.EncryptionConverter;

@Entity
@Table(name = "calendar_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CalendarToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false, unique = true)
    private Company company;

    @Column(nullable = false)
    private String provider; // GOOGLE or OUTLOOK

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "encrypted_access_token", nullable = false)
    private String accessToken;

    @Convert(converter = EncryptionConverter.class)
    @Column(name = "encrypted_refresh_token", nullable = false)
    private String refreshToken;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
