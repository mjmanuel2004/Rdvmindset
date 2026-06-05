package com.rdvmindset.controller;

import com.rdvmindset.dto.CompanyCreateRequest;
import com.rdvmindset.dto.CompanyResponse;
import com.rdvmindset.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    /**
     * Endpoint d'inscription d'une nouvelle entreprise.
     * Accessible publiquement (non sécurisé par Keycloak) pour permettre l'onboarding.
     */
    @PostMapping("/register")
    public ResponseEntity<CompanyResponse> registerCompany(@Valid @RequestBody CompanyCreateRequest request) {
        CompanyResponse response = companyService.createCompany(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
