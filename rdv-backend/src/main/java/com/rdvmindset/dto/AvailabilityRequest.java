package com.rdvmindset.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalTime;

@Data
public class AvailabilityRequest {

    @Min(value = 1, message = "Le jour de la semaine doit être entre 1 (Lundi) et 7 (Dimanche)")
    @Max(value = 7, message = "Le jour de la semaine doit être entre 1 (Lundi) et 7 (Dimanche)")
    private int dayOfWeek;

    @NotNull(message = "L'heure de début est obligatoire")
    private LocalTime startTime;

    @NotNull(message = "L'heure de fin est obligatoire")
    private LocalTime endTime;

    @Min(value = 1, message = "La capacité maximale doit être d'au moins 1")
    private int maxCapacity = 1;
}
