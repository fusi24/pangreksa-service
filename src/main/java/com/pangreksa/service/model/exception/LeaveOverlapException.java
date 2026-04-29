package com.pangreksa.service.model.exception;

import lombok.Getter;

import java.time.LocalDate;

/**
 * Thrown when an employee submits a leave request whose date range overlaps an existing application.
 */
@Getter
public class LeaveOverlapException extends DomainException {

    private final Long personId;
    private final LocalDate startDate;
    private final LocalDate endDate;

    public LeaveOverlapException(Long personId, LocalDate startDate, LocalDate endDate) {
        super("Leave request for person " + personId + " overlaps existing leave between " + startDate + " and " + endDate);
        this.personId = personId;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
