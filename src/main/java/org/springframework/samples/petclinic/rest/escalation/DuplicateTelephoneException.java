package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a normalized telephone that is already used
 * by another owner. Handled globally by {@link DuplicateTelephoneExceptionHandler}, which
 * responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone is already used by another owner: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
