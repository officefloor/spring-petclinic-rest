package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a create-owner request carries a telephone that
 * cannot be normalized into valid E.164 form (8 to 15 digits after the leading {@code '+'}, once
 * spaces, dashes and brackets are stripped). Handled by {@link InvalidTelephoneExceptionHandler},
 * which responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must form valid E.164 (8 to 15 digits after the '+'): " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
