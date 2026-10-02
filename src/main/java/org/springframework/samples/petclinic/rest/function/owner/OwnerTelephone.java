package org.springframework.samples.petclinic.rest.function.owner;

import java.util.regex.Pattern;

import org.springframework.samples.petclinic.model.TelephoneFormatter;

/**
 * Single source of truth for what a "normalized" owner telephone is: the E.164 form.
 * {@link ValidateNewOwner} enforces the rule and stores the E.164 value through here, so the stored and
 * returned telephone (and the owner's derived identity key, which includes it) share one canonical form.
 *
 * <p>Normalization strips spaces, dashes and brackets, then forms the canonical E.164 string:
 * <ul>
 *   <li>a leading {@code '+'} and country code are kept as-is;</li>
 *   <li>otherwise country code {@code +61} is assumed and a single leading {@code '0'} is dropped from
 *       the national digits.</li>
 * </ul>
 * The result must be a {@code '+'} followed by 8 to 15 digits. On top of that general rule, the
 * national-number length (the digits after the country code) must match the country code when it is one
 * we recognize: {@code +61} requires 9 national digits and {@code +1} requires 10 (see
 * {@link TelephoneFormatter#hasValidNationalLength(String)}). Anything else — non-digit leftovers, a
 * total digit count outside 8 to 15,
 * or a national-number length that is wrong for a recognized country code — is not valid E.164 and
 * yields {@code null}. A country code that is not in the table is accepted on the general rule alone.
 */
final class OwnerTelephone {

    /** A valid E.164 value: '+' then 8 to 15 digits. */
    private static final Pattern E164 = Pattern.compile("^\\+[0-9]{8,15}$");

    /** Characters removed before interpreting the number: spaces, dashes and brackets. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()]");

    private OwnerTelephone() {
    }

    /**
     * Returns the E.164 form of {@code telephone}, or {@code null} when it cannot form valid E.164.
     */
    static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = SEPARATORS.matcher(telephone).replaceAll("");
        String digits;
        if (cleaned.startsWith("+")) {
            // Keep the explicit country code; the '+' is re-added below.
            digits = cleaned.substring(1);
        }
        else {
            // No country code: assume +61 and drop a single leading '0' from the national digits.
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        String e164 = "+" + digits;
        if (!E164.matcher(e164).matches()) {
            return null;
        }
        return TelephoneFormatter.hasValidNationalLength(digits) ? e164 : null;
    }
}
