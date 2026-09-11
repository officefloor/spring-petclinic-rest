package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create request supplies a normalized telephone that is already used by
 * another owner. Handled by {@link OwnerTelephoneInUseExceptionHandler}, which responds
 * 409 Conflict.
 */
public class OwnerTelephoneInUseException extends Exception {

    private final String telephone;

    public OwnerTelephoneInUseException(String telephone) {
        super("Telephone already in use: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }
}
