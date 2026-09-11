package com.docappoint.entity;

public enum Role {
    doctor,
    patient;

    public static final Role DOCTOR = doctor;
    public static final Role PATIENT = patient;

    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        for (Role r : Role.values()) {
            if (r.name().equalsIgnoreCase(value)) {
                return r;
            }
        }
        throw new IllegalArgumentException("Unknown Role value: " + value);
    }
}
