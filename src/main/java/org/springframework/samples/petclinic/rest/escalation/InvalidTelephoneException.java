package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a create-owner request carries a telephone that
 * cannot form a valid E.164 number (8 to 15 digits after the '+', once spaces, dashes and brackets are
 * stripped and the country code is resolved). Handled globally by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot form a valid E.164 number (8 to 15 digits after the '+'): " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
