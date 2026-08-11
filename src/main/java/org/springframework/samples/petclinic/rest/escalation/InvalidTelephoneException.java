package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request's telephone cannot be normalised to E.164 form (non-digit
 * content after stripping separators, or not 8 to 15 digits after the {@code '+'}). Handled by
 * {@link InvalidTelephoneExceptionHandler}, which responds 400 with the offending value.
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
