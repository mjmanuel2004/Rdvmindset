-- Script d'initialisation PostgreSQL
-- Crée le schéma dédié pour Keycloak afin qu'il ne mélange
-- pas ses tables avec celles de l'application RdvMindset.
CREATE SCHEMA IF NOT EXISTS keycloak;
