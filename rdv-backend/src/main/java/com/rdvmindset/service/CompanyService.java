package com.rdvmindset.service;

import com.rdvmindset.dto.CompanyCreateRequest;
import com.rdvmindset.dto.CompanyResponse;
import com.rdvmindset.entity.Company;
import com.rdvmindset.entity.Industry;
import com.rdvmindset.entity.User;
import com.rdvmindset.entity.enums.UserRole;
import com.rdvmindset.repository.CompanyRepository;
import com.rdvmindset.repository.IndustryRepository;
import com.rdvmindset.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final IndustryRepository industryRepository;
    private final UserRepository userRepository;
    private final KeycloakAdminService keycloakAdminService;

    @Transactional
    public CompanyResponse createCompany(CompanyCreateRequest request) {
        // 1. Vérifier si l'email de l'entreprise existe déjà
        if (companyRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Une entreprise avec cet email existe déjà");
        }
        
        // Vérifier l'email du propriétaire également
        // (En supposant que l'email de l'entreprise et du proprio peuvent être les mêmes, 
        // l'unicité de l'email du User est requise)
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Un utilisateur avec cet email existe déjà");
        }

        // 2. Créer l'entreprise
        Company company = new Company();
        company.setName(request.getCompanyName());
        company.setEmail(request.getEmail());
        company.setPhone(request.getPhone());
        company.setSubscriptionPlan("STANDARD"); // Plan par défaut
        company.setActive(true);
        company.setCreatedAt(LocalDateTime.now());

        // Rattacher les secteurs (Industries)
        if (request.getIndustryIds() != null && !request.getIndustryIds().isEmpty()) {
            List<Industry> industries = industryRepository.findAllById(request.getIndustryIds());
            company.setIndustries(industries);
        }

        // Sauvegarder l'entreprise pour générer son ID
        company = companyRepository.save(company);

        // 3. Créer l'utilisateur dans Keycloak
        log.info("Création de l'utilisateur Keycloak pour {}", request.getEmail());
        UUID keycloakId = keycloakAdminService.createUser(
                request.getEmail(),
                request.getOwnerFirstName(),
                request.getOwnerLastName(),
                request.getOwnerPassword()
        );

        // 4. Créer l'utilisateur (Propriétaire) en base de données
        User owner = new User();
        owner.setCompany(company);
        owner.setEmail(request.getEmail()); // L'email de l'entreprise est utilisé comme identifiant de connexion par défaut
        owner.setKeycloakId(keycloakId);
        owner.setRole(UserRole.OWNER); // Le premier utilisateur est le propriétaire
        owner.setCreatedAt(LocalDateTime.now());

        userRepository.save(owner);
        
        log.info("Entreprise {} et propriétaire créés avec succès", company.getName());

        return CompanyResponse.fromEntity(company);
    }
}
