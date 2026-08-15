package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * Shared knowledge of disposable email domains for the owner pipeline.
 *
 * <p>{@link #BLOCKED} is the exact blocklist rejected outright with 400 by {@link OwnerEmail}, so a
 * persisted owner never carries one of these domains. {@link #isAdjacent(String)} recognises the
 * weaker, softer signal used by {@link RiskFlag}: an address whose domain is <em>disposable-adjacent</em>
 * &mdash; a subdomain of a blocked domain (e.g. {@code inbox.mailinator.com}) or one that shares a
 * blocked domain's second-level label under a different TLD (e.g. {@code tempmail.io}). Such an
 * address passes validation (it is not an exact block) yet still resembles a throwaway provider.
 */
final class DisposableDomains {

    /** Disposable email domains that are never accepted (compared case-insensitively). */
    static final Set<String> BLOCKED = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableDomains() {
    }

    /**
     * True when {@code email}'s domain is disposable-adjacent: it is, or is a subdomain of, a
     * blocked domain, or it shares a blocked domain's second-level label under any TLD. Absent or
     * blank emails are never adjacent.
     */
    static boolean isAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.indexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase();
        for (String blocked : BLOCKED) {
            if (domain.equals(blocked) || domain.endsWith("." + blocked)) {
                return true; // exact block or subdomain of a blocked domain
            }
            if (secondLevelLabel(domain).equals(secondLevelLabel(blocked))) {
                return true; // same registrable name under a different TLD
            }
        }
        return false;
    }

    /** The label immediately before the top-level domain, e.g. {@code a.mailinator.com -> mailinator}. */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : domain;
    }
}
