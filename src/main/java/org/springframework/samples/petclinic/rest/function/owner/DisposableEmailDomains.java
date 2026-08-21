package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

/**
 * The disposable email-domain blocklist and the checks over it. A domain is
 * <em>blocked</em> when it is exactly on the list (rejected at create by
 * {@link ValidateOwnerEmailDomain}); it is <em>disposable-adjacent</em> when it shares a
 * blocked domain's registrable base label — the same second-level name under any TLD, or
 * any subdomain of it (e.g. {@code mailinator.net} or {@code x.mailinator.com} are adjacent
 * to the blocked {@code mailinator.com}). An exactly-blocked domain is disposable-adjacent too.
 */
public final class DisposableEmailDomains {

    private static final Set<String> BLOCKED_DOMAINS = Set.of(
            "mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /** The domain part of an email, lower-cased, or {@code null} when absent or malformed. */
    static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        String trimmed = email.trim();
        int at = trimmed.lastIndexOf('@');
        if (at < 0 || at == trimmed.length() - 1) {
            return null;
        }
        return trimmed.substring(at + 1).toLowerCase(Locale.ROOT);
    }

    /** True when the email's domain is exactly on the disposable blocklist. */
    static boolean isBlocked(String email) {
        String domain = domainOf(email);
        return domain != null && BLOCKED_DOMAINS.contains(domain);
    }

    /**
     * True when the email's domain is disposable-adjacent: it shares the registrable base
     * label of a blocked domain (the same second-level name under any TLD, or a subdomain of a
     * blocked domain). An exactly-blocked domain is adjacent too. False when the email is absent
     * or has no domain.
     */
    public static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null) {
            return false;
        }
        String base = registrableBase(domain);
        for (String blocked : BLOCKED_DOMAINS) {
            if (base.equals(registrableBase(blocked))) {
                return true;
            }
        }
        return false;
    }

    /** The second-level label of a dotted domain ({@code x.mailinator.com} -> {@code mailinator}). */
    private static String registrableBase(String domain) {
        String[] labels = domain.split("\\.");
        if (labels.length >= 2) {
            return labels[labels.length - 2];
        }
        return domain;
    }
}
