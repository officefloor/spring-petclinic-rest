package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

import org.springframework.samples.petclinic.model.Owner;
import org.springframework.samples.petclinic.util.Sha256;

/**
 * Shared household identity for the create-owner pipeline: derives the deterministic
 * {@code householdId} from an owner's last name and postcode, so every owner with the same
 * last name and postcode resolves to the same value automatically — without any owner having
 * to opt in.
 *
 * <p>Two owners belong to the same household exactly when their last names match
 * case-insensitively (with collapsed whitespace) and their postcodes are equal.
 */
final class OwnerHouseholds {

    private OwnerHouseholds() {
    }

    /**
     * Deterministic household identifier for {@code lastName} + {@code postcode}: the first 12
     * hex characters of SHA-256 over {@code normalizedLastName + '|' + postcode}. Every owner in
     * the same household resolves to the same value regardless of when they are created.
     */
    static String id(String lastName, String postcode) {
        String key = normalizeLastName(lastName) + "|" + (postcode == null ? "" : postcode);
        return Sha256.hex(key).substring(0, 12);
    }

    /** Deterministic household id for an existing owner's stored last name and postcode. */
    static String of(Owner owner) {
        return id(owner.getLastName(), owner.getPostcode());
    }

    /** Trim, collapse internal whitespace runs to a single space, and lower-case. */
    private static String normalizeLastName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
