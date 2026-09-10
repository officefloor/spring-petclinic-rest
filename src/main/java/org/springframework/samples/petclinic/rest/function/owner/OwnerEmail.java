package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalization for the optional owner {@code email}: when present it must be a syntactically
 * valid address whose domain is not on the disposable-domain blocklist; it is stored and returned
 * lower-cased. An absent (null/blank) email is left untouched. A syntactically invalid value
 * raises {@link InvalidOwnerEmailException}; a blocklisted domain raises
 * {@link DisposableOwnerEmailException}; both make the endpoint respond 400.
 */
final class OwnerEmail {

    /** A pragmatic address check: one {@code @}, non-empty local and domain parts, a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    /** Disposable email domains rejected outright. */
    private static final Set<String> BLOCKED_DOMAINS =
            Set.of("mailinator.com", "tempmail.com", "guerrillamail.com");

    /** The second-level labels of the blocked domains (e.g. {@code mailinator}). */
    private static final Set<String> BLOCKED_LABELS = BLOCKED_DOMAINS.stream()
            .map(OwnerEmail::secondLevelLabel).collect(Collectors.toUnmodifiableSet());

    private OwnerEmail() {
    }

    /**
     * If the request carries a non-blank email, validate and lower-case it in place; otherwise leave
     * it as-is.
     */
    static void normalize(OwnerFieldsDto request)
            throws InvalidOwnerEmailException, DisposableOwnerEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidOwnerEmailException(email);
        }
        String normalized = email.toLowerCase();
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new DisposableOwnerEmailException(email);
        }
        request.setEmail(normalized);
    }

    /**
     * Whether the given email's domain is <em>disposable-adjacent</em>: a near-variant of a
     * blocklisted disposable domain that dodges the exact-match rejection in {@link #normalize}.
     * It is adjacent when its second-level label equals a blocked domain's — whether under a
     * different public suffix ({@code mailinator.net}) or as a subdomain ({@code x.mailinator.com}).
     * A null, blank or address-less value, or one whose domain matches no blocked label, is not
     * adjacent.
     */
    static boolean isDisposableAdjacent(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }
        int at = email.lastIndexOf('@');
        if (at < 0 || at == email.length() - 1) {
            return false;
        }
        String domain = email.substring(at + 1).toLowerCase();
        return BLOCKED_LABELS.contains(secondLevelLabel(domain));
    }

    /**
     * The second-level label of a dotted domain — the label immediately left of the final
     * (public-suffix) label, e.g. {@code mailinator} for both {@code mailinator.com} and
     * {@code x.mailinator.com}. The whole string when it has no dot.
     */
    private static String secondLevelLabel(String domain) {
        String[] labels = domain.split("\\.");
        return labels.length >= 2 ? labels[labels.length - 2] : domain;
    }
}
