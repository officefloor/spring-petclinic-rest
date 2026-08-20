package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Pipeline variable carrying the stable shared household identifier assigned by
 * {@link CheckOwnerHouseholdUnique} to {@link AssignOwnerHousehold}. A dedicated type
 * (rather than a bare {@code String}) keeps the variable unambiguous by type.
 */
public record HouseholdId(String value) {
}
