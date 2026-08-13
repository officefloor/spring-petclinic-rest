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

    private OwnerEmail() {
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
