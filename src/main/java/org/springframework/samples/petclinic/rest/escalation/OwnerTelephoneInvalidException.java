package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the telephone, after stripping every non-digit
 * character, is not exactly 10 digits.
 *
 * <p>Carries the stripped digit string so {@link OwnerTelephoneInvalidExceptionHandler}
 * can respond 400 explaining why the telephone was rejected.
 */
public class OwnerTelephoneInvalidException extends Exception {

    private final String digits;

    public OwnerTelephoneInvalidException(String digits) {
        super("Telephone must be exactly 10 digits after removing non-digit characters, but had "
                + digits.length() + ": '" + digits + "'");
        this.digits = digits;
    }

    public String getDigits() {
        return digits;
    }
}
