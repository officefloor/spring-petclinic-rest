package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link ValidateOwnerFields} when a create-owner request's telephone does not
 * contain exactly ten digits once every non-digit character has been stripped. Handled by
 * {@code InvalidOwnerTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidOwnerTelephoneException extends Exception {

    public InvalidOwnerTelephoneException(String normalized) {
        super("Telephone must contain exactly 10 digits after stripping non-digit characters, but had "
                + normalized.length() + ": '" + normalized + "'");
    }
}
