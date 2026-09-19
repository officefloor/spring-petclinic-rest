package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;

/**
 * The fixed blocklist of disposable email domains that owner registrations are not
 * allowed to use. Kept separate from the syntactic email check so the two concerns
 * stay independent and reusable.
 */
public final class DisposableEmailDomains {

    /** Domains known to hand out throwaway inboxes. */
    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /**
     * Whether an email address's domain is on the disposable-domain blocklist. Matching
     * is case-insensitive; an address with no '@' or no domain is not blocked (its
     * syntax is the concern of the email format check).
     *
     * @param email the candidate email address
     * @return {@code true} when the domain after the last '@' is blocked
     */
    public static boolean isBlocked(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        return BLOCKED.contains(domain);
    }
}
