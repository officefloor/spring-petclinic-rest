package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create pipeline when a new owner would be a second member of a household that
 * already exists — a household being keyed on (last name, postcode) via the deterministic
 * {@code householdId}. Setting {@code sharesHousehold} true bypasses this, creating the owner as
 * a declared household member instead. Handled globally by {@link DuplicateIdentityExceptionHandler},
 * which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String message) {
        super(message);
    }
}
