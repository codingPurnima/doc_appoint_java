package com.docappoint.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;

public record UserLogin(
    @NotBlank(message = "Username is required")
    @JsonProperty("username")
    @JsonAlias({"username", "name", "phone"})
    String username,

    @NotBlank(message = "Password is required")
    @JsonProperty("password")
    String password
) {}
