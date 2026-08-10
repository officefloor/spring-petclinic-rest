package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.OwnerEmailDisposableException;
import org.springframework.samples.petclinic.rest.escalation.OwnerEmailInvalidException;

/**
 * Shared email handling for owner endpoints. An owner's email is optional; when supplied it must be
 * a syntactically valid address whose domain is not on the disposable-domain blocklist, and is
 * stored and returned lower-cased.
 */
final class OwnerEmail {

    /** Pragmatic single-address syntax check: a non-empty local part, an '@', and a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    /** Disposable email domains that are never accepted (compared lower-cased). */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmail() {
    }

    /**
     * Returns the normalized (trimmed, lower-cased) email, or {@code null} when none was supplied.
     * A supplied-but-invalid address, or one whose domain is on the disposable-domain blocklist, is
     * rejected.
     *
     * @throws OwnerEmailInvalidException when a non-blank email is not a valid address.
     * @throws OwnerEmailDisposableException when the email's domain is on the disposable blocklist.
     */
    static String normalize(String email) throws OwnerEmailInvalidException, OwnerEmailDisposableException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new OwnerEmailInvalidException(email);
        }
        String normalized = trimmed.toLowerCase(java.util.Locale.ROOT);
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new OwnerEmailDisposableException(email);
        }
        return normalized;
    }
}
