package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner} when the
 * owner telephone does not contain exactly ten digits after every non-digit character has been
 * stripped. Handled by {@link OwnerTelephoneInvalidExceptionHandler}, which responds 400.
 */
public class OwnerTelephoneInvalidException extends Exception {

    private final String submitted;

    public OwnerTelephoneInvalidException(String submitted) {
        super("Telephone must contain exactly 10 digits after stripping non-digits: " + submitted);
        this.submitted = submitted;
    }

    public String getSubmitted() {
        return this.submitted;
    }
}
