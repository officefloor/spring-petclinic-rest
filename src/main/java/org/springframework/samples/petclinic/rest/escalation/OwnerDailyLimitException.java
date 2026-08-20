package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckOwnerDailyLimit} when a create-owner request arrives after {@code 100} or
 * more owners have already been registered today (by {@code registrationDate}). Handled by
 * {@link OwnerDailyLimitExceptionHandler}, which responds 429.
 */
public class OwnerDailyLimitException extends Exception {

    public OwnerDailyLimitException(int count) {
        super("Daily owner registration limit reached (" + count + " owners created today)");
    }
}
