package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeOwnerTelephone} when an owner create request carries a telephone
 * that is not exactly ten digits once every non-digit character has been stripped. Checked so
 * it appears in the {@code throws} clause and is routed to its escalation handler, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must contain exactly 10 digits: " + telephone);
    }
}
