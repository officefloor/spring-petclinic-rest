package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Shared knowledge of disposable email domains. A small, fixed blocklist of exact disposable
 * domains is rejected outright at create time (see {@link RejectDisposableEmailDomain}); this
 * class additionally recognises domains that are merely <em>disposable-adjacent</em> — admitted,
 * but similar enough to a known disposable service to be worth flagging on the response
 * (see the {@code riskFlag} derivation).
 *
 * <p>Adjacency is decided on the domain's <em>second-level label</em>: the label immediately to
 * the left of the top-level domain. A domain is disposable-adjacent when that label matches the
 * second-level label of any blocked domain. This catches the same service under a different TLD
 * ({@code mailinator.net}) and subdomains of a blocked domain ({@code inbox.mailinator.com}),
 * without flagging unrelated domains.
 */
public final class DisposableEmailDomains {

    /** Exact disposable domains that are rejected outright at create time. */
    static final Set<String> BLOCKED =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** Second-level labels of the blocked domains (e.g. "mailinator", "tempmail"). */
    private static final Set<String> BLOCKED_LABELS =
            BLOCKED.stream().map(DisposableEmailDomains::secondLevelLabel).collect(Collectors.toSet());

    private DisposableEmailDomains() {
    }

    /** True when {@code domain} is one of the exactly-blocked disposable domains. */
    static boolean isBlocked(String domain) {
        return domain != null && BLOCKED.contains(domain.toLowerCase(Locale.ROOT));
    }

    /**
     * True when the email's domain is disposable-adjacent: its second-level label matches a known
     * disposable service (a different TLD or a subdomain of a blocked domain both qualify). False
     * when the email is null or carries no domain.
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED_LABELS.contains(secondLevelLabel(domain));
    }

    /** The lower-cased domain part of {@code email}, or null when absent/malformed. */
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

    /** The label immediately left of the TLD (e.g. "inbox.mailinator.com" -> "mailinator"). */
    private static String secondLevelLabel(String domain) {
        String[] parts = domain.split("\\.");
        if (parts.length >= 2) {
            return parts[parts.length - 2];
        }
        return domain;
    }
}
