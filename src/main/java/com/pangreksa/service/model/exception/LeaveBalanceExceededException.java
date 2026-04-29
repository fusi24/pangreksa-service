package com.pangreksa.service.model.exception;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * Thrown when a leave application exceeds the employee's available balance for the given leave type/year.
 */
@Getter
public class LeaveBalanceExceededException extends DomainException {

    private final Long personId;
    private final BigDecimal requested;
    private final BigDecimal available;

    public LeaveBalanceExceededException(Long personId, BigDecimal requested, BigDecimal available) {
        super("Leave request for person " + personId + " requested " + requested + " day(s) but only " + available + " available");
        this.personId = personId;
        this.requested = requested;
        this.available = available;
    }
}
