package com.pangreksa.service.model.exception;

import lombok.Getter;

import java.time.LocalDate;

/**
 * Thrown when no active work schedule can be resolved for a user on a given date — typically blocks attendance check-in.
 */
@Getter
public class WorkScheduleNotFoundException extends DomainException {

    private final Long appUserId;
    private final LocalDate date;

    public WorkScheduleNotFoundException(Long appUserId, LocalDate date) {
        super("No active work schedule for app user " + appUserId + " on " + date);
        this.appUserId = appUserId;
        this.date = date;
    }
}
