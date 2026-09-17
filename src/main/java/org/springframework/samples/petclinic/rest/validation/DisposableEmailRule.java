package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Decides whether an email address belongs to a disposable-email provider.
 *
 * <p>Disposable providers hand out throwaway inboxes, so owners registered with one cannot be
 * reliably contacted. An email is rejected when the domain after its {@code '@'} is on a small,
 * fixed blocklist ({@code mailinator.com}, {@code tempmail.com}, {@code guerrillamail.com}). The
 * domain is compared case-insensitively; an absent email carries no domain and is accepted.
 */
public final class DisposableEmailRule {

    /** Lower-cased domains whose emails are rejected as disposable. */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com",
        "tempmail.com",
        "guerrillamail.com");

    /** The second-level labels of the blocked domains (e.g. {@code mailinator} for {@code mailinator.com}). */
    private static final Set<String> DISPOSABLE_LABELS = BLOCKED_DOMAINS.stream()
        .map(DisposableEmailRule::secondLevelLabel)
        .collect(Collectors.toUnmodifiableSet());

    private DisposableEmailRule() {
    }

    /**
     * Whether {@code email} uses a disposable-email domain.
     *
     * @param email the owner's email, possibly {@code null} or without an {@code '@'}
     * @return {@code true} when the domain following the last {@code '@'} is on the blocklist,
     *         {@code false} otherwise (including for a {@code null} email or one with no domain)
     */
    public static boolean isDisposable(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent: it shares the second-level label of a
     * known disposable domain even though it is not itself blocked, catching the same provider under
     * a different TLD (e.g. {@code mailinator.net}) or a subdomain (e.g. {@code mx.mailinator.com}).
     *
     * @param email the owner's email, possibly {@code null} or without an {@code '@'}
     * @return {@code true} when the domain's second-level label is one of a blocked provider's,
     *         {@code false} otherwise (including for a {@code null} email or one with no domain)
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        return domain != null && DISPOSABLE_LABELS.contains(secondLevelLabel(domain));
    }

    /** The lower-cased domain following the last {@code '@'}, or {@code null} when absent. */
    private static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.lastIndexOf('@');
        return at < 0 ? null : email.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /** The registrable second-level label of {@code domain} (the label left of the final TLD label). */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length < 2 ? domain : labels[labels.length - 2];
    }
}
