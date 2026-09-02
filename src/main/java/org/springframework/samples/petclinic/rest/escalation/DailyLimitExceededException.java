package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RejectDailyLimit} when a create request arrives after the maximum number of
 * owners have already been registered today. Handled by {@link DailyLimitExceededExceptionHandler},
 * which responds 429.
 */
public class DailyLimitExceededException extends Exception {

    public DailyLimitExceededException() {
        super("Daily owner registration limit reached");
    }
}
