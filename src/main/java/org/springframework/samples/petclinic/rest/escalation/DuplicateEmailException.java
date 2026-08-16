package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.CheckEmailUnique}
 * when the lower-cased owner email is already used by another owner.
 * Handled globally by {@link DuplicateEmailExceptionHandler}, which responds 409.
 */
public class DuplicateEmailException extends Exception {

    private final String email;

    public DuplicateEmailException(String email) {
        super("Email is already used by another owner: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
