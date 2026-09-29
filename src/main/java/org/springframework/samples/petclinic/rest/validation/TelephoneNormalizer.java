/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Map;

/**
 * Utility for normalizing telephone numbers into
 * <a href="https://en.wikipedia.org/wiki/E.164">E.164</a> form.
 *
 * <p>Spaces, dashes and brackets are stripped. When the value already carries a
 * leading {@code '+'} the country code is kept as supplied; otherwise the country
 * code {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the
 * national digits. The result must contain between 8 and 15 digits after the
 * {@code '+'}; anything else raises an {@link InvalidTelephoneException} so the
 * request is rejected with {@code 400 Bad Request}. For example {@code '0412 345 678'}
 * becomes {@code '+61412345678'}.
 *
 * <p>In addition, for recognized country codes the national number (the digits after
 * the country code) must be exactly the length that country uses: {@code '+61'}
 * (Australia) requires 9 national digits and {@code '+1'} (NANP) requires 10. A number
 * whose national-number length does not match its country code is rejected.
 */
public final class TelephoneNormalizer {

    private static final String DEFAULT_COUNTRY_CODE = "61";

    private static final int MIN_DIGITS = 8;

    private static final int MAX_DIGITS = 15;

    /**
     * Known country calling codes mapped to the exact number of national digits that
     * must follow them in a valid E.164 number. Codes not listed here fall back to the
     * generic {@link #MIN_DIGITS}-to-{@link #MAX_DIGITS} bound only.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS_BY_COUNTRY_CODE = Map.of(
        "61", 9,
        "1", 10);

    private TelephoneNormalizer() {
    }

    /**
     * Normalize the supplied telephone into E.164 form.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the normalized E.164 telephone (a leading {@code '+'} followed by digits)
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public static String normalize(String telephone) {
        String e164 = toE164(telephone);
        if (e164 == null) {
            throw new InvalidTelephoneException(
                "telephone must be a valid E.164 number (8 to 15 digits after the '+', "
                    + "with a national-number length matching its country code: "
                    + "'+61' requires 9 national digits, '+1' requires 10)");
        }
        return e164;
    }

    /**
     * Best-effort conversion of the supplied telephone into E.164 form, returning
     * {@code null} instead of throwing when the value cannot form a valid E.164 number.
     * Useful for comparing telephones that may already be stored in a non-normalized form.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the E.164 telephone, or {@code null} if it cannot be formed
     */
    public static String toE164(String telephone) {
        if (telephone == null) {
            return null;
        }
        String cleaned = telephone.replaceAll("[\\s\\-()\\[\\]]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        }
        else {
            String national = cleaned;
            if (national.startsWith("0")) {
                national = national.substring(1);
            }
            digits = DEFAULT_COUNTRY_CODE + national;
        }
        if (!digits.matches("\\d+")) {
            return null;
        }
        if (digits.length() < MIN_DIGITS || digits.length() > MAX_DIGITS) {
            return null;
        }
        if (!hasValidNationalLength(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Canonicalize the supplied telephone for equality comparison: its {@link #toE164 E.164} form
     * when it can be normalized, otherwise the raw value unchanged, or an empty string when
     * {@code null}. Unlike {@link #normalize} this never throws, so callers that compare telephones
     * (identity keys, soft-duplicate matching, derived customer codes) treat non-normalizable values
     * consistently rather than rejecting them.
     *
     * @param telephone the raw telephone value (may be {@code null})
     * @return the E.164 form, else the raw value, else an empty string
     */
    public static String toComparable(String telephone) {
        String e164 = toE164(telephone);
        if (e164 != null) {
            return e164;
        }
        return telephone == null ? "" : telephone;
    }

    /**
     * Check the national-number length of the supplied all-digit E.164 body against its
     * country code. The longest matching known country code wins; when the code is
     * recognized the remaining (national) digits must be exactly the required length.
     * Numbers whose country code is not recognized pass this check (only the generic
     * digit-count bound applies to them).
     *
     * @param digits the E.164 digits (country code plus national number, no {@code '+'})
     * @return {@code true} if the national-number length is acceptable for the country code
     */
    private static boolean hasValidNationalLength(String digits) {
        String matchedCode = null;
        for (String code : NATIONAL_DIGITS_BY_COUNTRY_CODE.keySet()) {
            if (digits.startsWith(code)
                && (matchedCode == null || code.length() > matchedCode.length())) {
                matchedCode = code;
            }
        }
        if (matchedCode == null) {
            return true;
        }
        int nationalLength = digits.length() - matchedCode.length();
        return nationalLength == NATIONAL_DIGITS_BY_COUNTRY_CODE.get(matchedCode);
    }
}
