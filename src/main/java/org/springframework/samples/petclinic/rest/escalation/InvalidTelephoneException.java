package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request's telephone cannot be converted to a valid E.164 number
 * (a '+' followed by 8 to 15 digits). Handled by {@link InvalidTelephoneExceptionHandler},
 * which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone could not be converted to a valid E.164 number: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
