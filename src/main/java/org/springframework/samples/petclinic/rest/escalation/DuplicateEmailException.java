package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckEmailUnique}
 * when a create-owner request carries a lower-cased email already used by another owner.
 * Handled globally by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String email) {
        super("Email already in use: " + email);
    }
}
