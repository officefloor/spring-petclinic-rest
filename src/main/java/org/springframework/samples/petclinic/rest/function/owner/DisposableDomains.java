package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * The disposable email domain policy, shared by the create-time hard block
 * ({@link RequireOwnerFields}) and the read-time risk signal ({@code riskFlag}).
 *
 * <p>{@link #BLOCKED} lists the domains that are rejected outright on create, so a
 * stored owner never carries one. {@link #isDisposableAdjacent(String)} recognises the
 * weaker signal: a domain that is not itself blocked but is clearly related to a blocked
 * one - it shares a base label (the second-level label immediately before the final TLD)
 * with a blocked domain. This catches other TLDs of the same provider (e.g.
 * {@code mailinator.net}) and subdomains of a blocked domain (e.g.
 * {@code mail.mailinator.com}).
 */
public final class DisposableDomains {

    private DisposableDomains() {
    }

    /** Disposable email domains that are never accepted, matched case-insensitively. */
    public static final Set<String> BLOCKED =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** The base labels (second-level label) of the blocked domains, e.g. "mailinator". */
    private static final Set<String> BLOCKED_BASE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    /** True when {@code domain} is exactly on the outright blocklist. */
    public static boolean isBlocked(String domain) {
        return domain != null && BLOCKED.contains(domain.toLowerCase());
    }

    /**
     * True when {@code domain} is disposable-adjacent: not itself blocked, but sharing a
     * base label with a blocked domain (a different TLD of the same provider, or a
     * subdomain of a blocked domain).
     */
    public static boolean isDisposableAdjacent(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        String lower = domain.toLowerCase();
        if (BLOCKED.contains(lower)) {
            return false; // an outright-blocked domain is not merely "adjacent"
        }
        String[] labels = lower.split("\\.");
        for (String label : labels) {
            if (BLOCKED_BASE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }

    /** True when {@code email}'s domain (text after the last '@') is disposable-adjacent. */
    public static boolean emailIsDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        return isDisposableAdjacent(email.substring(at + 1));
    }
}
