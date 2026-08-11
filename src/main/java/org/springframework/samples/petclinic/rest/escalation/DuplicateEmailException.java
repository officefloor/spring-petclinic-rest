package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request carries an email that, lower-cased, is already used
 * by another owner. Carries the normalized (lower-cased) value so the handler can report
 * what collided.
 */
public class DuplicateEmailException extends Exception {

    private final String normalized;

    public DuplicateEmailException(String normalized) {
        super("Email is already used by another owner: '" + normalized + "'");
        this.normalized = normalized;
    }

    public String getNormalized() {
        return normalized;
    }
}
