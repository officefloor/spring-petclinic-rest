package org.springframework.samples.petclinic.model;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Single source of truth for the set of known disposable-email providers, plus the two
 * views over it that the application needs:
 *
 * <ul>
 *   <li>an <em>exact</em> full-domain match — the hard rejection applied at owner creation
 *       (a disposable address is a 400); and</li>
 *   <li><em>base-label adjacency</em> — the soft risk signal behind
 *       {@link Owner#getRiskFlag() riskFlag}, which also catches neighbours of the blocklist
 *       such as a different TLD or a subdomain.</li>
 * </ul>
 *
 * <p>Both views derive from the one {@link #DOMAINS} set, so adding a provider is a
 * single-line edit in one place. It lives in the model layer — the lower, more fundamental
 * layer — so both the request-function layer (which rejects a disposable address) and the
 * derived entity getter {@code Owner.getRiskFlag()} can depend on it in the natural
 * {@code rest -> model} direction, rather than the fact being owned upstream and copied down.
 */
public final class DisposableEmailDomains {

    /** The canonical set of known disposable-provider domains; the one place a new provider is added. */
    private static final Set<String> DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Base labels (the registrable name before the final TLD) of {@link #DOMAINS}, derived once. */
    private static final Set<String> BASE_LABELS = DOMAINS.stream()
        .map(DisposableEmailDomains::baseLabel)
        .collect(Collectors.toUnmodifiableSet());

    private DisposableEmailDomains() {
    }

    /** The lower-cased domain part (after the last '@') of {@code email}, or null when absent. */
    public static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        return at < 0 ? null : email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /** True when {@code email}'s domain is exactly one of the known disposable-provider domains. */
    public static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && DOMAINS.contains(domain);
    }

    /**
     * Whether {@code email}'s domain is "disposable-adjacent": one of its dot-separated labels
     * is the base label of a known disposable provider. This matches the exact domains and
     * their neighbours alike — a different TLD ({@code mailinator.net}) or a subdomain
     * ({@code x.mailinator.com}). An absent or domain-less email is not adjacent.
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null) {
            return false;
        }
        for (String label : domain.split("\\.")) {
            if (BASE_LABELS.contains(label)) {
                return true;
            }
        }
        return false;
    }

    /** The registrable base label of {@code domain}: the label immediately before the final TLD. */
    private static String baseLabel(String domain) {
        int lastDot = domain.lastIndexOf('.');
        String withoutTld = lastDot < 0 ? domain : domain.substring(0, lastDot);
        int prevDot = withoutTld.lastIndexOf('.');
        return prevDot < 0 ? withoutTld : withoutTld.substring(prevDot + 1);
    }
}
