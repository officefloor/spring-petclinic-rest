package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Known disposable email domains and the "disposable-adjacent" test derived from them.
 *
 * <p>An exact match on {@link #DISPOSABLE_DOMAINS} is rejected outright at registration, so a stored
 * owner never carries one. A domain is <em>disposable-adjacent</em> when it is not itself on the
 * blocklist but shares its second-level label with one of the blocked domains - a different TLD
 * ({@code mailinator.net}) or a subdomain ({@code smtp.mailinator.com}). Such an address passes
 * registration but is still treated as a risk signal.
 */
public final class DisposableDomains {

    /** Disposable email domains that are not accepted for owner registration. */
    public static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** The second-level label of each blocked domain (e.g. "mailinator" from "mailinator.com"). */
    private static final Set<String> DISPOSABLE_LABELS = DISPOSABLE_DOMAINS.stream()
            .map(DisposableDomains::secondLevelLabel)
            .collect(Collectors.toUnmodifiableSet());

    private DisposableDomains() {
    }

    /** True when {@code domain} is exactly one of the blocked disposable domains. */
    public static boolean isDisposable(String domain) {
        return domain != null && DISPOSABLE_DOMAINS.contains(domain.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * True when {@code email}'s domain is disposable-adjacent: its second-level label matches a
     * blocked disposable domain's, whether under a different TLD or as a subdomain. A null, blank or
     * label-less domain is not adjacent.
     */
    public static boolean isAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        String label = secondLevelLabel(domain);
        return label != null && DISPOSABLE_LABELS.contains(label);
    }

    /** The label immediately before the top-level label (the "core" name), or null when absent. */
    private static String secondLevelLabel(String domain) {
        if (domain == null) {
            return null;
        }
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : null;
    }
}
