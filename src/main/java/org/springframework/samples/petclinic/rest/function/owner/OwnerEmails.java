package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.rest.escalation.InvalidEmailException;

/**
 * Shared email handling for the owner pipeline. Not an OfficeFloor function class (it is never
 * referenced from a step), so it may expose helper methods.
 */
final class OwnerEmails {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    private OwnerEmails() {
    }

    /**
     * Returns the email lower-cased, or the original value unchanged when absent (null or blank).
     * Throws when a present value is not a syntactically valid address.
     */
    static String normalize(String email) throws InvalidEmailException {
        if (email == null || email.isBlank()) {
            return email;
        }
        if (!EMAIL.matcher(email).matches()) {
            throw new InvalidEmailException(email);
        }
        return email.toLowerCase();
    }
}
