package com.rdvmindset.service;

import com.rdvmindset.dto.AppointmentCreateRequest;
import com.rdvmindset.dto.AppointmentResponse;
import com.rdvmindset.entity.*;
import com.rdvmindset.repository.AgentRepository;
import com.rdvmindset.repository.AppointmentRepository;
import com.rdvmindset.repository.AvailabilityRepository;
import com.rdvmindset.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final ClientRepository clientRepository;
    private final AvailabilityRepository availabilityRepository;
    private final AgentRepository agentRepository;
    private final UserService userService;
    private final WebSocketNotificationService notificationService;

    /**
     * Recherche un client par téléphone ou email, ou le crée s'il n'existe pas.
     */
    private Client findOrCreateClient(String firstName, String lastName, String phone, String email) {
        if (phone != null && !phone.isEmpty()) {
            var clientOpt = clientRepository.findByPhone(phone);
            if (clientOpt.isPresent()) return clientOpt.get();
        }
        if (email != null && !email.isEmpty()) {
            var clientOpt = clientRepository.findByEmail(email);
            if (clientOpt.isPresent()) return clientOpt.get();
        }

        Client newClient = new Client();
        newClient.setFirstName(firstName);
        newClient.setLastName(lastName);
        newClient.setPhone(phone);
        newClient.setEmail(email);
        return clientRepository.save(newClient);
    }

    /**
     * Calcule les créneaux disponibles pour une entreprise, à une date donnée.
     * Prend en compte la durée du créneau souhaitée, les horaires d'ouverture et les RDV existants.
     */
    public List<LocalTime> getAvailableSlots(UUID companyId, LocalDate date, int durationMinutes) {
        int dayOfWeek = date.getDayOfWeek().getValue(); // 1=Lundi, 7=Dimanche

        // 1. Récupérer les plages horaires d'ouverture pour ce jour précis
        List<Availability> availabilities = availabilityRepository.findByCompanyId(companyId)
                .stream()
                .filter(a -> a.getDayOfWeek() == dayOfWeek)
                .collect(Collectors.toList());

        if (availabilities.isEmpty()) {
            return new ArrayList<>(); // L'entreprise est fermée ce jour-là
        }

        // 2. Récupérer les rendez-vous déjà existants ce jour-là
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> existingAppointments = appointmentRepository.findByCompanyIdAndDateTimeBetween(companyId, startOfDay, endOfDay);

        List<LocalTime> availableSlots = new ArrayList<>();

        // 3. Découper chaque plage horaire d'ouverture en créneaux
        for (Availability availability : availabilities) {
            LocalTime slotTime = availability.getStartTime();
            
            while (slotTime.plusMinutes(durationMinutes).isBefore(availability.getEndTime()) 
                    || slotTime.plusMinutes(durationMinutes).equals(availability.getEndTime())) {
                
                final LocalTime currentSlot = slotTime;
                
                // Compter combien de rendez-vous chevauchent ce créneau
                long overlappingCount = existingAppointments.stream().filter(appt -> {
                    LocalTime apptStart = appt.getDateTime().toLocalTime();
                    LocalTime apptEnd = apptStart.plusMinutes(appt.getDurationMinutes());
                    LocalTime slotEnd = currentSlot.plusMinutes(durationMinutes);
                    
                    // Vérifier si [apptStart, apptEnd] chevauche [currentSlot, slotEnd]
                    return apptStart.isBefore(slotEnd) && apptEnd.isAfter(currentSlot);
                }).count();

                // S'il reste de la capacité, on ajoute ce créneau aux disponibilités
                if (overlappingCount < availability.getMaxCapacity()) {
                    availableSlots.add(currentSlot);
                }

                // Avancer au créneau suivant
                slotTime = slotTime.plusMinutes(durationMinutes);
            }
        }

        return availableSlots;
    }

    /**
     * Crée un nouveau rendez-vous.
     */
    @Transactional
    public AppointmentResponse createAppointment(AppointmentCreateRequest request) {
        User currentUser = userService.getCurrentUser();
        Company company = currentUser.getCompany();

        if (company == null) {
            throw new IllegalStateException("L'utilisateur n'est rattaché à aucune entreprise");
        }

        // 1. Vérification de disponibilité stricte
        List<LocalTime> availableSlots = getAvailableSlots(company.getId(), request.getDateTime().toLocalDate(), request.getDurationMinutes());
        if (!availableSlots.contains(request.getDateTime().toLocalTime())) {
            throw new IllegalArgumentException("Ce créneau n'est pas disponible ou l'entreprise est fermée à cette heure.");
        }

        // 2. Création ou récupération du client
        Client client = findOrCreateClient(
                request.getClientFirstName(),
                request.getClientLastName(),
                request.getClientPhone(),
                request.getClientEmail()
        );

        // 3. Récupération de l'Agent IA si spécifié (ex: pris par Vapi au téléphone)
        Agent agent = null;
        if (request.getAgentId() != null) {
            agent = agentRepository.findById(request.getAgentId())
                    .orElseThrow(() -> new IllegalArgumentException("Agent IA introuvable"));
            if (!agent.getCompany().getId().equals(company.getId())) {
                throw new IllegalArgumentException("Cet Agent IA n'appartient pas à votre entreprise.");
            }
        }

        // 4. Enregistrement du rendez-vous
        Appointment appointment = new Appointment();
        appointment.setCompany(company);
        appointment.setClient(client);
        appointment.setAgent(agent);
        appointment.setDateTime(request.getDateTime());
        appointment.setDurationMinutes(request.getDurationMinutes());
        appointment.setReason(request.getReason());
        appointment.setNotes(request.getNotes());
        appointment.setStatus("CONFIRMED"); // Pour l'instant on confirme direct

        appointment = appointmentRepository.save(appointment);

        log.info("Rendez-vous créé le {} pour le client {} {}", appointment.getDateTime(), client.getFirstName(), client.getLastName());

        AppointmentResponse response = AppointmentResponse.fromEntity(appointment);
        
        // 5. Envoi de la notification WebSocket en temps réel
        notificationService.notifyNewAppointment(company.getId(), response);

        return response;
    }
}
