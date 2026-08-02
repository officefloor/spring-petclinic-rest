package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner would exceed the maximum number of owners allowed to be
 * registered on a single day (by registration date). Handled globally by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 400 Bad Request.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(String message) {
        super(message);
    }
}
