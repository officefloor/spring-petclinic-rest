package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Shared formatting for an owner's customer code: {@code <LAST3>-<NNNN>}, where LAST3 is
 * the upper-cased first three letters of the last name and NNNN is a 4-digit zero-padded
 * global sequence (e.g. {@code SMI-0007}).
 */
final class OwnerCustomerCodes {

    private OwnerCustomerCodes() {
    }

    /** Format {@code lastName} and {@code sequence} as {@code <LAST3>-<NNNN>}. */
    static String format(String lastName, int sequence) {
        return prefix(lastName) + "-" + String.format("%04d", sequence);
    }

    /** The upper-cased first three letters of {@code lastName}, ignoring any non-letters. */
    private static String prefix(String lastName) {
        StringBuilder letters = new StringBuilder(3);
        for (int i = 0; lastName != null && i < lastName.length() && letters.length() < 3; i++) {
            char c = lastName.charAt(i);
            if (Character.isLetter(c)) {
                letters.append(c);
            }
        }
        return letters.toString().toUpperCase(Locale.ROOT);
    }
}
