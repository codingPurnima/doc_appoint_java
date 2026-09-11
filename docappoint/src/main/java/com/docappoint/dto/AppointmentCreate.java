package com.docappoint.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;

public record AppointmentCreate(
    @NotNull(message = "Slot ID is required")
    @JsonProperty("slot_id")
    Integer slotId
) {}
