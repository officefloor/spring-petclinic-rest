package org.springframework.samples.petclinic.rest.function.common;

import java.util.Locale;
import java.util.Set;

/**
 * Single source of truth for the disposable email-domain rules.
 *
 * <p>A small blocklist of throwaway providers is hard-rejected at create time (a 400). A domain is
 * "disposable-adjacent" when it is <em>not</em> itself blocked but is closely related to one — a
 * subdomain of a blocked domain, or a domain sharing a blocked provider's second-level label under a
 * different top-level domain (e.g. {@code mailinator.net} next to the blocked {@code mailinator.com}).
 * Adjacency is a soft signal used to flag an owner as risky, not to reject the create.
 */
public final class DisposableDomains {

    /** Domains for throwaway email providers that are not accepted. */
    public static final Set<String> BLOCKED =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Second-level labels of the blocked domains, used to detect adjacent variants. */
    private static final Set<String> BLOCKED_LABELS =
            Set.of("mailinator", "tempmail", "guerrillamail");

    private DisposableDomains() {
    }

    /** The lower-cased domain part of {@code email}, or {@code null} when there is none. */
    public static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return domain.isEmpty() ? null : domain;
    }

    /** True when {@code domain} is on the hard blocklist. */
    public static boolean isBlocked(String domain) {
        return domain != null && BLOCKED.contains(domain);
    }

    /**
     * True when {@code domain} is disposable-adjacent: not itself blocked, but a subdomain of a
     * blocked domain, or sharing a blocked provider's second-level label under a different TLD.
     */
    public static boolean isAdjacent(String domain) {
        if (domain == null || BLOCKED.contains(domain)) {
            return false;
        }
        for (String blocked : BLOCKED) {
            if (domain.endsWith("." + blocked)) {
                return true;
            }
        }
        return BLOCKED_LABELS.contains(secondLevelLabel(domain));
    }

    /** The second-level label of {@code domain} (the label before the final TLD label). */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : domain;
    }
}
