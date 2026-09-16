package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Shared formatting for an owner's customer code: {@code <CITY3>-<LAST3>-<NNNN>}, where
 * CITY3 is the upper-cased first three letters of the city, LAST3 the upper-cased first
 * three of the last name and NNNN a 4-digit zero-padded per-city sequence
 * (e.g. {@code LON-SMI-0007}).
 */
final class OwnerCustomerCodes {

    private OwnerCustomerCodes() {
    }

    /** Format {@code city}, {@code lastName} and {@code sequence} as {@code <CITY3>-<LAST3>-<NNNN>}. */
    static String format(String city, String lastName, int sequence) {
        return prefix(city) + "-" + prefix(lastName) + "-" + String.format("%04d", sequence);
    }

    /** The upper-cased first three letters of {@code value}, ignoring any non-letters. */
    private static String prefix(String value) {
        StringBuilder letters = new StringBuilder(3);
        for (int i = 0; value != null && i < value.length() && letters.length() < 3; i++) {
            char c = value.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(c);
            }
        }
        return letters.toString().toUpperCase(Locale.ROOT);
    }
}
