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
 * characters of SHA-256 over {@code identityRegion + '|' + normalizedLastName + '|' + postcode},
 * where {@code identityRegion} is the version-2 identity region code (e.g. {@code V2NSW} — see
 * {@link OwnerRegion#identityRegionOf}). Mixing the {@code V2} tag in means no householdId
 * matches a value the same household produced under version 1. Two owners of the same last name
 * and postcode derive the same region and so still compute the same id automatically — no opt-in
 * and no back-filling required. Household membership itself (see {@link #matches}) is keyed on
 * the last name and postcode only; the region does not widen or narrow a household.
 */
final class HouseholdKey {

    private final String lastName;

    private final String postcode;

    private final String identityRegion;

    private HouseholdKey(String lastName, String postcode, String identityRegion) {
        this.lastName = lastName;
        this.postcode = postcode;
        this.identityRegion = identityRegion;
    }

    /** The household the given owner belongs to. */
    static HouseholdKey of(Owner owner) {
        return new HouseholdKey(normalize(owner.getLastName()),
                owner.getPostcode() == null ? "" : owner.getPostcode(),
                OwnerRegion.identityRegionOf(owner));
    }

    /** Whether {@code owner} belongs to this household. */
    boolean matches(Owner owner) {
        return equals(of(owner));
    }

    /** Stable canonical form of the key, suitable for deriving a shared identifier. Carries the
     *  version-2 identity region tag so the derived id is distinct from any version-1 value. */
    private String canonical() {
        return identityRegion + "|" + lastName + "|" + postcode;
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
