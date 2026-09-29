package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code NormalizeOwnerTelephone} when a create-owner request carries a telephone that cannot
 * be converted to valid E.164 form (8 to 15 digits after the '+'). Carries the offending input value so
 * {@link InvalidTelephoneExceptionHandler} can report a 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot be converted to valid E.164 form, but was: '" + telephone + "'");
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
