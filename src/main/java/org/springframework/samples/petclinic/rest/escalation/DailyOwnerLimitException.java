package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after the maximum number of owners have
 * already been registered on the current day. Handled by
 * {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(int limit) {
        super("Daily limit of " + limit + " owner registrations has been reached");
    }
}
