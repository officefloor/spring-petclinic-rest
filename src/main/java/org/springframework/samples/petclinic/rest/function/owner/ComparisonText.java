package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Case-insensitive text normalization used for grouping and comparison: trim, collapse internal
 * whitespace to single spaces and lower-case; {@code null} becomes an empty string. Keeping the
 * primitive in one place lets {@link HouseholdId} (last-name grouping) and {@link EnsureCityCapacity}
 * (case-insensitive city comparison) share exactly one implementation.
 *
 * <p>Distinct from {@link AddressNormalizer}, which upper-cases and expands abbreviations.
 */
final class ComparisonText {

    private ComparisonText() {
    }

    /** Trim, collapse internal whitespace and lower-case; {@code null} becomes an empty string. */
    static String of(String value) {
        if (value == null) {
            return "";
        }
        return value.trim().replaceAll("\\s+", " ").toLowerCase();
    }
}
