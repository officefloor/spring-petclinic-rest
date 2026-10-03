/*
 * Copyright 2002-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.samples.petclinic.util;

/**
 * Normalizes free-form telephone numbers into canonical <a
 * href="https://en.wikipedia.org/wiki/E.164">E.164</a> form (a leading {@code '+'} followed by
 * country code and national digits, e.g. {@code '+61412345678'}).
 *
 * <p>The rules, applied in order: strip spaces, dashes and brackets; keep an explicit leading
 * {@code '+'} and country code when present, otherwise assume country code {@code '+61'} and drop a
 * single leading {@code '0'} from the national digits; require 8 to 15 digits after the {@code '+'}.
 * In addition, the national-number length must match the country code for the countries we recognise:
 * {@code '+61'} (Australia) requires exactly 9 national digits and {@code '+1'} (NANP) requires
 * exactly 10. A value that cannot satisfy these rules has no valid E.164 form.
 *
 * <p>This is the single source of truth shared by the inbound {@code TelephoneValidation}
 * constraint (which rejects numbers with no valid E.164 form) and {@code OwnerMapper} (which stores
 * the E.164 string), so a value that validates is exactly one that normalizes.
 */
public final class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Australia ({@code '+61'}): national numbers are exactly 9 digits. */
    private static final int AU_NATIONAL_LENGTH = 9;

    /** NANP country code ({@code '+1'}): US/Canada/etc. */
    private static final String NANP_COUNTRY_CODE = "1";

    /** NANP ({@code '+1'}): national numbers are exactly 10 digits. */
    private static final int NANP_NATIONAL_LENGTH = 10;

    private TelephoneNormalizer() {
    }

    /**
     * Convert a raw telephone number to its E.164 form, or return {@code null} when it has none.
     *
     * @param raw the inbound telephone number (may be {@code null})
     * @return the E.164 string (e.g. {@code '+61412345678'}), or {@code null} if {@code raw} is
     * {@code null} or cannot form a valid E.164 number
     */
    public static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String trimmed = raw.trim();
        boolean hasCountryCode = trimmed.startsWith("+");
        String body = hasCountryCode ? trimmed.substring(1) : trimmed;
        String digits = body.replaceAll("[\\s()\\-]", "");
        if (digits.isEmpty() || !digits.matches("\\d+")) {
            return null;
        }
        String e164Digits;
        if (hasCountryCode) {
            e164Digits = digits;
        } else {
            String national = digits.startsWith("0") ? digits.substring(1) : digits;
            e164Digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (e164Digits.length() < 8 || e164Digits.length() > 15) {
            return null;
        }
        if (!hasValidNationalLength(e164Digits)) {
            return null;
        }
        return "+" + e164Digits;
    }

    /**
     * Format a stored E.164 number for humans: the {@code '+'} and country code, a space, then the
     * national digits grouped in threes separated by spaces, e.g. {@code '+61412345678'} becomes
     * {@code '+61 412 345 678'}. The country code is recognised for the countries we know
     * ({@code '+61'} Australia, {@code '+1'} NANP); any other value is returned unchanged.
     *
     * @param e164 a stored E.164 telephone (e.g. {@code '+61412345678'}), may be {@code null}
     * @return the human-formatted display string, or {@code null} when {@code e164} is {@code null}
     */
    public static String toDisplay(String e164) {
        if (e164 == null) {
            return null;
        }
        if (!e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        if (!digits.matches("\\d+")) {
            return e164;
        }
        String countryCode = countryCodeOf(digits);
        if (countryCode == null) {
            return e164;
        }
        String national = digits.substring(countryCode.length());
        StringBuilder sb = new StringBuilder("+").append(countryCode);
        for (int i = 0; i < national.length(); i += 3) {
            sb.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return sb.toString();
    }

    /**
     * Return the country code of the given E.164 digits (the part after the {@code '+'}) for the
     * countries we recognise ({@code '61'} then {@code '1'}), or {@code null} when none matches.
     */
    private static String countryCodeOf(String e164Digits) {
        if (e164Digits.startsWith(DEFAULT_COUNTRY_CODE)) {
            return DEFAULT_COUNTRY_CODE;
        }
        if (e164Digits.startsWith(NANP_COUNTRY_CODE)) {
            return NANP_COUNTRY_CODE;
        }
        return null;
    }

    /**
     * Check that the national-number length matches the country code for the countries we recognise.
     * {@code '+61'} (Australia) requires 9 national digits and {@code '+1'} (NANP) requires 10; any
     * other country code is left to the generic 8-to-15 digit bound.
     */
    private static boolean hasValidNationalLength(String e164Digits) {
        if (e164Digits.startsWith(DEFAULT_COUNTRY_CODE)) {
            return nationalLength(e164Digits, DEFAULT_COUNTRY_CODE) == AU_NATIONAL_LENGTH;
        }
        if (e164Digits.startsWith(NANP_COUNTRY_CODE)) {
            return nationalLength(e164Digits, NANP_COUNTRY_CODE) == NANP_NATIONAL_LENGTH;
        }
        return true;
    }

    /** National-digit count for {@code e164Digits}, i.e. its length minus the country code. */
    private static int nationalLength(String e164Digits, String countryCode) {
        return e164Digits.length() - countryCode.length();
    }
}
