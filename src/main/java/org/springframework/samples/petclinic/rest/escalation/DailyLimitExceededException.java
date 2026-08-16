package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckDailyLimit}
 * when {@value org.springframework.samples.petclinic.rest.function.owner.CheckDailyLimit#MAX_PER_DAY}
 * or more owners have already been created today (by registrationDate), so no further owner may be
 * created until tomorrow. Handled globally by {@link DailyLimitExceededExceptionHandler}, which
 * responds 429.
 */
public class DailyLimitExceededException extends Exception {

    private final int count;

    public DailyLimitExceededException(int count) {
        super("Daily owner creation limit reached (" + count + " owners created today)");
        this.count = count;
    }

    public int getCount() {
        return this.count;
    }
}
