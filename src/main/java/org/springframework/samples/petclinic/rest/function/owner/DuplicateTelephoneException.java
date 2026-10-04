package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link CheckOwnerTelephoneUnique} when an owner create request's normalised
 * telephone is already in use by an existing owner. Checked so it appears in the
 * {@code throws} clause and is routed to its escalation handler, which responds 409.
 */
public class DuplicateTelephoneException extends Exception {

    public DuplicateTelephoneException(String telephone) {
        super("Telephone already in use: " + telephone);
    }
}
