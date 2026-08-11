package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after 100 or more owners have already been registered
 * today (by registration date). Handled by {@link DailyLimitExceededExceptionHandler}, which
 * responds 429 Too Many Requests.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException() {
        super("The daily owner registration limit has been reached");
    }
}
