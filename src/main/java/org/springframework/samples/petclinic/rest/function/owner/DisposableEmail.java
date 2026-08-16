package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

/**
 * Shared knowledge of disposable email domains, used both by the hard create-time check
 * ({@link ValidateOwner}, which rejects an exact blocklisted domain with 400) and by the softer
 * {@code riskFlag} response signal, so both read the same blocklist the same way.
 *
 * <p>Two notions:
 * <ul>
 * <li><b>blocked</b> — the domain exactly matches a known disposable provider; such an email is
 * never accepted at create time.</li>
 * <li><b>adjacent</b> — the domain is not itself blocked, yet its registrable label (the label
 * immediately left of the top-level domain) is that of a blocked provider: a subdomain of a
 * disposable domain (e.g. {@code inbox.mailinator.com}) or the same second-level name under a
 * different TLD (e.g. {@code mailinator.net}). Such an address slips past the exact-match block but
 * still smells disposable, so it raises {@code riskFlag}.</li>
 * </ul>
 */
public final class DisposableEmail {

    /** Disposable email domains that are never accepted, even when syntactically valid. */
    static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Second-level labels of the disposable domains (e.g. "mailinator"), for adjacency matching. */
    private static final Set<String> DISPOSABLE_LABELS = deriveLabels(DISPOSABLE_DOMAINS);

    private DisposableEmail() {
    }

    /** Whether {@code domain} exactly matches a known disposable provider. */
    public static boolean isBlocked(String domain) {
        return domain != null && DISPOSABLE_DOMAINS.contains(domain.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent: not itself blocked, but sharing the
     * registrable label of a blocked provider (a subdomain of one, or the same second-level name
     * under a different TLD). Returns false for a null, blank or address-less value.
     */
    public static boolean isAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.indexOf('@');
        if (at < 0) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        if (domain.isEmpty() || isBlocked(domain)) {
            return false;
        }
        String label = secondLevelLabel(domain);
        return label != null && DISPOSABLE_LABELS.contains(label);
    }

    /** The label immediately left of the top-level domain (e.g. "mailinator" for "a.mailinator.com"). */
    private static String secondLevelLabel(String domain) {
        String[] parts = domain.split("\\.");
        return parts.length >= 2 ? parts[parts.length - 2] : null;
    }

    private static Set<String> deriveLabels(Set<String> domains) {
        return domains.stream().map(DisposableEmail::secondLevelLabel).collect(java.util.stream.Collectors.toSet());
    }
}
