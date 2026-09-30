package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerDailyLimit} when a new owner would be created on a day that has
 * already seen {@code 100} or more owners registered (counted by {@code registrationDate}).
 * Handled globally by {@link DailyOwnerLimitExceededExceptionHandler}, which responds
 * 429 Too Many Requests.
 */
public class DailyOwnerLimitExceededException extends Exception {

    public DailyOwnerLimitExceededException(String message) {
        super(message);
    }
}
