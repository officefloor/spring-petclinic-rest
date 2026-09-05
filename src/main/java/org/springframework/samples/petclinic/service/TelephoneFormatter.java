package org.springframework.samples.petclinic.service;

import java.util.List;

/**
 * Formats a stored E.164 telephone for humans: the country code, a space, then the
 * national digits grouped in threes (e.g. '+61412345678' -> '+61 412 345 678').
 */
public final class TelephoneFormatter {

    private TelephoneFormatter() {
    }

    /** Known country codes, longest first, so the national number can be split off. */
    private static final List<String> COUNTRY_CODES = List.of("61", "1");

    public static String display(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        String code = COUNTRY_CODES.stream().filter(digits::startsWith).findFirst()
            .orElse(digits.substring(0, 1));
        String national = digits.substring(code.length()).replaceAll("(\\d{3})(?=\\d)", "$1 ");
        return "+" + code + " " + national;
    }
}
