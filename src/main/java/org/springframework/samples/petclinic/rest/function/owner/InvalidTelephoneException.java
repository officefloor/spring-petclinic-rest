package org.springframework.samples.petclinic.rest.function.owner;

/**
 * Thrown by {@link NormalizeOwnerTelephone} when an owner create request carries a telephone
 * that cannot be canonicalised to E.164 form (a {@code '+'} followed by 8 to 15 digits). Checked
 * so it appears in the {@code throws} clause and is routed to its escalation handler, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Telephone must be a valid E.164 number (8 to 15 digits): " + telephone);
    }
}
