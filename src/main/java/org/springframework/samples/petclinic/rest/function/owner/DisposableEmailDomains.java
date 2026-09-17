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
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return BLOCKED.contains(domain);
    }
}
