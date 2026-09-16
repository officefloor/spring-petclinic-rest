package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * The single rule set for owner email addresses: what counts as "present", what counts as
 * a syntactically valid address, and how one is normalized for storage. Shared so every step
 * that touches an owner email treats it the same way.
 */
final class OwnerEmails {

    /** local-part {@code @} domain, no whitespace, and a dotted domain. */
    private static final Pattern SYNTAX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private OwnerEmails() {
    }

    /** An email is present when it is non-null and not blank; blank is treated as omitted. */
    static boolean isPresent(String email) {
        return email != null && !email.isBlank();
    }

    /** Whether {@code email} is a syntactically valid address. */
    static boolean isValid(String email) {
        return email != null && SYNTAX.matcher(email).matches();
    }

    /** The stored form of an email: trimmed and lower-cased. */
    static String normalize(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
