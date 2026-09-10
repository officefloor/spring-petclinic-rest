package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link ValidateOwnerFields} and {@link ValidateOwner} when an owner request supplies
 * an {@code email} that is present but not a syntactically valid address. Handled by
 * {@code InvalidOwnerEmailExceptionHandler}, which responds 400.
 */
public class InvalidOwnerEmailException extends Exception {

    public InvalidOwnerEmailException(String email) {
        super("Email must be a syntactically valid address, but was: '" + email + "'");
    }
}
