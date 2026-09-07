package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when 100 or more owners have already been registered today
 * (by {@code registrationDate}). Handled globally by {@link DailyRegistrationLimitExceptionHandler},
 * which responds 429 Too Many Requests.
 */
public class DailyRegistrationLimitException extends Exception {

    public DailyRegistrationLimitException(String message) {
        super(message);
    }
}
