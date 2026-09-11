package com.docappoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record BreakInterval(
    @NotNull(message = "Break start time is required")
    @JsonProperty("start")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime start,

    @NotNull(message = "Break end time is required")
    @JsonProperty("end")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime end
) {}
