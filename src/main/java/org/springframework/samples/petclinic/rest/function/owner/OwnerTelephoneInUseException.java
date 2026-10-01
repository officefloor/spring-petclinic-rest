package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerTelephoneUnique} when a create request carries a normalized telephone
 * that is already used by another owner. Handled globally by
 * {@code OwnerTelephoneInUseExceptionHandler}, which responds 409 Conflict.
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
