package com.rdvmindset.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class AvailabilityBulkRequest {
    
    @NotNull(message = "La liste des disponibilités ne peut pas être nulle")
    @Valid
    private List<AvailabilityRequest> availabilities;
}
