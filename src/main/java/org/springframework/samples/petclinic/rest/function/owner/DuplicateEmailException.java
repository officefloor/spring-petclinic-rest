package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link RejectDuplicateEmail} when a create-owner request's lower-cased email is already
 * used by another owner. Handled globally by {@code DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email already in use by another owner: " + email);
    }
}
