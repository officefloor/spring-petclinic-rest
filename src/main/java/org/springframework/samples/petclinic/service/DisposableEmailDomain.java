package org.springframework.samples.petclinic.service;

import java.util.Locale;
import java.util.Set;

/**
 * Rejects an owner's email whose domain is a known disposable-mail provider. A
 * {@code null} or domain-less email is left untouched; a blocklisted domain raises
 * {@link IllegalArgumentException}, which the REST layer reports as 400 Bad Request.
 */
public final class DisposableEmailDomain {

    private static final Set<String> BLOCKED =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomain() {
    }

    public static String validate(String email) {
        if (email != null) {
            int at = email.lastIndexOf('@');
            if (at >= 0 && BLOCKED.contains(email.substring(at + 1).toLowerCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Email domain is not allowed: " + email);
            }
        }
        return email;
    }

    /**
     * True when the email's domain is disposable-adjacent: a subdomain of a known disposable
     * provider (e.g. {@code inbox.mailinator.com}). Exactly-blocked domains are rejected at
     * creation, so only these near-matches reach a saved owner. A {@code null} or domain-less
     * email is not adjacent.
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return BLOCKED.stream().anyMatch(b -> domain.endsWith("." + b));
    }
}
