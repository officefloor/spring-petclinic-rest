package org.springframework.samples.petclinic.util;

/**
 * Thrown when an owner request supplies no usable postal address in either accepted form, i.e.
 * it has neither a non-blank structured {@code addressLine1} nor a non-blank flat {@code address}.
 * Handled by the REST layer as a 400 Bad Request.
 */
public class InvalidAddressException extends RuntimeException {

    public InvalidAddressException() {
        super("An address must be supplied, either as 'addressLine1' or the flat 'address'");
    }
}
