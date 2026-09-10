package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.dto.OwnerFieldsDto;

/**
 * Normalization for the optional owner {@code email}: when present it must be a syntactically
 * valid address; it is stored and returned lower-cased. An absent (null/blank) email is left
 * untouched. Invalid values raise {@link InvalidOwnerEmailException} so the endpoint responds 400.
 */
final class OwnerEmail {

    /** A pragmatic address check: one {@code @}, non-empty local and domain parts, a dotted domain. */
    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private OwnerEmail() {
    }

    /**
     * If the request carries a non-blank email, validate and lower-case it in place; otherwise leave
     * it as-is.
     */
    static void normalize(OwnerFieldsDto request) throws InvalidOwnerEmailException {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            return;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidOwnerEmailException(email);
        }
        request.setEmail(email.toLowerCase());
    }
}
