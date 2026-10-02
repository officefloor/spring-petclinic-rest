package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

/**
 * Single source of truth for the owner email rule, shared by the owner pipelines. An
 * owner may omit the email, but when supplied it must be a syntactically valid address
 * and is stored and returned lower-cased. Keeping the validity check and the
 * lower-casing here keeps the create and update paths consistent.
 */
final class OwnerEmail {

    /**
     * Pragmatic syntactic check for a single address: a non-empty local part, an
     * {@code @}, then a domain with at least one dot and no whitespace. Rejects values
     * like {@code not-an-email} that lack an {@code @} and a dotted domain.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    private OwnerEmail() {
    }

    /** Whether {@code email} is a syntactically valid address. */
    static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }

    /** Lower-case and trim the email, or {@code null} when absent/blank. */
    static String normalize(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        return trimmed.isEmpty() ? null : trimmed.toLowerCase();
    }
}
