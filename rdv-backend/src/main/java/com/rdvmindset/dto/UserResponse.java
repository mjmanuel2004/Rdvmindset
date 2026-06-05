package com.rdvmindset.dto;

import com.rdvmindset.entity.User;
import com.rdvmindset.entity.enums.UserRole;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class UserResponse {
    private UUID id;
    private UUID companyId;
    private String email;
    private UserRole role;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setCompanyId(user.getCompany() != null ? user.getCompany().getId() : null);
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }
}
