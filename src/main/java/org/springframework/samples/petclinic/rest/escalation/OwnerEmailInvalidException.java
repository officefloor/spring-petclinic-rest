package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the owner pipelines when an owner supplies an {@code email} that is present
 * but not a syntactically valid address.
 *
 * <p>Carries the offending value so {@link OwnerEmailInvalidExceptionHandler} can respond
 * 400 explaining why the email was rejected.
 */
public class OwnerEmailInvalidException extends Exception {

    private final String email;

    public OwnerEmailInvalidException(String email) {
        super("Email must be a syntactically valid address, but was: '" + email + "'");
        this.email = email;
    }

    public String getEmail() {
        return email;
    }
}
