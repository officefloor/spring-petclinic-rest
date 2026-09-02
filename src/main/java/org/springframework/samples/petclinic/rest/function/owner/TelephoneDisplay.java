package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats the owner's stored E.164 telephone for humans: the country code, a space, then the
 * national digits grouped in threes (e.g. {@code +61412345678} -> {@code +61 412 345 678}).
 * Country codes match {@link E164Telephone} ('+61' is two digits, otherwise one). Returns the raw
 * value unchanged when it is null or not in '+' E.164 form.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(Owner owner) {
        String e164 = owner.getTelephone();
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        int countryCodeLength = digits.startsWith("61") ? 2 : 1;
        String national = digits.substring(countryCodeLength);
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(national.charAt(i));
        }
        return "+" + digits.substring(0, countryCodeLength) + " " + grouped;
    }
}
