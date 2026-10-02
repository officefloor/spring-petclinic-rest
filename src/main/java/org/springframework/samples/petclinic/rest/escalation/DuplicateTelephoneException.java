package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when a request to create an owner carries an E.164 telephone that already belongs to
 * another owner. Carries the offending (E.164) telephone so
 * {@link DuplicateTelephoneExceptionHandler} can report it. Checked so it appears in the function's
 * {@code throws} clause and is routed as an escalation (a 409 Conflict).
 */
public class DuplicateTelephoneException extends Exception {

    private final String telephone;

    public DuplicateTelephoneException(String telephone) {
        super("An owner with telephone '" + telephone + "' already exists");
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
