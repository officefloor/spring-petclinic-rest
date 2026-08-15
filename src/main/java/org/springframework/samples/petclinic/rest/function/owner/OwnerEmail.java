package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.DisposableEmailDomainException;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared normalization for an owner's optional {@code email}. When absent (null or blank) the
 * email is left unset; when present it must be a syntactically valid address and is stored and
 * returned lower-cased. An invalid address is rejected with 400 via {@link InvalidEmailException}.
 * An address whose domain is on the disposable-domain blocklist is rejected with 400 via
 * {@link DisposableEmailDomainException}.
 */
final class OwnerEmail {

    /** Syntactic address check: non-empty local part, one '@', and a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private OwnerEmail() {
    }

    /**
     * Returns the normalized (lower-cased) email, or {@code null} when none was supplied.
     *
     * @throws InvalidEmailException when an email is present but not syntactically valid.
     * @throws DisposableEmailDomainException when the email's domain is on the disposable blocklist.
     */
    static String normalize(String email) throws InvalidEmailException, DisposableEmailDomainException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        String normalized = trimmed.toLowerCase();
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DisposableDomains.BLOCKED.contains(domain)) {
            throw new DisposableEmailDomainException(email);
        }
        return normalized;
    }
}
