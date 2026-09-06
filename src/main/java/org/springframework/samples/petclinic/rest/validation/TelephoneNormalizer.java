package org.springframework.samples.petclinic.rest.validation;

import java.util.List;
import java.util.Map;

import org.springframework.samples.petclinic.rest.advice.InvalidOwnerFieldsException;
import org.springframework.stereotype.Component;

/**
 * Normalizes owner telephone numbers into the single canonical E.164 form that is stored, returned
 * and compared for duplicates. Keeping this logic in one place lets the create endpoint treat a
 * telephone as an opaque, already-canonical value: it {@link #normalize(String) normalizes} an
 * incoming number once, then relies on {@link #canonicalize(String)} to compare it against
 * existing owners regardless of how those numbers were originally formatted.
 */
@Component
public class TelephoneNormalizer {

    /** Assumed country code (with '+') for numbers submitted without an explicit country code. */
    private static final String DEFAULT_COUNTRY_CODE = "+61";

    /** Minimum number of digits after the leading '+' for a valid E.164 number. */
    private static final int MIN_DIGITS = 8;

    /** Maximum number of digits after the leading '+' for a valid E.164 number. */
    private static final int MAX_DIGITS = 15;

    /**
     * Required number of national-significant digits (the digits after the country code) for each
     * recognised country calling code. The Australian code {@code +61} carries 9 national digits and
     * the North American {@code +1} carries 10; a number whose country code appears here but whose
     * national portion has the wrong length is rejected. Codes not listed only face the generic
     * {@code MIN_DIGITS}/{@code MAX_DIGITS} bound.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS_BY_COUNTRY_CODE = Map.of(
        "61", 9,
        "1", 10);

    /**
     * Normalizes a telephone number submitted to the create endpoint into E.164 form: a leading
     * '+' and country code are kept when present, otherwise the country code {@code +61} is assumed
     * and a single leading '0' is dropped from the national digits; spaces, dashes and brackets are
     * stripped. The result must carry 8 to 15 digits after the '+'; additionally, for a recognised
     * country calling code the national-significant part must have exactly the length that code
     * requires ({@code +61} => 9 national digits, {@code +1} => 10). A value that fails any of these
     * rules is rejected as a 400 (via {@link InvalidOwnerFieldsException}, which the
     * {@code ExceptionControllerAdvice} renders with {@code telephone} in its {@code errors} array).
     * For example {@code "0412 345 678"} becomes {@code "+61412345678"}.
     *
     * @param telephone the raw telephone value from the request
     * @return the canonical E.164 telephone to store and return
     * @throws InvalidOwnerFieldsException if the value cannot form a valid E.164 number
     */
    public String normalize(String telephone) {
        String e164 = canonicalize(telephone);
        int digitCount = e164.length() - 1;
        if (!e164.startsWith("+") || digitCount < MIN_DIGITS || digitCount > MAX_DIGITS
                || !hasValidNationalLength(e164)) {
            throw new InvalidOwnerFieldsException(List.of("telephone"));
        }
        return e164;
    }

    /**
     * Checks the national-significant length of a canonical E.164 number against the requirement for
     * its country calling code. Numbers whose code is not one of the recognised codes pass this check
     * (they are only bound by the generic digit-count range).
     *
     * @param e164 a canonical E.164 number (leading '+' followed by the country code and national digits)
     * @return {@code true} if the national-number length matches the country code's requirement, or the
     *         country code is not recognised
     */
    private boolean hasValidNationalLength(String e164) {
        String digits = e164.substring(1);
        for (Map.Entry<String, Integer> entry : NATIONAL_DIGITS_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = entry.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalLength = digits.length() - countryCode.length();
                return nationalLength == entry.getValue();
            }
        }
        return true;
    }

    /**
     * Reduces an arbitrary telephone (possibly {@code null} or differently formatted) to the
     * canonical E.164 comparison form, so numbers that differ only in formatting still collide.
     * A leading '+' and country code are kept when present; otherwise the country code {@code +61}
     * is assumed and a single leading '0' is dropped from the national digits. Spaces, dashes,
     * brackets and any other non-digit characters are stripped. Unlike {@link #normalize(String)}
     * this never rejects its input (it does not enforce the digit-count rule); it is applied to
     * existing owners' stored telephones during duplicate detection.
     *
     * @param telephone a stored or submitted telephone, or {@code null}
     * @return the telephone reduced to its canonical E.164 comparison form
     */
    public String canonicalize(String telephone) {
        if (telephone == null) {
            return "";
        }
        boolean hasCountryCode = telephone.trim().startsWith("+");
        String digits = telephone.replaceAll("\\D", "");
        if (!hasCountryCode) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = DEFAULT_COUNTRY_CODE.substring(1) + digits;
        }
        return "+" + digits;
    }
}
