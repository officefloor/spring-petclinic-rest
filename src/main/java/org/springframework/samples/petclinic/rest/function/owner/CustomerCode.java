package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Formats an owner's customer code as {@code <LAST3>-<NNNN>}, where {@code LAST3} is the
 * upper-cased first three letters of the last name and {@code NNNN} is a 4-digit
 * zero-padded sequence number (e.g. {@code SMI-0007}).
 */
final class CustomerCode {

    private CustomerCode() {
    }

    static String format(String lastName, int sequence) {
        String prefix = lastName.substring(0, Math.min(3, lastName.length())).toUpperCase(Locale.ROOT);
        return String.format("%s-%04d", prefix, sequence);
    }
}
