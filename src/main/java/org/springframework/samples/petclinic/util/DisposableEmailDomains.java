package org.springframework.samples.petclinic.util;

import java.util.Set;

/**
 * Pinned blocklist of disposable email domains. An owner whose email is registered under one of these
 * throwaway providers is rejected, since such an address cannot be relied on for contact. Pure lookup
 * with no dependency on other owners, so it is applied at request time.
 */
public final class DisposableEmailDomains {

    /** Known disposable-mail providers, held lower-cased for case-insensitive matching. */
    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com",
            "tempmail.com",
            "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /** Whether {@code domain} is a blocked disposable-mail provider (case-insensitive). */
    public static boolean isBlocked(String domain) {
        return domain != null && BLOCKED.contains(domain.toLowerCase());
    }
}
