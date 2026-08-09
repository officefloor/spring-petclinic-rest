package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a telephone that, after every non-digit
 * character is stripped, is not exactly ten digits. Handled globally by
 * {@link OwnerTelephoneInvalidExceptionHandler}, which responds 400.
 */
public class OwnerTelephoneInvalidException extends Exception {

    public OwnerTelephoneInvalidException(String telephone) {
        super("Telephone must be exactly 10 digits after removing non-digit characters: " + telephone);
    }
}
