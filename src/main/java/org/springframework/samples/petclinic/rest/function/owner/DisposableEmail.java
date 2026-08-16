package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

/**
 * Shared knowledge of disposable/throwaway email domains, used by the create-owner pipeline.
 *
 * <p>Two levels of match:
 * <ul>
 * <li>{@link #isBlocked(String)} — an EXACT domain on the blocklist. {@link RequireOwnerFields}
 *     rejects such a create with 400, so a blocked domain is never persisted.</li>
 * <li>{@link #isAdjacent(String)} — the email's domain is disposable-ADJACENT: its second-level
 *     label matches a known disposable domain's base label. This is broader than an exact match,
 *     so subdomains ({@code inbox.mailinator.com}) and sibling suffixes ({@code mailinator.net})
 *     count too. Adjacency does not reject; it feeds the owner's {@code riskFlag}.</li>
 * </ul>
 */
public final class DisposableEmail {

    /** Disposable/throwaway email domains a create-owner request may not use (hard reject). */
    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** The registrable base labels (the label before the public suffix) of the blocked domains.
     *  A domain is disposable-adjacent when its second-level label is one of these. */
    private static final Set<String> BASE_LABELS = Set.of("mailinator", "tempmail", "guerrillamail");

    private DisposableEmail() {
    }

    /** True when the exact domain is on the disposable-domain blocklist. */
    public static boolean isBlocked(String domain) {
        return domain != null && BLOCKED_DOMAINS.contains(domain.trim().toLowerCase(Locale.ROOT));
    }

    /** True when {@code email}'s domain is disposable-adjacent: present, with at least a
     *  second-level and top-level label, whose second-level label matches a known disposable
     *  domain's base label (so subdomains and sibling suffixes match too). */
    public static boolean isAdjacent(String email) {
        if (email == null) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return false;
        }
        return BASE_LABELS.contains(labels[labels.length - 2]);
    }
}
