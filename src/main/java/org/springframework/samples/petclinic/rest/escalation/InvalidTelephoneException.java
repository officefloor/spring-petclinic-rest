package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone cannot be normalised to E.164 form (non-digit
 * content after stripping separators, not 8 to 15 digits after the {@code '+'}, or the wrong
 * national-number length for a known country code &mdash; {@code +61} needs 9 national digits,
 * {@code +1} needs 10). Handled by {@link InvalidTelephoneExceptionHandler}, which responds 400
 * with the offending value.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot be normalised to E.164 form: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
