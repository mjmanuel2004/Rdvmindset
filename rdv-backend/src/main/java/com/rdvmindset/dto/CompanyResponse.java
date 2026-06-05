package com.rdvmindset.dto;

import com.rdvmindset.entity.Company;
import com.rdvmindset.entity.Industry;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Data
public class CompanyResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String subscriptionPlan;
    private boolean active;
    private LocalDateTime createdAt;
    private List<String> industries;

    public static CompanyResponse fromEntity(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setEmail(company.getEmail());
        response.setPhone(company.getPhone());
        response.setSubscriptionPlan(company.getSubscriptionPlan());
        response.setActive(company.isActive());
        response.setCreatedAt(company.getCreatedAt());
        
        if (company.getIndustries() != null) {
            response.setIndustries(company.getIndustries().stream()
                    .map(Industry::getName)
                    .collect(Collectors.toList()));
        }
        
        return response;
    }
}
