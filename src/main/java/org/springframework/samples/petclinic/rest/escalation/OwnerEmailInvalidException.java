package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create/update-owner request supplies an {@code email} that is not a
 * syntactically valid address. Handled globally by
 * {@link OwnerEmailInvalidExceptionHandler}, which responds 400.
 */
public class OwnerEmailInvalidException extends Exception {

    public OwnerEmailInvalidException(String email) {
        super("Email must be a syntactically valid address: " + email);
    }
}
