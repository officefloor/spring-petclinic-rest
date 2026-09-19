package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone does not contain exactly 10 digits
 * after every non-digit character is stripped. Handled by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits after removing non-digit characters: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
