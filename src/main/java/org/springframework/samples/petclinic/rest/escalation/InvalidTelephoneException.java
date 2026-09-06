package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a create-owner request supplies a telephone that cannot be normalized to
 * the stored form. The normalization rule lives in {@code TelephoneNormalizer}; the
 * user-facing explanation lives in {@link InvalidTelephoneExceptionHandler}. Handled as
 * a 400.
 */
public class InvalidTelephoneException extends Exception {

    public InvalidTelephoneException(String telephone) {
        super("Invalid telephone number: " + telephone);
    }
}
