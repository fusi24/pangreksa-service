package com.pangreksa.service.model.exception;

import lombok.Getter;

/**
 * Thrown when a mobile check-in/out coordinate falls outside the configured office geofence buffer.
 */
@Getter
public class AttendanceOutsideGeofenceException extends DomainException {

    private final Long personId;
    private final double latitude;
    private final double longitude;
    private final Long officeLocationId;

    public AttendanceOutsideGeofenceException(Long personId, double latitude, double longitude, Long officeLocationId) {
        super("Attendance for person " + personId + " at (" + latitude + ", " + longitude + ") is outside office geofence "
                + (officeLocationId != null ? officeLocationId : "<no matching office>"));
        this.personId = personId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.officeLocationId = officeLocationId;
    }
}
