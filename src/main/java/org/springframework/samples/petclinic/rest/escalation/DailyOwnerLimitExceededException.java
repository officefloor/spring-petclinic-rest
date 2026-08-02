package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when attempting to create an owner on a day that has already reached its
 * cap of registrations (by registration date).
 * Handled globally by {@link DailyOwnerLimitExceededExceptionHandler}, which responds 400.
 */
public class DailyOwnerLimitExceededException extends Exception {

    public DailyOwnerLimitExceededException(String message) {
        super(message);
    }
}
