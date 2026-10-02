package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner carries a telephone that cannot be normalized to valid
 * E.164 (a '+' followed by 8 to 15 digits). Carries the value as submitted so
 * {@link InvalidTelephoneExceptionHandler} can report what was actually seen. Checked so it appears in
 * the function's {@code throws} clause and is routed as an escalation.
 */
public class InvalidTelephoneException extends Exception {

    private final String telephone;

    public InvalidTelephoneException(String telephone) {
        super("Telephone could not be normalized to valid E.164 (+ then 8 to 15 digits): '"
                + telephone + "'");
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
