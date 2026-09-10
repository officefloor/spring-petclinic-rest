package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link ValidateOwnerFields} when a create-owner request's telephone cannot be
 * normalized into valid E.164 form (8 to 15 digits after the '+'). Handled by
 * {@code InvalidOwnerTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidOwnerTelephoneException extends Exception {

    public InvalidOwnerTelephoneException(String telephone) {
        super("Telephone cannot be normalized into valid E.164 form: '" + telephone + "'");
    }
}
