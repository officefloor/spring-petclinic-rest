package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckHouseholdUnique}
 * when a create-owner request would join an existing household (another owner already shares the
 * computed {@code householdId}, i.e. the same normalized last name and postcode) without declaring
 * {@code sharesHousehold}. Setting {@code sharesHousehold} bypasses this block and creates the owner
 * as a declared household member. Handled globally by {@link DuplicateHouseholdExceptionHandler},
 * which responds 409.
 */
public class DuplicateHouseholdException extends Exception {

    public DuplicateHouseholdException(String householdId) {
        super("Household already exists: " + householdId
                + " (set sharesHousehold to join it)");
    }
}
