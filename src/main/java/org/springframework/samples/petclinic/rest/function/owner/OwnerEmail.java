package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;

/**
 * Shared email handling for owner endpoints. An owner's email is optional; when supplied it must be
 * a syntactically valid address and is stored and returned lower-cased.
 */
final class OwnerEmail {

    /** Pragmatic single-address syntax check: a non-empty local part, an '@', and a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private OwnerEmail() {
    }

    /**
     * Returns the normalized (trimmed, lower-cased) email, or {@code null} when none was supplied.
     * A supplied-but-invalid address is rejected.
     *
     * @throws OwnerEmailInvalidException when a non-blank email is not a valid address.
     */
    static String normalize(String email) throws OwnerEmailInvalidException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new OwnerEmailInvalidException(email);
        }
        return trimmed.toLowerCase(java.util.Locale.ROOT);
    }
}
