package org.springframework.samples.petclinic.rest.validation;

/**
 * Thrown when an owner's telephone number is not a valid telephone number.
 */
public class InvalidTelephoneException extends RuntimeException {

    private final String rejectedValue;

    public InvalidTelephoneException(String rejectedValue) {
        super("Telephone number must form a valid E.164 number with " + OwnerTelephone.MIN_DIGITS
            + " to " + OwnerTelephone.MAX_DIGITS + " digits after the '+'");
        this.rejectedValue = rejectedValue;
    }

    public String getRejectedValue() {
        return rejectedValue;
    }
}
