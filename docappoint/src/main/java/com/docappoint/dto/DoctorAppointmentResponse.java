package com.docappoint.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDate;
import java.time.LocalTime;

public record DoctorAppointmentResponse(
    @JsonProperty("appointment_id")
    Integer appointmentId,

    @JsonProperty("date")
    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate date,

    @JsonProperty("start_time")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime startTime,

    @JsonProperty("end_time")
    @JsonFormat(pattern = "HH:mm:ss")
    LocalTime endTime,

    @JsonProperty("status")
    String status,

    @JsonProperty("patient_name")
    String patientName
) {}
