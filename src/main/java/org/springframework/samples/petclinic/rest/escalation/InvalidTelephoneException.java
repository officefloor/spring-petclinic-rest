package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request carries a telephone that does not reduce to
 * exactly ten digits once every non-digit character is stripped. Carries the
 * normalized (digits-only) value so the handler can report what was actually seen.
 */
public class InvalidTelephoneException extends Exception {

    private final String normalized;

    public InvalidTelephoneException(String normalized) {
        super("Telephone must contain exactly 10 digits after stripping non-digit characters, but had "
                + normalized.length() + ": '" + normalized + "'");
        this.normalized = normalized;
    }

    public String getNormalized() {
        return normalized;
    }
}
