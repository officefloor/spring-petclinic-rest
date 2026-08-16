package org.springframework.samples.petclinic.rest.escalation;

/**
 * Thrown by the create-owner pipeline when the telephone cannot be converted into a valid
 * E.164 number (8 to 15 digits after the '+', once spaces, dashes and brackets are stripped
 * and a national number is defaulted to country code '+61').
 *
 * <p>Carries the offending input so {@link OwnerTelephoneInvalidExceptionHandler}
 * can respond 400 explaining why the telephone was rejected.
 */
public class OwnerTelephoneInvalidException extends Exception {

    private final String telephone;

    public OwnerTelephoneInvalidException(String telephone) {
        super("Telephone '" + telephone + "' cannot be converted to a valid E.164 number "
                + "(requires 8 to 15 digits after the '+')");
        this.telephone = telephone;
    }

    public String getTelephone() {
        return telephone;
    }

    public String getDigits() {
        return telephone;
    }
}
