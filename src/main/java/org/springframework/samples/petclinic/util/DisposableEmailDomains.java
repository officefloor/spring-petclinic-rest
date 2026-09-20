package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The fixed blocklist of disposable email domains that owner registrations are not
 * allowed to use. Kept separate from the syntactic email check so the two concerns
 * stay independent and reusable.
 */
public final class DisposableEmailDomains {

    /** Domains known to hand out throwaway inboxes. */
    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The registrable base label of each blocked domain (e.g. {@code "mailinator"} for
     * {@code "mailinator.com"}), used to spot domains that are merely adjacent to a
     * blocked one.
     */
    private static final Set<String> BLOCKED_BASE_LABELS = BLOCKED.stream()
            .map(DisposableEmailDomains::baseLabel)
            .filter(Objects::nonNull)
            .collect(Collectors.toUnmodifiableSet());

    private DisposableEmailDomains() {
    }

    /**
     * Whether an email address's domain is on the disposable-domain blocklist. Matching
     * is case-insensitive; an address with no '@' or no domain is not blocked (its
     * syntax is the concern of the email format check).
     *
     * @param email the candidate email address
     * @return {@code true} when the domain after the last '@' is blocked
     */
    public static boolean isBlocked(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED.contains(domain);
    }

    /**
     * Whether an email address's domain is adjacent to a disposable one: it shares a
     * blocked domain's registrable base label (the label immediately left of the final
     * label) yet is not necessarily the blocked domain itself. This catches subdomains of
     * a blocked domain (e.g. {@code "signup.mailinator.com"}) and blocked look-alikes on a
     * different top-level domain (e.g. {@code "mailinator.net"}), which pass the exact
     * {@link #isBlocked(String) blocklist} but still smell disposable. Matching is
     * case-insensitive; an address with no '@' or no domain is not adjacent.
     *
     * @param email the candidate email address
     * @return {@code true} when the domain shares a blocked domain's base label
     */
    public static boolean isDisposableAdjacent(String email) {
        String base = baseLabel(domainOf(email));
        return base != null && BLOCKED_BASE_LABELS.contains(base);
    }

    /**
     * The lower-cased domain after the last '@', or null when the address has no '@' or
     * no domain part.
     */
    private static String domainOf(String email) {
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
     * The registrable base label of a domain: the dot-separated label immediately left of
     * the final label (e.g. {@code "mailinator"} for both {@code "mailinator.com"} and
     * {@code "signup.mailinator.com"}). Null when the domain has fewer than two labels.
     */
    private static String baseLabel(String domain) {
        if (domain == null) {
            return null;
        }
        String[] labels = domain.split("\\.");
        return labels.length < 2 ? null : labels[labels.length - 2];
    }
}
