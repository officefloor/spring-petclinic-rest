package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;

/**
 * Validates an owner's email address against a fixed blocklist of disposable-email domains.
 *
 * <p>An email is validated ONLY when present; a {@code null} or empty email is always accepted,
 * keeping the request contract backward-compatible. When present, its domain (the part after the
 * last {@code '@'}) is compared case-insensitively against the blocklist and the email is rejected
 * when it matches. The syntactic validity of any value present is already guaranteed by the
 * schema-level {@code format: email} on the request field.
 */
public final class DisposableEmailValidator {

    /** Disposable-email domains that are not accepted (compared in lower case). */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailValidator() {
    }

    /**
     * Validate an email address, throwing when its domain is on the disposable-domain blocklist.
     *
     * @param email the owner's email, or {@code null} when none was given
     * @throws DisposableEmailException if a non-blank email's domain is on the blocklist.
     */
    public static void validate(String email) {
        if (email == null || email.isEmpty()) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailException(email);
        }
    }
}
