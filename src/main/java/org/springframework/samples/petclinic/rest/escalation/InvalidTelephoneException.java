package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown on create when the supplied telephone does not contain exactly ten digits once every
 * non-digit character is stripped. Handled by {@link InvalidTelephoneExceptionHandler}, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
