package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a telephone that cannot be normalized into a valid
 * E.164 number (a '+' followed by 8 to 15 digits). Handled globally by
 * {@link OwnerTelephoneInvalidExceptionHandler}, which responds 400.
 */
public class OwnerTelephoneInvalidException extends Exception {

    public OwnerTelephoneInvalidException(String telephone) {
        super("Telephone could not be normalized to a valid E.164 number: " + telephone);
    }
}
