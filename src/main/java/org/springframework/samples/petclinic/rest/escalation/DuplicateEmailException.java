package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by {@code RequireUniqueEmail} when a create-owner request carries an email whose lower-cased
 * form is already used by another owner. Carries the offending (normalized) value so
 * {@link DuplicateEmailExceptionHandler} can report a 409.
 */
public class DuplicateEmailException extends Exception {

    private final String normalized;

    public DuplicateEmailException(String normalized) {
        super("Email is already used by another owner: '" + normalized + "'");
        this.normalized = normalized;
    }

    public String getNormalized() {
        return this.normalized;
    }
}
