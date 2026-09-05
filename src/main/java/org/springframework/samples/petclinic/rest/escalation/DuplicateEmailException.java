package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a normalized (lower-cased) email that is
 * already used by another owner. Handled by {@link DuplicateEmailHandler}, which responds
 * 409.
 */
public class DuplicateEmailException extends Exception {

    public DuplicateEmailException(String message) {
        super(message);
    }
}
