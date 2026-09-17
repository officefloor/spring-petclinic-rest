package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries an email that is not a syntactically valid
 * address. Email is optional, so this is raised only when a non-blank value is present.
 * Handled by {@link InvalidEmailExceptionHandler} as a 400.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException() {
        super("Email must be a syntactically valid address");
    }
}
