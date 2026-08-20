package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner once 100 or more owners have already been created today
 * (by registrationDate). Handled by {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException() {
        super("The daily limit for creating owners has been reached; please try again tomorrow");
    }
}
