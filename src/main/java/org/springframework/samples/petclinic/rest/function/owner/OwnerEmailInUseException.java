package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerEmailUnique} when a create request carries a lower-cased email that is
 * already used by another owner. Handled globally by {@code OwnerEmailInUseExceptionHandler}, which
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
