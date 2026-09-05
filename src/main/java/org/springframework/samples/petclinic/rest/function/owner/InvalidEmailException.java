package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeEmail} when an owner request supplies an {@code email} that is not a
 * syntactically valid address. Handled globally by {@code InvalidEmailExceptionHandler}, which
 * responds 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
    }
}
