package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Single source of truth for the owner-household identity rule. Two owners count as the same
 * household when their last names match and their addresses match, each compared case-insensitively
 * with surrounding and repeated whitespace collapsed.
 *
 * <p>Used by {@link CheckOwnerHouseholdUnique} to normalize both the submitted values and every
 * stored owner's values before comparison, so differently cased or spaced values that mean the same
 * thing still collide.
 */
final class OwnerHousehold {

    private OwnerHousehold() {
    }

    /**
     * Reduces a value to its comparison form: trimmed, inner runs of whitespace collapsed to a
     * single space, and lower-cased. A {@code null} value yields an empty string.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
