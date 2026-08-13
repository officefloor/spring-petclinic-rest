package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared handling of an owner's optional {@code email}. Email is optional; when present
 * it must be a syntactically valid address and is stored and returned lower-cased.
 */
final class OwnerEmail {

    /**
     * A pragmatic "syntactically valid address" check: a non-empty local part, an
     * {@code @}, and a domain of at least two dot-separated labels — none of which may
     * contain whitespace or a further {@code @}.
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s.]+(\\.[^@\\s.]+)+$");

    /**
     * Disposable email domains that owners may not register with. Compared lower-cased.
     */
    private static final Set<String> DISPOSABLE_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /**
     * The second-level labels of the blocked {@link #DISPOSABLE_DOMAINS} (e.g.
     * {@code mailinator} for {@code mailinator.com}). A domain is "disposable-adjacent"
     * when it shares one of these labels without being an outright-blocked domain.
     */
    private static final Set<String> DISPOSABLE_LABELS = DISPOSABLE_DOMAINS.stream()
            .map(OwnerEmail::secondLevelLabel).collect(java.util.stream.Collectors.toUnmodifiableSet());

    private OwnerEmail() {
    }

    /**
     * Whether {@code email}'s domain is <em>disposable-adjacent</em>: a near neighbour of
     * a blocked disposable domain that is not itself blocked (blocked domains are rejected
     * by {@link #normalize} before an owner is ever created). A domain qualifies when its
     * second-level label matches a disposable domain's label (see {@link #DISPOSABLE_LABELS})
     * under a different top-level domain (e.g. {@code mailinator.net}) or as a subdomain
     * (e.g. {@code x.mailinator.com}). A {@code null} or blank email is not adjacent.
     */
    static boolean isDisposableAdjacent(String email) {
        if (email == null) {
            return false;
        }
        String trimmed = email.strip();
        if (trimmed.isEmpty()) {
            return false;
        }
        int at = trimmed.indexOf('@');
        if (at < 0 || at == trimmed.length() - 1) {
            return false;
        }
        String domain = trimmed.substring(at + 1).toLowerCase(Locale.ROOT);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            return false; // an outright-blocked domain is rejected, not merely adjacent
        }
        return DISPOSABLE_LABELS.contains(secondLevelLabel(domain));
    }

    /**
     * The second-level label of a dotted domain — the label immediately before the final
     * (top-level) label, e.g. {@code mailinator} for both {@code mailinator.com} and
     * {@code x.mailinator.com}. A domain with fewer than two labels yields the whole domain.
     */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        if (labels.length < 2) {
            return domain;
        }
        return labels[labels.length - 2];
    }

    /**
     * Normalize a supplied email. A {@code null} or blank value is treated as absent and
     * returns {@code null}. A present value must match {@link #EMAIL} and must not use a
     * disposable domain (see {@link #DISPOSABLE_DOMAINS}) or an
     * {@link InvalidEmailException} (400) is thrown; otherwise it is returned lower-cased.
     */
    static String normalize(String email) throws InvalidEmailException {
        if (email == null) {
            return null;
        }
        String trimmed = email.strip();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException("Email is not a syntactically valid address: " + email);
        }
        String normalized = trimmed.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.indexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            throw new InvalidEmailException("Email domain is not permitted: " + email);
        }
        return normalized;
    }
}
