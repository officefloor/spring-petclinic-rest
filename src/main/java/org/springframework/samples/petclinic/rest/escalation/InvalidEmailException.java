package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request carries an email that is not a syntactically valid address. Carries the
 * offending value so {@link InvalidEmailExceptionHandler} can report it. Handled as a 400, distinct
 * from bean-validation's {@code MethodArgumentNotValidException}.
 */
public class InvalidEmailException extends Exception {

    private final String email;

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
