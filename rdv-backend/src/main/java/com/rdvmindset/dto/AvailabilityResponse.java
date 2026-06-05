package com.rdvmindset.dto;

import com.rdvmindset.entity.Availability;
import lombok.Data;

import java.time.LocalTime;
import java.util.UUID;

@Data
public class AvailabilityResponse {

    private UUID id;
    private int dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;
    private int maxCapacity;

    public static AvailabilityResponse fromEntity(Availability availability) {
        AvailabilityResponse response = new AvailabilityResponse();
        response.setId(availability.getId());
        response.setDayOfWeek(availability.getDayOfWeek());
        response.setStartTime(availability.getStartTime());
        response.setEndTime(availability.getEndTime());
        response.setMaxCapacity(availability.getMaxCapacity());
        return response;
    }
}
