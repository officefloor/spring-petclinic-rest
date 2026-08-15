package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Shared handling of the optional owner {@code email}: when present it must be a syntactically
 * valid address, and is stored (and later returned) lower-cased. Absent or blank email is accepted.
 *
 * <p>Used by both create ({@link ValidateOwnerFields}) and update ({@link ValidateOwner}) so the two
 * endpoints treat email identically.
 */
final class OwnerEmail {

    /**
     * Practical RFC-5322-ish syntactic check: a non-empty local part, an {@code @}, and a dotted
     * domain ending in a letter-only TLD of at least two characters. Rejects values with whitespace
     * or a missing {@code @}/domain (e.g. {@code not-an-email}).
     */
    private static final Pattern EMAIL = Pattern.compile(
        "^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*"
            + "@(?:[A-Za-z0-9](?:[A-Za-z0-9-]*[A-Za-z0-9])?\\.)+[A-Za-z]{2,}$");

    /**
     * Disposable email domains that are not accepted: an email on one of these domains is rejected
     * (400) even though it is syntactically valid. Compared case-insensitively.
     */
    private static final Set<String> DISPOSABLE_DOMAINS = Set.of(
        "mailinator.com", "tempmail.com", "guerrillamail.com");

    private OwnerEmail() {
    }

    /**
     * Validates and normalizes the email on the request in place. When an email is present but not
     * syntactically valid, or its domain is on the disposable-domain blocklist, adds {@code "email"}
     * to {@code errors} (a 400). When valid, rewrites it lower-cased so persistence and the response
     * carry the canonical form. Absent/blank is a no-op.
     */
    static void normalize(OwnerFieldsDto request, List<String> errors) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            errors.add("email");
            return;
        }
        String normalized = email.toLowerCase(Locale.ROOT);
        String domain = normalized.substring(normalized.lastIndexOf('@') + 1);
        if (DISPOSABLE_DOMAINS.contains(domain)) {
            errors.add("email");
            return;
        }
        request.setEmail(normalized);
    }
}
