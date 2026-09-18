package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

/**
 * The single definition of the disposable email-domain blocklist: addresses whose domain is a
 * known throwaway provider are not accepted for an owner. Lookup only; a pure function of the
 * email address.
 */
public final class DisposableEmailDomains {

    private static final Set<String> BLOCKED =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /** Whether {@code email}'s domain is on the disposable-domain blocklist. */
    public static boolean isBlocked(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED.contains(domain);
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent: not itself blocked, but a subdomain
     * of a blocked domain (e.g. {@code inbox.mailinator.com}). Such a near-miss slips past
     * {@link #isBlocked} yet still looks like a throwaway provider.
     */
    public static boolean isAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || BLOCKED.contains(domain)) {
            return false;
        }
        for (String blocked : BLOCKED) {
            if (domain.endsWith("." + blocked)) {
                return true;
            }
        }
        return false;
    }

    /** The lower-cased domain of {@code email}, or {@code null} when it has none. */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        return email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
    }
}
