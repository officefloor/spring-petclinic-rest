package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request arrives after the maximum number of owners (100) have
 * already been registered today (by registrationDate). Handled as a 429 Too Many Requests.
 */
public class DailyOwnerLimitException extends Exception {

    public DailyOwnerLimitException(int count) {
        super(count + " owners have already been created today; no further owners can be created "
                + "until tomorrow");
    }
}
