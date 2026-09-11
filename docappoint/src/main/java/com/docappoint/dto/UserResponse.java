package com.docappoint.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

public record UserResponse(
    @JsonProperty("id")
    Integer id,

    @JsonProperty("username")
    @JsonAlias({"username", "name"})
    String username
) {}
