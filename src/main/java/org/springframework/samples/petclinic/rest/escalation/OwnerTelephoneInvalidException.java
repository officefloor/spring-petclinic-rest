package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@link org.springframework.samples.petclinic.rest.function.owner.ValidateOwner} when the
 * owner telephone cannot be converted to a valid E.164 number. Handled by
 * {@link OwnerTelephoneInvalidExceptionHandler}, which responds 400.
 */
public class OwnerTelephoneInvalidException extends Exception {

    private final String submitted;

    public OwnerTelephoneInvalidException(String submitted) {
        super("Telephone cannot form a valid E.164 number: " + submitted);
        this.submitted = submitted;
    }

    public String getSubmitted() {
        return this.submitted;
    }
}
