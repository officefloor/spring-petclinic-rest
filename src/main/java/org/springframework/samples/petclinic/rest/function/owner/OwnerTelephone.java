package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Single source of truth for what a "normalized" owner telephone is: the E.164 form. Both
 * {@link ValidateNewOwner} (which enforces the rule and stores the E.164 value) and
 * {@link CheckTelephoneUnique} (which compares against stored owners) normalize through here, so the
 * two cannot drift apart.
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
 * {@link #NATIONAL_LENGTHS}). Anything else — non-digit leftovers, a total digit count outside 8 to 15,
 * or a national-number length that is wrong for a recognized country code — is not valid E.164 and
 * yields {@code null}. A country code that is not in the table is accepted on the general rule alone.
 */
final class OwnerTelephone {

    /** A valid E.164 value: '+' then 8 to 15 digits. */
    private static final Pattern E164 = Pattern.compile("^\\+[0-9]{8,15}$");

    /** Characters removed before interpreting the number: spaces, dashes and brackets. */
    private static final Pattern SEPARATORS = Pattern.compile("[\\s\\-()]");

    /**
     * Country code (without the '+') to its required national-number length. Iterated longest-code-first
     * (hence {@link LinkedHashMap}) so a longer code is matched before a shorter one that is a prefix of
     * the number. Country codes absent here are not length-checked.
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS;
    static {
        Map<String, Integer> lengths = new LinkedHashMap<>();
        lengths.put("61", 9); // Australia
        lengths.put("1", 10); // North American Numbering Plan
        NATIONAL_LENGTHS = lengths;
    }

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
        return hasValidNationalLength(digits) ? e164 : null;
    }

    /**
     * Whether the national-number length of {@code digits} (a '+'-less E.164 digit string) matches its
     * country code. Unrecognized country codes pass (checked only by the general 8-to-15 rule above).
     */
    private static boolean hasValidNationalLength(String digits) {
        for (Map.Entry<String, Integer> entry : NATIONAL_LENGTHS.entrySet()) {
            String code = entry.getKey();
            if (digits.startsWith(code)) {
                return digits.length() - code.length() == entry.getValue();
            }
        }
        return true;
    }
}
