package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeTelephone} when a create-owner request's telephone cannot be normalized to
 * valid E.164 form (8 to 15 digits after the '+'). Handled globally by
 * {@code InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot be normalized to valid E.164 (8 to 15 digits after '+'): "
                + telephone);
    }
}
