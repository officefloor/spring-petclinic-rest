package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a telephone that is not exactly 10 digits
 * once every non-digit character has been stripped. Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must be exactly 10 digits after removing non-digit characters: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
