package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request supplies an email address that is not
 * syntactically valid.
 */
public class InvalidEmailException extends Exception {

    public InvalidEmailException(String email) {
        super("Email must be a syntactically valid address: " + email);
    }
}
