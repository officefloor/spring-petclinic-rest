package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the submitted owner's normalized telephone is already used by
 * another owner. Handled globally by {@link DuplicateTelephoneExceptionHandler}, which
 * responds 409 Conflict.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone is already used by another owner (rejected value: " + telephone + ")");
    }
}
