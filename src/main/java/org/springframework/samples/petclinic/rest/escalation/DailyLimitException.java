package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after 100 or more owners have already
 * been registered today. Handled by {@link DailyLimitExceptionHandler}, which
 * responds 429.
 */
public class DailyLimitException extends Exception {

    public DailyLimitException() {
        super("Daily owner registration limit reached");
    }
}
