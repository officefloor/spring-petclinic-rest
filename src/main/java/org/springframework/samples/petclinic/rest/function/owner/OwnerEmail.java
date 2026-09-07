package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared normalization for the optional owner email. An email is optional: when absent (null or
 * blank) it is left as {@code null}. When present it must be a syntactically valid address; the
 * canonical stored/returned form is trimmed and lower-cased.
 */
final class OwnerEmail {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Disposable email providers rejected up front: their addresses are throwaway. */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmail() {
    }

    /**
     * @return the normalized (trimmed, lower-cased) email, or {@code null} when none was supplied.
     * @throws InvalidEmailException when a non-blank value is not a valid address.
     */
    static String normalize(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String normalized = email.trim().toLowerCase(Locale.ROOT);
        if (!EMAIL.matcher(normalized).matches()) {
            throw new InvalidEmailException("Email must be a syntactically valid address");
        }
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("Email domain is not accepted");
        }
        return normalized;
    }
}
