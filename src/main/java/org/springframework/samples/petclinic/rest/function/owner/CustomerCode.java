package org.springframework.samples.petclinic.rest.function.owner;

import java.util.Locale;

/**
 * Formats an owner's customer code as {@code <CITY3>-<LAST3>-<NNNN>}, where {@code CITY3}
 * is the upper-cased first three letters of the city, {@code LAST3} the upper-cased first
 * three letters of the last name and {@code NNNN} a 4-digit zero-padded sequence number
 * (e.g. {@code LON-SMI-0007}).
 */
final class CustomerCode {

    private CustomerCode() {
    }

    static String format(String city, String lastName, int sequence) {
        return String.format("%s-%s-%04d", prefix(city), prefix(lastName), sequence);
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }
}
