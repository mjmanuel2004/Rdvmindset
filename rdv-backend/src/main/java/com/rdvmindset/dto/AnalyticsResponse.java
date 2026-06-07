package com.rdvmindset.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsResponse {
    private long totalAppointments;
    private long confirmedAppointments;
    private long cancelledAppointments;
    private long totalClients;
}
