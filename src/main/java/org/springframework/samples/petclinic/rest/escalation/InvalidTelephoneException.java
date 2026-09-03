package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request carries a telephone that cannot be normalized to E.164 form (a '+'
 * followed by 8 to 15 digits). Handled globally by {@link InvalidTelephoneExceptionHandler}, which
 * responds 400.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone cannot be normalized to E.164 form (a '+' followed by 8 to 15 digits): " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
