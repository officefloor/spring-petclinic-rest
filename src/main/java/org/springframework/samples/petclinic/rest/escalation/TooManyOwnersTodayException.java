package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after the maximum number of owners (100) have already
 * been created today (by registration date). Handled by {@link TooManyOwnersTodayExceptionHandler},
 * which responds 429.
 */
public class TooManyOwnersTodayException extends Exception {

    public TooManyOwnersTodayException() {
        super("Daily owner creation limit reached; no more owners may be created today");
    }
}
