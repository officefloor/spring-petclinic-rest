package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner would exceed the maximum number of owners allowed to
 * be registered on a single day. Handled globally by
 * {@link OwnerDailyLimitExceptionHandler}, which responds 400.
 */
public class OwnerDailyLimitException extends Exception {

    public OwnerDailyLimitException(String message) {
        super(message);
    }
}
