package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner} when an
 * owner email is present on the request but is not a syntactically valid address. Handled by
 * {@link OwnerEmailInvalidExceptionHandler}, which responds 400.
 */
public class OwnerEmailInvalidException extends Exception {

    private final String submitted;

    public OwnerEmailInvalidException(String submitted) {
        super("Email is not a syntactically valid address: " + submitted);
        this.submitted = submitted;
    }

    public String getSubmitted() {
        return this.submitted;
    }
}
