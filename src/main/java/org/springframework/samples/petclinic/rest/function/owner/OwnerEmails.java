package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

/**
 * Shared email helpers for the owner pipeline: the single source of truth for the
 * disposable-domain blocklist and the derived "disposable-adjacent" classification.
 *
 * <p>An <em>exact</em> disposable domain is rejected outright (400) during
 * {@link NormalizeOwnerEmail}, so a created owner never carries one. A
 * <em>disposable-adjacent</em> domain is the near-miss that slips past that exact-match
 * blocklist — it shares a known throwaway provider's registrable base name (its
 * second-to-last label) while differing in TLD (e.g. {@code mailinator.net}) or carrying
 * a subdomain (e.g. {@code inbox.mailinator.com}). Those are admitted but flagged for
 * risk review.
 */
public final class OwnerEmails {

    private OwnerEmails() {
    }

    /** Disposable email domains that are rejected outright (400). */
    static final Set<String> DISPOSABLE_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The lower-cased domain part of an email address, or {@code null} when the address
     * is absent/blank or has no usable domain.
     */
    public static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim().toLowerCase();
        int at = trimmed.indexOf('@');
        if (at < 0 || at == trimmed.length() - 1) {
            return null;
        }
        return trimmed.substring(at + 1);
    }

    /** Whether the domain is an exact blocked disposable domain. */
    public static boolean isDisposable(String domain) {
        return domain != null && DISPOSABLE_DOMAINS.contains(domain);
    }

    /**
     * Whether the domain shares a known disposable provider's registrable base name (its
     * second-to-last label) — catching different-TLD and subdomain variants of a
     * throwaway provider. {@code false} for a null/blank or single-label domain.
     */
    public static boolean isDisposableAdjacent(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        String base = registrableBase(domain);
        if (base == null) {
            return false;
        }
        for (String disposable : DISPOSABLE_DOMAINS) {
            if (base.equals(registrableBase(disposable))) {
                return true;
            }
        }
        return false;
    }

    /** The second-to-last label of a dotted domain (its registrable base), or null. */
    private static String registrableBase(String domain) {
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return null;
        }
        return labels[labels.length - 2];
    }
}
