package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when a new owner belongs to the household of an existing owner (same last name and postcode)
 * without declaring that it shares the household.
 */
public class DuplicateHouseholdException extends RuntimeException {

    private final String householdId;

    public DuplicateHouseholdException(String householdId) {
        super("An owner with the same last name and postcode already exists; set 'sharesHousehold' to join the household");
        this.householdId = householdId;
    }

    public String getHouseholdId() {
        return householdId;
    }
}
