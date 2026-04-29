package com.pangreksa.service.model.exception;

import lombok.Getter;

/**
 * Thrown when a leave application references an unknown leave-absence type.
 */
@Getter
public class LeaveTypeNotFoundException extends DomainException {

    private final Object identifier;

    public LeaveTypeNotFoundException(Object identifier) {
        super("Leave absence type not found for identifier: " + identifier);
        this.identifier = identifier;
    }
}
