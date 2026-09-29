package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RequireDailyOwnerLimit} when a create-owner request would exceed the maximum number
 * of owners allowed to be created in a single day (100), counted by {@code registrationDate}.
 * {@link DailyOwnerLimitExceededExceptionHandler} reports a 429 (Too Many Requests).
 */
public class DailyOwnerLimitExceededException extends Exception {

    private final int limit;

    public DailyOwnerLimitExceededException(int limit) {
        super("The daily owner creation limit has been reached: " + limit);
        this.limit = limit;
    }

    public int getLimit() {
        return this.limit;
    }
}
