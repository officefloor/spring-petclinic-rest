package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner carries a telephone that, once every non-digit
 * character is stripped, is not exactly ten digits. Carries the normalized (digits-only)
 * value so {@link InvalidTelephoneExceptionHandler} can report what was actually seen.
 * Checked so it appears in the function's {@code throws} clause and is routed as an escalation.
 */
public class InvalidTelephoneException extends Exception {

    private final String normalized;

    public InvalidTelephoneException(String normalized) {
        super("Telephone must be exactly 10 digits after removing non-digit characters, but was "
                + normalized.length() + ": '" + normalized + "'");
        this.normalized = normalized;
    }

    public String getNormalized() {
        return this.normalized;
    }
}
