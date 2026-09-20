package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Shared household matching: canonicalizes the free-text fields that identify a household
 * (last name and address) so values that differ only in letter case or in the amount of
 * surrounding/internal whitespace compare equal. Used to detect when a new owner shares a
 * household with an existing one.
 */
final class Households {

    private Households() {
    }

    /**
     * Canonicalizes {@code value} for case-insensitive, whitespace-insensitive comparison:
     * trims the ends, collapses every run of whitespace to a single space and lower-cases
     * the result. Returns an empty string when {@code value} is {@code null}.
     */
    static String normalize(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
