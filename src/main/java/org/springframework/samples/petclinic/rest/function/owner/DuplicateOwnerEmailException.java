package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueOwnerEmail} when a create-owner request's lower-cased
 * email is already used by an existing owner. Handled by
 * {@code DuplicateOwnerEmailExceptionHandler}, which responds 409.
 */
public class DuplicateOwnerEmailException extends Exception {

    public DuplicateOwnerEmailException(String email) {
        super("Email already in use by another owner: '" + email + "'");
    }
}
