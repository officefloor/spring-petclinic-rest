package org.springframework.samples.petclinic.util;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Knowledge of the disposable email-domain blocklist and the related
 * "disposable-adjacent" heuristic, kept in one place so the create-time validation
 * and the owner risk flag agree on the same list.
 *
 * <p>A domain is <em>disposable</em> when it is exactly one of the blocklisted
 * throw-away email providers ({@code mailinator.com}, {@code tempmail.com},
 * {@code guerrillamail.com}); the create endpoint rejects such an email outright.
 *
 * <p>A domain is <em>disposable-adjacent</em> when it is not itself blocklisted but
 * shares a blocklisted provider's registrable label — its second-level label (the
 * label immediately before the final TLD label) equals that of a blocklisted
 * domain. This catches the same provider under a different TLD
 * ({@code mailinator.net}) and subdomains of a blocklisted provider
 * ({@code signup.mailinator.com}), which pass create-time validation but are still
 * worth flagging as risky. All comparisons are case-insensitive.
 */
public final class DisposableDomains {

    /**
     * The exact disposable email domains that are rejected at create time.
     */
    private static final Set<String> DISPOSABLE_DOMAINS =
        Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The second-level labels of the blocklisted domains (e.g. {@code mailinator}),
     * used for the disposable-adjacent heuristic.
     */
    private static final Set<String> DISPOSABLE_LABELS = DISPOSABLE_DOMAINS.stream()
        .map(DisposableDomains::secondLevelLabel)
        .collect(Collectors.toUnmodifiableSet());

    private DisposableDomains() {
    }

    /**
     * Whether {@code email}'s domain is exactly one of the blocklisted disposable domains (see
     * {@link #isDisposable(String)}). An absent email, or one without a domain part, is not
     * disposable.
     */
    public static boolean isDisposableEmail(String email) {
        return isDisposable(domainOf(email));
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent (see
     * {@link #isDisposableAdjacent(String)}). An absent email, or one without a domain part, is not
     * disposable-adjacent.
     */
    public static boolean isDisposableAdjacentEmail(String email) {
        return isDisposableAdjacent(domainOf(email));
    }

    /**
     * Extract the domain part of an email address: everything after the last {@code '@'}. Returns
     * {@code null} when the email is {@code null}, has no {@code '@'}, or ends with {@code '@'} (no
     * domain), so the domain-level checks treat it as not disposable.
     */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1);
    }

    /**
     * Whether {@code domain} is exactly one of the blocklisted disposable domains.
     * A {@code null} or blank domain is not disposable.
     */
    public static boolean isDisposable(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        return DISPOSABLE_DOMAINS.contains(domain.toLowerCase());
    }

    /**
     * Whether {@code domain} is disposable-adjacent: not itself blocklisted, but sharing
     * a blocklisted provider's second-level label (a different TLD or a subdomain of a
     * blocklisted provider). A {@code null} or blank domain is not adjacent.
     */
    public static boolean isDisposableAdjacent(String domain) {
        if (domain == null || domain.isBlank()) {
            return false;
        }
        String normalized = domain.toLowerCase();
        if (DISPOSABLE_DOMAINS.contains(normalized)) {
            return false;
        }
        return DISPOSABLE_LABELS.contains(secondLevelLabel(normalized));
    }

    /**
     * The second-level label of a domain: the dot-separated label immediately before the
     * final (TLD) label, or the whole string when it has no dot. Returns the input
     * unchanged when it is {@code null}.
     */
    private static String secondLevelLabel(String domain) {
        if (domain == null) {
            return null;
        }
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return domain;
        }
        return labels[labels.length - 2];
    }
}
