package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Pipeline variable carrying the stable shared household identifier from
 * {@link EnsureUniqueHousehold} to {@link AssignHousehold}. A dedicated type (rather than a raw
 * {@code String}) keeps the OfficeFloor by-type variable matching unambiguous without a qualifier.
 */
public final class HouseholdId {

    private final String value;

    public HouseholdId(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }
}
