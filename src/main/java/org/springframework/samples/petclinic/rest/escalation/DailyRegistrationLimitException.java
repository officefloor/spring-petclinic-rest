package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code EnsureDailyRegistrationCapacity} when 100 or more owners have already been
 * registered today. Handled globally by {@link DailyRegistrationLimitExceptionHandler}, which
 * responds 429.
 */
public class DailyRegistrationLimitException extends Exception {

    public DailyRegistrationLimitException(String message) {
        super(message);
    }
}
