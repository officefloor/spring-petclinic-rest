package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared handling for the optional owner {@code email}. Email is optional, so a
 * blank or missing value is left untouched; when present it must be a syntactically
 * valid address and is stored lower-cased.
 */
public final class OwnerEmails {

    // Syntactic check: a non-empty local part, a single '@', and a dotted domain,
    // none of the parts containing whitespace or a stray '@'.
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    // Disposable/throwaway email providers are rejected: a real, durable address is required.
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    // Second-level labels of the rejected disposable providers. An address on the same provider
    // dressed up under a different TLD ('mailinator.net') or as a subdomain ('x.tempmail.com')
    // slips past the exact-match rejection above but is still "disposable-adjacent".
    private static final Set<String> DISPOSABLE_LABELS = Set.of(
            "mailinator", "tempmail", "guerrillamail");

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
        String normalized = trimmed.toLowerCase(java.util.Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("Email must not use a disposable-email domain");
        }
        return normalized;
    }

    /**
     * Whether a present email's domain is "disposable-adjacent": its second-level label matches a
     * known disposable provider, so an address on the same provider under a different TLD
     * ({@code mailinator.net}) or as a subdomain ({@code x.tempmail.com}) is flagged even though it
     * is not the exact rejected domain. A blank/absent email or one with no dotted domain is not
     * adjacent.
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        String normalized = email.trim().toLowerCase(java.util.Locale.ROOT);
        int at = normalized.indexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = normalized.substring(at + 1);
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false;
        }
        String secondLevel = labels[labels.length - 2];
        return DISPOSABLE_LABELS.contains(secondLevel);
    }
}
