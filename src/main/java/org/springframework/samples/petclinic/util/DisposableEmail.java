package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;

/**
 * Rejects owner emails whose domain belongs to a known disposable-email provider.
 * The blocklist is fixed: {@code mailinator.com}, {@code tempmail.com} and
 * {@code guerrillamail.com}. An absent or domain-less email is never blocked.
 */
public final class DisposableEmail {

    /** Lower-case domains that are not accepted for an owner's email. */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmail() {
    }

    /**
     * True when {@code email} has a domain (the part after the last {@code @}) that is on
     * the disposable-domain blocklist. Comparison ignores case; a null or {@code @}-less
     * value returns {@code false}.
     */
    public static boolean isBlocked(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return BLOCKED_DOMAINS.contains(domain);
    }
}
