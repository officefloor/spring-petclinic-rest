package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the submitted owner falls into an existing owner's household.
 * A household is keyed on {@code (lastName, postcode)}, so a new owner that shares a last
 * name and postcode with an existing owner is a household duplicate — unless the request
 * sets {@code sharesHousehold}, which bypasses this check and declares the owner a
 * household member. Handled globally by {@link DuplicateHouseholdExceptionHandler}, which
 * responds 409 Conflict.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String householdId) {
        super("An owner in the same household already exists (rejected household: " + householdId + ")");
    }
}
