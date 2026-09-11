package com.docappoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record SlotGenerateRequest(
    @NotNull(message = "Date is required")
    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,

    @NotNull(message = "Day start time is required")
    @JsonProperty("day_start")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime dayStart,

    @NotNull(message = "Day end time is required")
    @JsonProperty("day_end")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime dayEnd,

    @NotNull(message = "Slot duration is required")
    @Min(value = 1, message = "Slot duration must be at least 1 minute")
    @JsonProperty("slot_duration_minutes")
    Integer slotDurationMinutes,

    @Valid
    @JsonProperty("breaks")
    List<BreakInterval> breaks
) {
    public SlotGenerateRequest {
        if (breaks == null) {
            breaks = List.of();
        }
    }
}
