package org.springframework.samples.petclinic.model;

import java.util.Locale;
import java.util.Set;

/**
 * The single definition of the disposable e-mail domains and of what counts as
 * <em>disposable-adjacent</em>.
 *
 * <p>{@link #DISPOSABLE} is the exact blocklist a create is rejected on
 * ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}); those addresses
 * never reach a persisted owner. A domain is <em>disposable-adjacent</em> when it is
 * <em>not</em> itself on that blocklist but sits right next to one of its entries — either a
 * subdomain of a blocked domain (e.g. {@code inbox.mailinator.com}) or a domain that shares a
 * blocked domain's second-level label under a different suffix (e.g. {@code mailinator.net},
 * {@code tempmail.io}). Such near-variants slip past the exact blocklist yet are still
 * throwaway-looking, so they are surfaced as a soft signal rather than rejected.
 *
 * <p>Lives beside {@link Owner} so the mapping layer can derive the risk signal without a
 * package cycle.
 */
public final class EmailDomains {

    /** Domains rejected outright at create time. */
    public static final Set<String> DISPOSABLE =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Second-level labels of the disposable domains (e.g. 'mailinator'). */
    private static final Set<String> DISPOSABLE_LABELS =
            Set.of("mailinator", "tempmail", "guerrillamail");

    private EmailDomains() {
    }

    /** The lower-cased domain part of {@code email}, or {@code null} when there is none. */
    public static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent — not itself blocklisted, but a
     * subdomain of a blocked domain or a sharer of a blocked domain's second-level label under
     * a different suffix. False when the email is absent, malformed or exactly blocklisted.
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null || DISPOSABLE.contains(domain)) {
            return false;
        }
        for (String blocked : DISPOSABLE) {
            if (domain.endsWith("." + blocked)) {
                return true; // a subdomain of a disposable domain
            }
        }
        String[] parts = domain.split("\\.");
        if (parts.length >= 2 && DISPOSABLE_LABELS.contains(parts[parts.length - 2])) {
            return true; // same second-level label under a different suffix
        }
        return false;
    }
}
