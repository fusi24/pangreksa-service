package com.pangreksa.service.model.exception;

import lombok.Getter;

import java.time.LocalDate;

/**
 * Thrown when an employee attempts to check in again on a day they have already checked in.
 */
@Getter
public class AlreadyCheckedInException extends DomainException {

    private final Long personId;
    private final LocalDate date;

    public AlreadyCheckedInException(Long personId, LocalDate date) {
        super("Person " + personId + " has already checked in on " + date);
        this.personId = personId;
        this.date = date;
    }
}
