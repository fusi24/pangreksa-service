package com.pangreksa.service.model.exception;

import lombok.Getter;

import java.time.LocalDate;

/**
 * Thrown when an employee attempts to check out without an open check-in record for the day.
 */
@Getter
public class NotCheckedInException extends DomainException {

    private final Long personId;
    private final LocalDate date;

    public NotCheckedInException(Long personId, LocalDate date) {
        super("Person " + personId + " has no open check-in for " + date);
        this.personId = personId;
        this.date = date;
    }
}
