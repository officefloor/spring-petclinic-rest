package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeOwnerTelephone} when a create request's telephone cannot be
 * converted to valid E.164 form: non-digits remain, the digit count is outside 8 to 15, or
 * the national number is the wrong length for its country code (see {@link Telephones}).
 * Carries the rejected (raw) value for the handler's message. Checked so it appears in the
 * function's {@code throws} clause and routes to an escalation.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must form a valid E.164 number, but was: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
