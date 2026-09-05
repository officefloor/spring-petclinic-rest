package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;

/**
 * Single source of truth for the disposable-email-domain rules.
 *
 * <p>Two related notions sit on the same blocklist:
 * <ul>
 * <li><b>Blocked</b> — an <em>exact</em> match against {@link #BLOCKED}. These are rejected outright at
 * create time by {@link RejectDisposableEmailDomain} (a 400), so a blocked domain never reaches storage.
 * <li><b>Disposable-adjacent</b> — a softer, non-rejecting signal used by the {@code riskFlag}
 * ({@link RiskFlag}): a domain that is not necessarily blocked but is clearly a sibling of one — a
 * subdomain of a blocked domain (e.g. {@code mail.mailinator.com}) or a same-brand domain on a different
 * TLD (e.g. {@code mailinator.net}, {@code tempmail.io}), matched by a shared second-level label.
 * </ul>
 */
public final class DisposableEmailDomains {

    /** Domains whose addresses are rejected outright at create time. */
    static final Set<String> BLOCKED = Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableEmailDomains() {
    }

    /** The lower-cased domain part of an email, or {@code null} when there is no usable domain. */
    static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        String raw = email.trim();
        int at = raw.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        String domain = raw.substring(at + 1).trim().toLowerCase(Locale.ROOT);
        return domain.isEmpty() ? null : domain;
    }

    /** True when the domain is an exact match against the blocklist. */
    static boolean isBlocked(String domain) {
        return domain != null && BLOCKED.contains(domain);
    }

    /** The second-level label of a domain: {@code mailinator.com} -&gt; {@code mailinator},
     *  {@code mail.tempmail.io} -&gt; {@code tempmail}. Returns the whole value when it has no dot. */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length < 2 ? domain : labels[labels.length - 2];
    }

    /**
     * True when the email's domain is disposable-adjacent: a subdomain of a blocked domain, or a domain
     * sharing the second-level label of a blocked one (same brand, any TLD). An absent, blank or
     * domain-less email is never adjacent.
     */
    static boolean isDisposableAdjacent(String email) {
        String domain = domainOf(email);
        if (domain == null) {
            return false;
        }
        String sld = secondLevelLabel(domain);
        for (String blocked : BLOCKED) {
            if (domain.equals(blocked) || domain.endsWith("." + blocked)
                    || sld.equals(secondLevelLabel(blocked))) {
                return true;
            }
        }
        return false;
    }
}
