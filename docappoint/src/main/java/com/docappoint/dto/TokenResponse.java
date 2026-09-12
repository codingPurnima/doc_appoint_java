package com.docappoint.dto;

import com.docappoint.entity.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TokenResponse(
    @JsonProperty("access_token")
    String accessToken,

    @JsonProperty("refresh_token")
    String refreshToken,

    @JsonProperty("token_type")
    String tokenType,

    @JsonProperty("role")
    Role role
) {
    public TokenResponse(String accessToken, String tokenType) {
        this(accessToken, null, tokenType, null);
    }
}
