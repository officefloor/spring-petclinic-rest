package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureDailyOwnerLimitNotExceeded} when 100 or more owners have already
 * been registered today. Handled by {@link DailyOwnerLimitExceptionHandler}, which
 * responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(String message) {
        super(message);
    }
}
