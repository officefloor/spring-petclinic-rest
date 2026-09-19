package org.springframework.samples.petclinic.util;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Central knowledge of disposable (throwaway) email providers.
 *
 * <p>An exact match on the {@link #BLOCKED_DOMAINS blocklist} is rejected outright at creation.
 * A domain is <em>disposable-adjacent</em> when it belongs to the same provider family without
 * being an exact blocklist entry: a subdomain of a blocked provider ({@code x.mailinator.com})
 * or the same provider label under a different suffix ({@code mailinator.net}, {@code tempmail.io}).
 * Adjacency is detected by the provider's second-level label (e.g. {@code mailinator}) appearing
 * as a label of the domain, so the two checks stay derived from the single blocklist below.
 */
public final class DisposableEmailDomains {

    /** Fully disposable domains, rejected outright at creation. */
    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Second-level provider labels of the blocked domains (e.g. {@code mailinator}). */
    private static final Set<String> PROVIDER_LABELS = BLOCKED_DOMAINS.stream()
            .map(domain -> domain.substring(0, domain.indexOf('.')))
            .collect(Collectors.toUnmodifiableSet());

    private DisposableEmailDomains() {
    }

    /**
     * Return the lower-cased domain of {@code email}, or {@code null} when the email is null,
     * blank or carries no {@code @}.
     */
    public static String domainOf(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        int at = email.indexOf('@');
        return at < 0 ? null : email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /** Whether {@code domain} is an exact match on the disposable blocklist. */
    public static boolean isBlocked(String domain) {
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * Whether {@code domain} belongs to a known disposable provider family: any of its
     * dot-separated labels is a blocked provider label. This holds for the blocked domains
     * themselves and for their subdomain / alternate-suffix variants.
     */
    public static boolean isDisposableAdjacent(String domain) {
        if (domain == null) {
            return false;
        }
        return Arrays.stream(domain.split("\\.")).anyMatch(PROVIDER_LABELS::contains);
    }
}
