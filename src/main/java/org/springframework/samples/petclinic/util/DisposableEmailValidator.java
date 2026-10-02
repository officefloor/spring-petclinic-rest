package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates an owner's email address against a fixed blocklist of disposable-email domains.
 *
 * <p>An email is validated ONLY when present; a {@code null} or empty email is always accepted,
 * keeping the request contract backward-compatible. When present, its domain (the part after the
 * last {@code '@'}) is compared case-insensitively against the blocklist and the email is rejected
 * when it matches. The syntactic validity of any value present is already guaranteed by the
 * schema-level {@code format: email} on the request field.
 */
public final class DisposableEmailValidator {

    /** Disposable-email domains that are not accepted (compared in lower case). */
    private static final Set<String> BLOCKED_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The second-level labels of the blocked domains (each registrable name before its final TLD,
     * e.g. {@code mailinator}, {@code tempmail}, {@code guerrillamail}), used to recognise
     * disposable-<em>adjacent</em> domains. Derived from {@link #BLOCKED_DOMAINS} so the single
     * blocklist stays the sole source of truth and the adjacency rule tracks it automatically.
     */
    private static final Set<String> BLOCKED_LABELS = BLOCKED_DOMAINS.stream()
        .map(d -> d.substring(0, d.lastIndexOf('.')))
        .collect(Collectors.toUnmodifiableSet());

    private DisposableEmailValidator() {
    }

    /**
     * Validate an email address, throwing when its domain is on the disposable-domain blocklist.
     *
     * @param email the owner's email, or {@code null} when none was given
     * @throws DisposableEmailException if a non-blank email's domain is on the blocklist.
     */
    public static void validate(String email) {
        if (email == null || email.isEmpty()) {
            return;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableEmailException(email);
        }
    }

    /**
     * Returns {@code true} when the given email's domain is <em>disposable-adjacent</em>: not itself
     * on the hard blocklist (those are rejected at creation and never stored), yet closely related
     * to a disposable domain — either a subdomain of a blocked domain (e.g.
     * {@code 'smtp.mailinator.com'}) or a domain sharing a blocked domain's second-level label under
     * a different TLD (e.g. {@code 'mailinator.net'}). A {@code null} or blank email, or one without
     * a usable domain, is never adjacent.
     *
     * @param email the owner's email, or {@code null} when none was given
     * @return whether the email's domain is disposable-adjacent
     */
    public static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isEmpty()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase(Locale.ROOT);
        if (BLOCKED_DOMAINS.contains(domain)) {
            // Exactly blocked: rejected at creation, so not treated as merely adjacent.
            return false;
        }
        // A subdomain of a blocked domain (e.g. 'smtp.mailinator.com').
        for (String blocked : BLOCKED_DOMAINS) {
            if (domain.endsWith("." + blocked)) {
                return true;
            }
        }
        // A sibling domain sharing a blocked second-level label under a different TLD
        // (e.g. 'mailinator.net'): take the label immediately before the final TLD.
        String[] parts = domain.split("\\.");
        if (parts.length >= 2) {
            String secondLevel = parts[parts.length - 2];
            return BLOCKED_LABELS.contains(secondLevel);
        }
        return false;
    }
}
