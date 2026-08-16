package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner}
 * when the owner email is present but is not a syntactically valid address.
 * Handled globally by {@link InvalidEmailExceptionHandler}, which responds 400.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
        this.email = email;
    }

    public String getEmail() {
        return this.email;
    }
}
