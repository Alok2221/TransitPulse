package com.transitpulse.dto;

import jakarta.validation.constraints.NotBlank;

public class AgentAdviseRequest {

    @NotBlank
    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
