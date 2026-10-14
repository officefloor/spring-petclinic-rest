package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when the maximum number of owners has already been created today.
 */
public class DailyOwnerLimitExceededException extends RuntimeException {

    private final int limit;

    public DailyOwnerLimitExceededException(int limit) {
        super("The maximum number of owners has already been created today");
        this.limit = limit;
    }

    public int getLimit() {
        return limit;
    }
}
