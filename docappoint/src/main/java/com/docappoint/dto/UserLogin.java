package com.docappoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UserLogin(
    @NotBlank(message = "Phone number is required")
    @JsonProperty("phone")
    String phone,

    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    String password
) {}
