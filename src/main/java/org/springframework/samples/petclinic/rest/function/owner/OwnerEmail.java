package org.springframework.samples.petclinic.rest.function.owner;

import java.util.List;
import java.util.Locale;
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

    private OwnerEmail() {
    }

    /**
     * Validates and normalizes the email on the request in place. When an email is present but not
     * syntactically valid, adds {@code "email"} to {@code errors} (a 400). When valid, rewrites it
     * lower-cased so persistence and the response carry the canonical form. Absent/blank is a no-op.
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
        request.setEmail(email.toLowerCase(Locale.ROOT));
    }
}
