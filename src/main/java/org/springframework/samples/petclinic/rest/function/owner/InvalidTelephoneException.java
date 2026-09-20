package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeOwnerTelephone} when a create request's telephone does not
 * contain exactly ten digits once every non-digit character is stripped. Carries the
 * rejected (raw) value for the handler's message. Checked so it appears in the function's
 * {@code throws} clause and routes to an escalation.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits, but was: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
