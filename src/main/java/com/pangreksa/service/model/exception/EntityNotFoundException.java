package com.pangreksa.service.model.exception;

import lombok.Getter;

/**
 * Thrown when a lookup by identifier (or natural key) finds no row. Adapters should map to HTTP 404.
 */
@Getter
public class EntityNotFoundException extends DomainException {

    private final String entityName;
    private final Object identifier;

    public EntityNotFoundException(String entityName, Object identifier) {
        super(entityName + " not found for identifier: " + identifier);
        this.entityName = entityName;
        this.identifier = identifier;
    }
}
