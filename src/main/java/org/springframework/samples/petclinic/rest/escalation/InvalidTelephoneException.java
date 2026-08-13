package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the supplied telephone cannot be normalized into a valid E.164 number
 * (8 to 15 digits after the '+'). Handled by {@link InvalidTelephoneExceptionHandler}, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must be a valid E.164 number (8 to 15 digits): " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
