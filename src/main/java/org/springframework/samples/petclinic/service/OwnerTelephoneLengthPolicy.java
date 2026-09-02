package org.springframework.samples.petclinic.service;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Business rule: the national-number length of an owner's E.164 telephone must match its
 * country code ('+61' requires 9 national digits, '+1' requires 10). Kept as a small,
 * self-contained unit so the rule can be enforced during telephone normalization without
 * adding complexity to the mapper or the owner model.
 */
public final class OwnerTelephoneLengthPolicy {

    private OwnerTelephoneLengthPolicy() {
    }

    /**
     * Reject {@code digits} (country code plus national number, without the leading '+') when
     * its national length is wrong for the country code: '+61' requires 9 national digits and
     * '+1' requires 10. Any other country code is accepted.
     *
     * @param digits the E.164 digits without the leading '+'
     * @throws InvalidNationalNumberLengthException if the national length is wrong for the country
     */
    public static void requireValidNationalLength(String digits) {
        if (digits.startsWith("61") && digits.length() != 2 + 9) {
            throw new InvalidNationalNumberLengthException();
        }
        if (digits.startsWith("1") && digits.length() != 1 + 10) {
            throw new InvalidNationalNumberLengthException();
        }
    }

    /** Thrown when an owner's national-number length does not match its E.164 country code. */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public static class InvalidNationalNumberLengthException extends RuntimeException {
    }
}
