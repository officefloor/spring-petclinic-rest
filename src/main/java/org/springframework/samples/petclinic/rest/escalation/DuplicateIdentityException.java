package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's household — its last name and postcode, captured as a
 * computed {@code householdId} — already belongs to another owner and the request did not opt
 * in with {@code sharesHousehold}. Handled by {@link DuplicateIdentityExceptionHandler} as a
 * 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String householdId) {
        super("Owner household already in use: " + householdId);
    }
}
