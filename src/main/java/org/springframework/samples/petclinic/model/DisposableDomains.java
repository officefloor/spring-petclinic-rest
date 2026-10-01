package org.springframework.samples.petclinic.model;

import java.util.Set;

/**
 * Single source of truth for the disposable-email-domain policy shared by the create-time rejection
 * (a body whose email domain is <em>on</em> the blocklist is refused with a 400) and the softer,
 * read-time {@code riskFlag} signal (an owner whose stored email domain is <em>adjacent</em> to the
 * blocklist is flagged but not rejected). Keeping both notions here means they cannot drift.
 *
 * <p>The blocklist is matched exactly (case-insensitively). A domain is <em>adjacent</em> when it is
 * not itself on the blocklist yet still resembles one of its members: it is a subdomain of a
 * blocklisted domain (e.g. {@code signup.mailinator.com}) or it shares a blocklisted domain's base
 * label - the registrable name before its public suffix - under a different suffix (e.g.
 * {@code mailinator.net}, {@code tempmail.io}). An owner can only have been stored with an adjacent
 * (never a blocklisted) domain, since the blocklisted ones are rejected before they are saved.
 */
public final class DisposableDomains {

    /** Disposable email domains that are refused outright; compared case-insensitively. */
    public static final Set<String> BLOCKLIST =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    private DisposableDomains() {
    }

    /**
     * The lower-cased domain part of an email - the single place email-to-domain derivation lives, so
     * the create-time and read-time checks parse an address identically.
     *
     * @param email an email address, or {@code null}
     * @return the lower-cased domain after the {@code '@'}, or {@code null} when there is no usable one
     */
    public static String domainOf(String email) {
        if (email == null) {
            return null;
        }
        int at = email.indexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return null;
        }
        return email.substring(at + 1).toLowerCase();
    }

    /**
     * Whether {@code domain} is exactly on the disposable-domain blocklist.
     *
     * @param domain the domain part of an email, or {@code null}
     * @return {@code true} when the (case-insensitive) domain is a blocklist member
     */
    public static boolean isBlocked(String domain) {
        return domain != null && BLOCKLIST.contains(domain.toLowerCase());
    }

    /**
     * Whether {@code email}'s domain is disposable-adjacent; see {@link #isAdjacent(String)}.
     *
     * @param email an email address, or {@code null}
     * @return {@code true} when the email's domain is adjacent to a blocklisted disposable domain
     */
    public static boolean isAdjacentEmail(String email) {
        return isAdjacent(domainOf(email));
    }

    /**
     * Whether {@code domain} is disposable-<em>adjacent</em>: not on the blocklist, but a subdomain
     * of a blocklisted domain or sharing a blocklisted domain's base label under a different suffix.
     *
     * @param domain the domain part of an email, or {@code null}
     * @return {@code true} when the domain is adjacent to a blocklisted disposable domain
     */
    public static boolean isAdjacent(String domain) {
        if (domain == null) {
            return false;
        }
        String d = domain.toLowerCase();
        if (BLOCKLIST.contains(d)) {
            return false; // exactly blocklisted is handled by isBlocked, not adjacency
        }
        String base = baseLabel(d);
        for (String blocked : BLOCKLIST) {
            if (d.endsWith("." + blocked)) {
                return true; // subdomain of a blocklisted domain
            }
            if (base.equals(baseLabel(blocked))) {
                return true; // same base label under a different public suffix
            }
        }
        return false;
    }

    /** The registrable name: the label immediately before the final dot (or the whole string). */
    private static String baseLabel(String domain) {
        int lastDot = domain.lastIndexOf('.');
        if (lastDot <= 0) {
            return domain;
        }
        String withoutSuffix = domain.substring(0, lastDot);
        int prevDot = withoutSuffix.lastIndexOf('.');
        return prevDot < 0 ? withoutSuffix : withoutSuffix.substring(prevDot + 1);
    }
}
