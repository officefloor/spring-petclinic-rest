package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when 100 or more owners have already been created today
 * (counted by {@code registrationDate}), i.e. the day's create quota is exhausted. Handled
 * globally by {@link DailyOwnerLimitExceptionHandler}, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(long created) {
        super("Daily owner creation limit reached: " + created + " owners already created today");
    }
}
