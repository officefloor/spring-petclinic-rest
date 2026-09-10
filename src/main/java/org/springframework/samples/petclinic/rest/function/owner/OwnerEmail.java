package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Set;
import java.util.regex.Pattern;

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
}
