package org.springframework.samples.petclinic.rest.escalation;

import java.time.LocalDate;

/**
 * Thrown when a create-owner request arrives on a day that has already reached the maximum number
 * of owners registered that date (100 or more), so no further owner may be created until the next
 * day. Handled by {@link DailyOwnerLimitExceptionHandler}, which responds 429 Too Many Requests.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(LocalDate date, long count) {
        super("Daily owner limit reached (" + count + " owners created on " + date + ")");
    }
}
