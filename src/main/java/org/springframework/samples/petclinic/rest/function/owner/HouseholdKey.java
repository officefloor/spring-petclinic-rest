package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Objects;

import org.springframework.samples.petclinic.model.Owner;

/**
 * The identity of a household: the same last name and the same postcode. The last name is
 * compared case-insensitively with whitespace trimmed and collapsed; the postcode is compared
 * as stored. So e.g. "  Smith " at postcode "2000" is the same household as "smith" at "2000".
 *
 * <p>Single-sources the "what is a household" definition. The {@code householdId} is derived
 * <em>deterministically</em> from this key (see {@link #householdId()}): the first 12 hex
 * characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. Every owner computes
 * the same id for the same household automatically, so two owners share a {@code householdId}
 * exactly when they share a last name and postcode — no opt-in and no back-filling required.
 */
final class HouseholdKey {

    private final String lastName;

    private final String postcode;

    private HouseholdKey(String lastName, String postcode) {
        this.lastName = lastName;
        this.postcode = postcode;
    }

    /** The household the given owner belongs to. */
    static HouseholdKey of(Owner owner) {
        return new HouseholdKey(normalize(owner.getLastName()), owner.getPostcode() == null ? "" : owner.getPostcode());
    }

    /** Whether {@code owner} belongs to this household. */
    boolean matches(Owner owner) {
        return equals(of(owner));
    }

    /** Stable canonical form of the key, suitable for deriving a shared identifier. */
    private String canonical() {
        return lastName + "|" + postcode;
    }

    /**
     * The deterministic {@code householdId} for this household: the first 12 hex characters of
     * SHA-256 over {@link #canonical()}. Owners with the same last name and postcode compute the
     * same value, so they share a household automatically.
     */
    String householdId() {
        return Hashing.sha256HexPrefix(canonical(), 6);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HouseholdKey other)) {
            return false;
        }
        return lastName.equals(other.lastName) && postcode.equals(other.postcode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lastName, postcode);
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
