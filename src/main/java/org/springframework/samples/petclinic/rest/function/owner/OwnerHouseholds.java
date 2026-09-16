package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Shared household identity for the create-owner pipeline: derives a canonical key from an
 * owner's last name and address so the duplicate-household check compares the same value.
 *
 * <p>Two owners share a household when their last name and address match once
 * case-insensitively normalized with collapsed whitespace (surrounding whitespace trimmed
 * and any internal run of whitespace reduced to a single space).
 */
final class OwnerHouseholds {

    private OwnerHouseholds() {
    }

    /**
     * Canonical household key for {@code lastName} + {@code address}. Two requests/owners
     * belong to the same household exactly when their keys are equal.
     */
    static String key(String lastName, String address) {
        // '\n' separates the fields so "a b" + "c" cannot collide with "a" + "b c"
        // (a newline never survives whitespace collapsing).
        return normalize(lastName) + "\n" + normalize(address);
    }

    /** Trim, collapse internal whitespace runs to a single space, and lower-case. */
    private static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
