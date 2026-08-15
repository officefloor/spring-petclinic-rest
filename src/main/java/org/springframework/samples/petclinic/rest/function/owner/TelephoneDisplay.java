package org.springframework.samples.petclinic.rest.function.owner;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats a stored E.164 telephone number for human display: the country code, a space, then the
 * national digits grouped in threes ('+61412345678' becomes '+61 412 345 678'). The raw
 * {@code telephone} field keeps its E.164 form; this is the derived {@code telephoneDisplay}.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /** The display form of an already-stored owner's telephone. */
    public static String forOwner(Owner owner) {
        return format(owner.getTelephone());
    }

    /**
     * Formats an E.164 number ('+' followed by digits) as the country code, a space, then the
     * national digits grouped in threes. Anything that is not a well-formed E.164 string is
     * returned unchanged.
     */
    public static String format(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        if (digits.isEmpty() || !digits.chars().allMatch(c -> c >= '0' && c <= '9')) {
            return e164;
        }
        int ccLength = countryCodeLength(digits);
        StringBuilder display = new StringBuilder("+").append(digits, 0, ccLength);
        for (int i = ccLength; i < digits.length(); i += 3) {
            display.append(' ').append(digits, i, Math.min(i + 3, digits.length()));
        }
        return display.toString();
    }

    /**
     * The E.164 country-code length within the given digit string: 1 for the NANP ('+1'),
     * otherwise 2 (covers '+61' and its neighbours), mirroring {@link TelephoneE164}.
     */
    private static int countryCodeLength(String digits) {
        if (digits.startsWith("1")) {
            return 1;
        }
        return 2;
    }
}
