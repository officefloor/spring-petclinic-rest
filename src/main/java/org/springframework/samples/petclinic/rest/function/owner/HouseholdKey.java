package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The identity of a household: the same last name and the same address. The last name is
 * compared case-insensitively with whitespace trimmed and collapsed; the address is compared
 * in its normalized form (see {@link OwnerAddresses#normalize}), so e.g. "7  elm  ave" matches
 * "7 ELM AVENUE".
 *
 * <p>Single-sources the "what is a household" definition used by {@link AssignHousehold} to
 * decide who shares a {@code householdId}; that shared id then feeds the owner's
 * {@code identityKey} (see {@link OwnerIdentity}), which is what duplicate detection rejects on.
 */
final class HouseholdKey {

    private final String lastName;

    private final String address;

    private HouseholdKey(String lastName, String address) {
        this.lastName = lastName;
        this.address = address;
    }

    /** The household the given owner belongs to. */
    static HouseholdKey of(Owner owner) {
        return new HouseholdKey(normalize(owner.getLastName()), OwnerAddresses.normalize(owner.getAddress()));
    }

    /** Whether {@code owner} belongs to this household. */
    boolean matches(Owner owner) {
        return equals(of(owner));
    }

    /** Stable canonical form of the key, suitable for deriving a shared identifier. */
    String canonical() {
        return lastName + "|" + address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HouseholdKey other)) {
            return false;
        }
        return lastName.equals(other.lastName) && address.equals(other.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastName, address);
    }

    @Override
    public String toString() {
        return canonical();
    }

    /** Lower-case, trim, and collapse any run of whitespace to a single space. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
