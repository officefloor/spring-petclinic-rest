package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone is not exactly 10 digits after every non-digit
 * character has been stripped. Handled by {@link InvalidTelephoneExceptionHandler}, which responds
 * 400 with the offending value.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must be exactly 10 digits after removing non-digit characters: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
