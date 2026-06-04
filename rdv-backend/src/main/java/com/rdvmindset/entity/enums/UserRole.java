package com.rdvmindset.entity.enums;

public enum UserRole {
    OWNER,   // Créateur du compte, gère l'abonnement et la facturation
    ADMIN,   // Administrateur, peut inviter des utilisateurs et configurer l'entreprise
    MANAGER, // Superviseur, peut voir les stats mais pas toucher à la facturation
    AGENT    // Utilisateur de base, gère ses propres RDVs et le bot
}
