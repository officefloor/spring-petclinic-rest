package org.springframework.samples.petclinic.util;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pinned blocklist of disposable email domains. An owner whose email is registered under one of these
 * throwaway providers is rejected, since such an address cannot be relied on for contact. Pure lookup
 * with no dependency on other owners, so it is applied at request time.
 */
public final class DisposableEmailDomains {

    /** Known disposable-mail providers, held lower-cased for case-insensitive matching. */
    private static final Set<String> BLOCKED = Set.of(
            "mailinator.com",
            "tempmail.com",
            "guerrillamail.com");

    /** The registrable labels of the blocked providers (e.g. 'mailinator'), the single source of
     *  truth for {@link #isDisposableAdjacent(String)}. */
    private static final Set<String> BLOCKED_LABELS = BLOCKED.stream()
            .map(DisposableEmailDomains::registrableLabel)
            .collect(Collectors.toUnmodifiableSet());

    private DisposableEmailDomains() {
    }

    /** Whether {@code domain} is a blocked disposable-mail provider (case-insensitive). */
    public static boolean isBlocked(String domain) {
        return domain != null && BLOCKED.contains(domain.toLowerCase());
    }

    /**
     * Whether {@code domain} is disposable-<em>adjacent</em>: it is not itself blocked, yet shares a
     * registrable label with a known disposable provider — a different TLD ({@code mailinator.net}) or
     * a subdomain ({@code smtp.mailinator.com}). Such an address is allowed but worth flagging. A
     * blocked domain is not adjacent (it is disposable outright), and an unrelated domain is not
     * adjacent. Case-insensitive; {@code null} or blank is not adjacent.
     */
    public static boolean isDisposableAdjacent(String domain) {
        if (domain == null || domain.isBlank() || isBlocked(domain)) {
            return false;
        }
        return BLOCKED_LABELS.contains(registrableLabel(domain));
    }

    /** The registrable label of a domain: its second-level part ('mailinator' of 'smtp.mailinator.net'),
     *  lower-cased. The whole (lower-cased) value when it has no dot. */
    private static String registrableLabel(String domain) {
        String[] parts = domain.toLowerCase().split("\\.");
        return parts.length >= 2 ? parts[parts.length - 2] : domain.toLowerCase();
    }
}
