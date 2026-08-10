package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;
import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared email handling for the owner create/update pipelines. Email is optional; when a
 * request carries one it must be a syntactically valid address, and it is stored and
 * returned lower-cased.
 */
final class OwnerEmail {

    /**
     * A pragmatic address check: a non-empty local part, an {@code @}, then a domain with at
     * least one dot and no whitespace. Rejects values like {@code not-an-email} (no {@code @}).
     */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private OwnerEmail() {
    }

    /**
     * Validates and normalizes the email on the request in place. Absent (null) email is left
     * untouched; a present value is trimmed, validated and lower-cased.
     *
     * @throws InvalidEmailException when an email is present but not a valid address.
     */
    static void normalize(OwnerFieldsDto request) throws InvalidEmailException {
        String email = request.getEmail();
        if (email == null) {
            return;
        }
        String trimmed = email.trim();
        if (!EMAIL.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        request.setEmail(trimmed.toLowerCase(Locale.ROOT));
    }
}
