package org.springframework.samples.petclinic.rest.function.owner;

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
 * The result must be a {@code '+'} followed by 8 to 15 digits; anything else (non-digit leftovers, or a
 * digit count outside that range) is not valid E.164 and yields {@code null}.
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
        return E164.matcher(e164).matches() ? e164 : null;
    }
}
