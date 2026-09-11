package com.docappoint.entity;

public enum Status {
    available,
    booked,
    completed,
    cancelled,
    frozen;

    public static final Status AVAILABLE = available;
    public static final Status BOOKED = booked;
    public static final Status COMPLETED = completed;
    public static final Status CANCELLED = cancelled;
    public static final Status FROZEN = frozen;

    public static Status fromString(String value) {
        if (value == null) {
            return null;
        }
        for (Status s : Status.values()) {
            if (s.name().equalsIgnoreCase(value)) {
                return s;
            }
        }
        throw new IllegalArgumentException("Unknown Status value: " + value);
    }
}
