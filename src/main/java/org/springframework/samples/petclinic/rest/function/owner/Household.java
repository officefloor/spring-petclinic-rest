package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Sha256;
import org.springframework.util.StringUtils;

/**
 * Household identity shared by the create-owner steps. A household is the set of owners with the same
 * last name (normalized case-insensitively with collapsed whitespace) and the same postcode. Its
 * {@code householdId} is derived deterministically from those two values, so any two owners with the
 * same last name and postcode share it automatically without an explicit link. Used by
 * {@link AssignHouseholdId} to stamp each owner, by {@link EnsureUniqueHousehold} to reject a second
 * owner in an occupied household, and by {@link CountHouseholdMembers} to size the household.
 */
final class Household {

    /** The number of leading hex characters of the SHA-256 digest that form a household id. */
    private static final int ID_LENGTH = 12;

    private Household() {
    }

    /** Case-insensitive form with leading/trailing and repeated inner whitespace collapsed to one space. */
    static String normalizeLastName(String lastName) {
        return lastName == null ? "" : lastName.strip().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    /**
     * The deterministic household id for the given last name and postcode: the first
     * {@value #ID_LENGTH} hex characters of {@code SHA-256(normalizedLastName + '|' + postcode)}.
     * Returns {@code null} when no postcode is supplied, since a household is keyed on postcode and an
     * owner without one belongs to no household.
     */
    static String idFor(String lastName, String postcode) {
        if (!StringUtils.hasText(postcode)) {
            return null;
        }
        return Sha256.hex(normalizeLastName(lastName) + '|' + postcode).substring(0, ID_LENGTH);
    }
}
