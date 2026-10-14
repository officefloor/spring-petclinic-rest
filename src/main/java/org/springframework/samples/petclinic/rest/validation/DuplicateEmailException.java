package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when an owner's lower-cased email is already used by another owner.
 */
public class DuplicateEmailException extends RuntimeException {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email is already used by another owner");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
