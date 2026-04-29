package com.pangreksa.service.model.exception;

/**
 * Root of the domain exception hierarchy. All business-rule violations thrown by use cases extend this type so that
 * adapters (REST {@code @ControllerAdvice}, Vaadin error handlers) can map them uniformly to user-facing responses.
 *
 * <p>Subclasses should carry structured fields (exposed via Lombok {@code @Getter}) and supply a default
 * English-language message via {@link #DomainException(String)}.</p>
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }

    protected DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
