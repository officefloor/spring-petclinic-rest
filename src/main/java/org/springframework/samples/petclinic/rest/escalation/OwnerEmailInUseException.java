package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request supplies an email that is already used by another owner
 * (compared lower-cased). Handled by {@link OwnerEmailInUseExceptionHandler}, which
 * responds 409 Conflict.
 */
public class OwnerEmailInUseException extends Exception {

    private final String email;

    public OwnerEmailInUseException(String email) {
        super("Email already in use: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
