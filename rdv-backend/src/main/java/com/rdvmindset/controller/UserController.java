package com.rdvmindset.controller;

import com.rdvmindset.dto.EmployeeInviteRequest;
import com.rdvmindset.dto.UserResponse;
import com.rdvmindset.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Récupère le profil de l'utilisateur actuellement connecté.
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUserProfile() {
        return ResponseEntity.ok(UserResponse.fromEntity(userService.getCurrentUser()));
    }

    /**
     * Permet à un OWNER ou ADMIN d'inviter un nouvel employé (MANAGER ou AGENT) dans son entreprise.
     */
    @PostMapping("/invite")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<UserResponse> inviteEmployee(@Valid @RequestBody EmployeeInviteRequest request) {
        UserResponse response = userService.inviteEmployee(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
}
