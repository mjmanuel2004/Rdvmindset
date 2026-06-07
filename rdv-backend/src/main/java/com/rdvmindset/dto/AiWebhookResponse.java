package com.rdvmindset.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiWebhookResponse {
    private boolean success;
    private String message;
    private Object data;

    public static AiWebhookResponse ok(String message, Object data) {
        return new AiWebhookResponse(true, message, data);
    }

    public static AiWebhookResponse error(String message) {
        return new AiWebhookResponse(false, message, null);
    }
}
