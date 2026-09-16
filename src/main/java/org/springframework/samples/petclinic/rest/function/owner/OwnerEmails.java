package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Shared email handling for the owner pipelines: normalizes an address to its canonical
 * lower-cased form so validation, storage and the response all use the same value.
 */
final class OwnerEmails {

    /** A single {@code @} separating a non-empty local part from a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private OwnerEmails() {
    }

    /** Trimmed, lower-cased form of {@code email}; {@code null} stays {@code null}. */
    static String normalize(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    /** Whether {@code email} is a syntactically valid address. */
    static boolean isValid(String email) {
        return email != null && EMAIL.matcher(email).matches();
    }
}
