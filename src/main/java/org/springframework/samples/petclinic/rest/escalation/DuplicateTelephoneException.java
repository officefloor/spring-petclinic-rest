package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request carries a normalized telephone that already belongs
 * to another owner. Handled by {@link DuplicateTelephoneExceptionHandler} as a 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
    }
}
