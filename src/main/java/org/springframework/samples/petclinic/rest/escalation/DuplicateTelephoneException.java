package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RequireUniqueTelephone} when a create-owner request carries a normalized telephone
 * that is already used by another owner. Carries the offending (normalized) value so
 * {@link DuplicateTelephoneExceptionHandler} can report a 409.
 */
public class DuplicateTelephoneException extends Exception {

    private final String normalized;

    public DuplicateTelephoneException(String normalized) {
        super("Telephone is already used by another owner: '" + normalized + "'");
        this.normalized = normalized;
    }

    public String getNormalized() {
        return this.normalized;
    }
}
