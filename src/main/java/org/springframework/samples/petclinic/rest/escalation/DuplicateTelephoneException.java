package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request carries a normalized telephone that is already used
 * by another owner. Carries the normalized (digits-only) value so the handler can report
 * what collided.
 */
public class DuplicateTelephoneException extends Exception {

    private final String normalized;

    public DuplicateTelephoneException(String normalized) {
        super("Telephone is already used by another owner: '" + normalized + "'");
        this.normalized = normalized;
    }

    public String getNormalized() {
        return normalized;
    }
}
