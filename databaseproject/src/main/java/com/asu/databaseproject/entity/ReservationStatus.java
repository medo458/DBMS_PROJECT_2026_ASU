package com.asu.databaseproject.entity;

/**
 * Maps the PostgreSQL enum type defined in V1.sql.
 * Constant names match the database labels exactly (lowercase).
 */
public enum ReservationStatus {
    active, cancelled, fulfilled
}
