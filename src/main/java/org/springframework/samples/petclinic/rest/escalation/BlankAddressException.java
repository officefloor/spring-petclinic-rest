package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner's address is blank after normalization (trim, collapse whitespace,
 * upper-case and expand abbreviations). The required-field check runs against the
 * normalized form, so a value made up only of whitespace no longer satisfies it. Handled
 * globally by {@link BlankAddressExceptionHandler}, which responds 400.
 */
public class BlankAddressException extends Exception {

    private final String rejectedValue;

    public BlankAddressException(String rejectedValue) {
        super("Address is required and must not be blank after normalization");
        this.rejectedValue = rejectedValue;
    }

    /** The original address value that was blank after normalization. */
    public String getRejectedValue() {
        return this.rejectedValue;
    }
}
