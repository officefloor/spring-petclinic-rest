package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code CheckUniqueIdentity} when a create-owner request would join a household that
 * already exists. The household is keyed on {@code (lastName, postcode)} through the deterministic
 * {@code householdId}, so a second owner sharing an existing owner's last name and postcode is a
 * household duplicate — unless the request declared {@code sharesHousehold=true}, which bypasses the
 * block. Handled by {@link DuplicateIdentityExceptionHandler}, which responds 409.
 */
public class DuplicateIdentityException extends Exception {

    public DuplicateIdentityException(String householdId) {
        super("An owner already exists in this household: " + householdId);
    }
}
