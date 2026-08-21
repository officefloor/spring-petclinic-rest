package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create/update-owner request includes a {@code telephone} that cannot be converted
 * to a valid E.164 number. Handled globally by {@link InvalidOwnerTelephoneExceptionHandler}, which
 * responds 400.
 */
public class InvalidOwnerTelephoneException extends Exception {

    private final String telephone;

    public InvalidOwnerTelephoneException(String telephone) {
        super("Invalid telephone: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
