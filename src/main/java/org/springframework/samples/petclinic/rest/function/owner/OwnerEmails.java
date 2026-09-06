package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared handling for the optional owner {@code email}. Email is optional, so a
 * blank or missing value is left untouched; when present it must be a syntactically
 * valid address and is stored lower-cased.
 */
final class OwnerEmails {

    // Syntactic check: a non-empty local part, a single '@', and a dotted domain,
    // none of the parts containing whitespace or a stray '@'.
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private OwnerEmails() {
    }

    /**
     * Normalize an optional email. Returns {@code null}/blank unchanged; otherwise
     * validates syntax and returns the address lower-cased.
     *
     * @throws InvalidEmailException when a value is present but not syntactically valid.
     */
    static String normalize(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            return email;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        return trimmed.toLowerCase(java.util.Locale.ROOT);
    }
}
