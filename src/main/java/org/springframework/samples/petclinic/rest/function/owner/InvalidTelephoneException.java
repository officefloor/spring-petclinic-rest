package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeTelephone} when a create-owner request's telephone does not contain
 * exactly ten digits once every non-digit character has been stripped. Handled globally by
 * {@code InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits after removing non-digit characters: "
                + telephone);
    }
}
