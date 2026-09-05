package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link RejectDuplicateTelephone} when a create-owner request's normalized telephone is
 * already used by another owner. Handled globally by {@code DuplicateTelephoneExceptionHandler},
 * which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use by another owner: " + telephone);
    }
}
