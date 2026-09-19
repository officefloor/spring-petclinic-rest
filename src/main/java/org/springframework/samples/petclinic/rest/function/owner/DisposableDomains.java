package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;

import org.springframework.util.StringUtils;

/**
 * The clinic's shared knowledge of disposable (throwaway) email domains. Two related checks are
 * expressed against the same blocklist so they cannot drift apart:
 *
 * <ul>
 * <li>{@link #isBlocked(String) blocked} — the domain is itself a known disposable domain. Such a
 * request is rejected outright (see {@link RejectDisposableEmail}).</li>
 * <li>{@link #isAdjacent(String) disposable-adjacent} — the domain is a subdomain of a known
 * disposable domain (e.g. {@code inbox.mailinator.com}). It is not blocked, so the owner is still
 * created, but it neighbours a disposable domain and so counts towards the owner's risk flag.</li>
 * </ul>
 */
final class DisposableDomains {

    /** Domains that only ever host throwaway mailboxes. */
    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableDomains() {
    }

    /** The lower-cased domain of an email address, or {@code null} when there is no usable domain. */
    static String domainOf(String email) {
        if (!StringUtils.hasText(email)) {
            return null;
        }
        int at = email.indexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).toLowerCase();
    }

    /** Whether the domain is itself a known disposable domain. */
    static boolean isBlocked(String domain) {
        return domain != null && BLOCKED.contains(domain);
    }

    /**
     * Whether the domain is disposable-adjacent: a subdomain of a known disposable domain but not
     * itself blocked.
     */
    static boolean isAdjacent(String domain) {
        if (domain == null || isBlocked(domain)) {
            return false;
        }
        return BLOCKED.stream().anyMatch(blocked -> domain.endsWith("." + blocked));
    }
}
