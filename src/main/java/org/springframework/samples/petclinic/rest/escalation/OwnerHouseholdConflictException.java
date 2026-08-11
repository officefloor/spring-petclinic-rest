package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when creating an owner whose computed {@code householdId} (derived from last name and
 * postcode) already belongs to another owner and the request did not declare
 * {@code sharesHousehold}. Because a household is keyed on (lastName, postcode), a second such owner
 * is a household duplicate; declaring {@code sharesHousehold} bypasses this block and creates the
 * owner as a declared household member instead. Handled globally by
 * {@link OwnerHouseholdConflictExceptionHandler}, which responds 409.
 */
public class OwnerHouseholdConflictException extends Exception {

    public OwnerHouseholdConflictException(String message) {
        super(message);
    }
}
