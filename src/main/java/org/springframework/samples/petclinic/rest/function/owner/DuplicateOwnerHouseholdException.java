package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueOwnerHousehold} when a create-owner request has the same
 * last name and address (compared case-insensitively with collapsed whitespace) as an
 * existing owner and the request did not set {@code sharesHousehold} true. Handled by
 * {@code DuplicateOwnerHouseholdExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerHouseholdException extends Exception {

    public DuplicateOwnerHouseholdException(String lastName, String address) {
        super("Another owner already shares this household (lastName '" + lastName
                + "', address '" + address + "'); set sharesHousehold true to allow it");
    }
}
