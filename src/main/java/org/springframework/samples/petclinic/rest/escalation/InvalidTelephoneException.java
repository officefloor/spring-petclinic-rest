package org.springframework.samples.petclinic.rest.escalation;

/**
 * Raised when a create-owner request carries a telephone that cannot form a valid E.164
 * number (a '+' followed by 8 to 15 digits) once normalized. Carries the E.164 candidate
 * that was produced so the handler can report what was actually seen.
 */
public class InvalidTelephoneException extends Exception {

    private final String normalized;

    public InvalidTelephoneException(String normalized) {
        super("Telephone must form a valid E.164 number with 8 to 15 digits after the '+', but was: '"
                + normalized + "'");
        this.normalized = normalized;
    }

    public String getNormalized() {
        return normalized;
    }
}
