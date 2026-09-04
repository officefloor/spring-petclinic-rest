package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * The owner's <em>deterministic</em> household id: it is derived purely from the normalized last name
 * and the postcode (see {@link #derive(String, String)}), so every owner sharing a last name and
 * postcode computes the same value automatically — no explicit linking is required.
 * {@link AssignHousehold} stores it, {@link AssignHouseholdSize} counts the members sharing it and
 * {@link CapMembershipLevel} caps membership within it.
 */
final class HouseholdId {

    private HouseholdId() {
    }

    /** The deterministic household id of a stored/built owner, from its last name and postcode. */
    static String of(Owner owner) {
        return derive(normalizeName(owner.getLastName()), normalizePostcode(owner.getPostcode()));
    }

    /** The deterministic household id a create request would receive, from its last name and postcode. */
    static String of(OwnerFieldsDto request) {
        return derive(normalizeName(request.getLastName()), normalizePostcode(request.getPostcode()));
    }

    /** Last-name normalization used for household grouping: trim, collapse whitespace, lower-case. */
    static String normalizeName(String value) {
        return ComparisonText.of(value);
    }

    /** Postcode normalization used for household grouping: trim; {@code null} becomes an empty string. */
    static String normalizePostcode(String value) {
        return value == null ? "" : value.trim();
    }

    /**
     * Deterministic household id: the first 12 hex characters (upper-case) of SHA-256 over
     * {@code normalizedLastName + '|' + postcode}. Owners with the same last name and postcode share it.
     */
    static String derive(String normalizedLastName, String postcode) {
        return Sha256Hex.of(normalizedLastName + "|" + postcode).substring(0, 12).toUpperCase();
    }
}
