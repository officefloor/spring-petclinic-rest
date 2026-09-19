package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown when an owner request's telephone is a well-formed E.164 number whose
 * national-number length is wrong for its country code (a '+61' number must carry 9
 * national digits, a '+1' number 10). Handled by
 * {@link TelephoneNationalNumberLengthExceptionHandler}, which responds 400.
 */
public class TelephoneNationalNumberLengthException extends Exception {

    private final String telephone;

    public TelephoneNationalNumberLengthException(String telephone) {
        super("Telephone national-number length is wrong for its country code: " + telephone);
        this.telephone = telephone;
    }

    public String getTelephone() {
        return this.telephone;
    }
}
