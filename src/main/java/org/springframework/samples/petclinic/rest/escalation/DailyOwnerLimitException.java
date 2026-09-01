package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when 100 or more owners have already been registered today, so no further owner may be
 * created until tomorrow. Handled by {@link DailyOwnerLimitExceptionHandler}, which responds
 * 429 Too Many Requests.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(int limit) {
        super("Daily owner registration limit reached: " + limit);
    }
}
