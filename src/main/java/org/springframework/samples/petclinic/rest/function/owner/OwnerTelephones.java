package org.springframework.samples.petclinic.rest.function.owner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.samples.petclinic.rest.escalation.InvalidTelephoneException;

/**
 * The single normalization rule for owner telephones: convert a raw telephone to E.164
 * form. Spaces, dashes and brackets are stripped; a leading {@code +} with its country
 * code is kept, otherwise country code {@code +61} is assumed and a single leading
 * {@code 0} is dropped from the national digits. The result must carry 8 to 15 digits
 * after the {@code +}, and — for a recognised country code — exactly the national-number
 * length that country requires ({@code +61} → 9 national digits, {@code +1} → 10). Shared
 * by {@link NormalizeOwnerTelephone} (which stores the E.164 value on the request) so
 * every telephone is stored and compared the same way.
 */
final class OwnerTelephones {

    private static final String DEFAULT_COUNTRY_CODE = "61";
    private static final int MIN_DIGITS = 8;
    private static final int MAX_DIGITS = 15;

    /**
     * Country calling code → required national-number length. Ordered longest code first
     * so {@link #nationalLength} matches the most specific calling code (e.g. {@code 61}
     * before {@code 1}).
     */
    private static final Map<String, Integer> NATIONAL_LENGTHS = new LinkedHashMap<>();

    static {
        NATIONAL_LENGTHS.put("61", 9);
        NATIONAL_LENGTHS.put("1", 10);
    }

    private OwnerTelephones() {
    }

    /**
     * The E.164 form of {@code telephone} (a leading {@code +} followed by 8 to 15 digits).
     *
     * @throws InvalidTelephoneException when the value cannot form a valid E.164 number.
     */
    static String toE164(String telephone) throws InvalidTelephoneException {
        String cleaned = (telephone == null ? "" : telephone).replaceAll("[\\s()-]", "");

        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            digits = dropSingleLeadingZero(cleaned);
            digits = DEFAULT_COUNTRY_CODE + digits;
        }

        if (!digits.matches("\\d+") || digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS) {
            throw new InvalidTelephoneException(
                    "Telephone must form a valid E.164 number with " + MIN_DIGITS + " to " + MAX_DIGITS
                            + " digits after the country code");
        }
        requireCountryNationalLength(digits);
        return "+" + digits;
    }

    /**
     * For a recognised country calling code, rejects a number whose national part is not the
     * exact length that country requires. Unrecognised codes keep the generic length check only.
     */
    private static void requireCountryNationalLength(String digits) throws InvalidTelephoneException {
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTHS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code)) {
                int nationalLength = digits.length() - code.length();
                int required = country.getValue();
                if (nationalLength != required) {
                    throw new InvalidTelephoneException("Telephone with country code +" + code
                            + " must have " + required + " national digits, but has " + nationalLength);
                }
                return;
            }
        }
    }

    private static String dropSingleLeadingZero(String digits) {
        return digits.startsWith("0") ? digits.substring(1) : digits;
    }
}
