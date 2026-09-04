package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats an owner's stored E.164 {@code telephone} for humans: the country code, a space, then the
 * national digits grouped in threes, e.g. {@code '+61412345678'} becomes {@code '+61 412 345 678'}.
 * The raw {@code telephone} is unchanged; this drives the derived {@code telephoneDisplay}.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(Owner owner) {
        String digits = owner.getTelephone().replaceAll("\\D", "");
        String country = digits.startsWith("61") ? "61" : digits.startsWith("1") ? "1" : "";
        String national = digits.substring(country.length());
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + country + " " + grouped;
    }
}
