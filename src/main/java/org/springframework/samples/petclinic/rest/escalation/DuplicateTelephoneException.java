package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request's normalized telephone is already used by another owner. Carries the
 * offending value so {@link DuplicateTelephoneExceptionHandler} can report it. Handled as a 409,
 * distinct from {@link InvalidTelephoneException}'s 400 for a malformed number.
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone is already in use: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
