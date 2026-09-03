package org.springframework.samples.petclinic.rest.controller.v1;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Formats a stored E.164 telephone for humans: the country code, a space, then the national
 * digits grouped in threes (e.g. {@code +61412345678} -> {@code +61 412 345 678}).
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    /** The human-formatted display of {@code owner}'s E.164 telephone. */
    public static String of(Owner owner) {
        return format(owner.getTelephone());
    }

    /** {@code e164} with its national digits grouped in threes; returned unchanged when not E.164. */
    static String format(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        int cc = e164.startsWith("+1") ? 2 : 3; // '+1' has a 1-digit country code, others 2 digits
        String national = e164.substring(cc);
        StringBuilder out = new StringBuilder(e164.substring(0, cc));
        for (int i = 0; i < national.length(); i += 3) {
            out.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return out.toString();
    }
}
