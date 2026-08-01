package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner would exceed the maximum number of owners that may be
 * registered on a single day (by registration date). Handled globally by
 * {@link DailyRegistrationLimitExceptionHandler}, which responds 400 Bad Request.
 */
public class DailyRegistrationLimitException extends Exception {

    public DailyRegistrationLimitException(String message) {
        super(message);
    }
}
