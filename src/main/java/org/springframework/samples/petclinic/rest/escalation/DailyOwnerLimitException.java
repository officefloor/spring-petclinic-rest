package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner once {@value #DAILY_OWNER_LIMIT} or more owners have already been
 * created today (by registration date). Handled globally by {@link DailyOwnerLimitExceptionHandler},
 * which responds 429 (Too Many Requests).
 */
public class DailyOwnerLimitException extends Exception {

    /** Maximum number of owners permitted to be created in a single day. */
    public static final int DAILY_OWNER_LIMIT = 100;

    public DailyOwnerLimitException(int limit) {
        super(limit + " or more owners have already been created today; no further owners can be "
                + "created until tomorrow");
    }
}
