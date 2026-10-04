package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckDailyOwnerLimit} when 100 or more owners have already been registered today
 * (by {@code registrationDate}). Checked so it appears in the {@code throws} clause and is routed to
 * its escalation handler, which responds 429.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(long count) {
        super("The daily owner registration limit has been reached: " + count + " owners already registered today");
    }
}
