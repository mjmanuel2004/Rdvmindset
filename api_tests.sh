#!/bin/bash

# ==========================================
# RdvMindset API Tests
# ==========================================
# Exécutez ces commandes pour tester l'API.
# ==========================================

# 1. Création d'une entreprise et du compte OWNER
echo "--- 1. Inscription Entreprise ---"
curl -X POST http://localhost:8080/api/v1/companies/register \
-H "Content-Type: application/json" \
-d '{
  "companyName": "Tech Agency",
  "email": "owner@techagency.com",
  "phone": "0601020304",
  "industryIds": [],
  "ownerFirstName": "Alice",
  "ownerLastName": "Smith",
  "ownerPassword": "Password123!"
}'
echo -e "\n\n"

# 2. Récupération du Token JWT via Keycloak (Utilise les identifiants créés ci-dessus)
echo "--- 2. Récupération Token JWT ---"
TOKEN=$(curl -s -X POST http://localhost:9090/realms/rdvmindset/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "username=owner@techagency.com" \
  -d "password=Password123!" \
  -d "grant_type=password" \
  -d "client_id=account-console" | grep -o '"access_token":"[^"]*' | cut -d'"' -f4)

echo "TOKEN = $TOKEN"
echo -e "\n"

# 3. Invitation d'un employé (Nécessite le token du OWNER)
echo "--- 3. Invitation d'un Employé ---"
curl -X POST http://localhost:8080/api/v1/users/invite \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "email": "agent@techagency.com",
    "firstName": "Bob",
    "lastName": "Lagent",
    "role": "AGENT"
  }'
echo -e "\n\n"

# 4. Configuration des disponibilités de l'entreprise
echo "--- 4. Configuration des Horaires ---"
curl -X PUT http://localhost:8080/api/v1/availabilities \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "availabilities": [
      {
        "dayOfWeek": 1,
        "startTime": "09:00:00",
        "endTime": "12:00:00",
        "maxCapacity": 2
      },
      {
        "dayOfWeek": 1,
        "startTime": "14:00:00",
        "endTime": "18:00:00",
        "maxCapacity": 2
      }
    ]
  }'
echo -e "\n\n"

# 5. Création d'un Agent IA (Vocal)
echo "--- 5. Création Agent IA ---"
AGENT_RES=$(curl -s -X POST http://localhost:8080/api/v1/agents \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "name": "Assistante Sarah",
    "type": "VOCAL",
    "phoneNumber": "+33123456789",
    "modelIndustry": "Cabinet Médical"
  }')
echo "Réponse Agent: $AGENT_RES"

AGENT_ID=$(echo $AGENT_RES | grep -o '"id":"[^"]*' | cut -d'"' -f4)
echo -e "\n\n"

# 6. Mise à jour de la configuration de l'Agent IA (FAQ et Tone)
echo "--- 6. Update Agent Config ---"
curl -X PUT http://localhost:8080/api/v1/agents/$AGENT_ID/config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "tone": "SYMPATHIQUE",
    "faq": "Le docteur ne prend pas la carte vitale. Le cabinet est au 2eme etage.",
    "pricing": "Consultation: 50 euros",
    "appointmentDurationMinutes": 20
  }'
echo -e "\n\n"
