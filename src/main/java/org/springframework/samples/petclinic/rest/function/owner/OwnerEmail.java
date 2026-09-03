package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Normalizes the optional owner {@code email}. Email is absent when null or blank; when present it must
 * be a syntactically valid address, and is stored/returned lower-cased.
 */
final class OwnerEmail {

    /**
     * A pragmatic syntactic check: a non-empty local part, a single {@code @}, and a domain carrying at
     * least one dot, none of the three parts containing whitespace or an {@code @}.
     */
    private static final Pattern SYNTAX = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private OwnerEmail() {
    }

    /**
     * @return the trimmed, lower-cased email, or {@code null} when it is absent (null or blank).
     * @throws InvalidEmailException when present but not syntactically valid.
     */
    static String normalize(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            return null;
        }
        String trimmed = email.trim();
        if (!SYNTAX.matcher(trimmed).matches()) {
            throw new InvalidEmailException(email);
        }
        return trimmed.toLowerCase();
    }
}
