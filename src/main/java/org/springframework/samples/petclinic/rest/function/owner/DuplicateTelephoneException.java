package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link EnsureUniqueTelephone} when a create request's normalized telephone is
 * already used by an existing owner. Carries the normalized value for the handler's
 * message. Checked so it appears in the function's {@code throws} clause and routes to an
 * escalation.
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
