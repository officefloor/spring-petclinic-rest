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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.samples.petclinic.rest.advice.InvalidTelephoneException;
import org.springframework.stereotype.Component;

/**
 * Turns owner telephone numbers into the single canonical E.164 form that gets stored, returned and compared, so the
 * REST controllers do not have to carry the telephone rules themselves.
 *
 * <p>Canonicalization keeps a leading {@code '+'} and country code when one is supplied; otherwise it assumes the
 * default country code {@code +61} and drops a single leading {@code '0'} from the national digits. Spaces, dashes and
 * brackets (indeed every non-digit character other than the leading {@code '+'}) are stripped. So {@code "0412 345 678"}
 * becomes {@code "+61412345678"}.
 *
 * <p>Two entry points share one notion of "canonical": {@link #normalize(String)} validates a raw value supplied on a
 * request and rejects anything that cannot form a valid E.164 number, while {@link #canonicalize(String)} reduces an
 * already-stored value to the same form for duplicate detection without rejecting it.
 */
@Component
public class TelephoneNormalizer {

    /** Country code assumed for national-format numbers that do not carry an explicit {@code '+'} prefix. */
    private static final String DEFAULT_COUNTRY_CODE = "61";

    /** Minimum number of digits an E.164 number may carry after the leading {@code '+'}. */
    private static final int MIN_E164_DIGITS = 8;

    /** Maximum number of digits an E.164 number may carry after the leading {@code '+'}. */
    private static final int MAX_E164_DIGITS = 15;

    /**
     * Exact number of national digits (digits following the country code) required for each known country code.
     * Ordered longest country code first so the most specific prefix wins when one code is a prefix of another.
     */
    private static final Map<String, Integer> NATIONAL_DIGITS_BY_COUNTRY_CODE = buildNationalDigitsByCountryCode();

    private static Map<String, Integer> buildNationalDigitsByCountryCode() {
        Map<String, Integer> byLongestFirst = new LinkedHashMap<>();
        byLongestFirst.put("61", 9);
        byLongestFirst.put("1", 10);
        return byLongestFirst;
    }

    /**
     * Normalizes a raw request telephone into its canonical stored E.164 form. A leading {@code '+'} and country code
     * are kept when present; otherwise the default country code {@code +61} is assumed and a single leading {@code '0'}
     * is dropped from the national digits. Spaces, dashes and brackets are stripped. The result must carry 8 to 15
     * digits after the {@code '+'}. The normalized E.164 value is what gets stored and returned as {@code telephone}.
     *
     * @param telephone the raw telephone value from the request
     * @return the normalized canonical E.164 telephone
     * @throws InvalidTelephoneException if the value cannot form a valid E.164 number
     */
    public String normalize(String telephone) {
        String e164 = toE164(telephone);
        int digitCount = e164.length() - 1;
        if (digitCount < MIN_E164_DIGITS || digitCount > MAX_E164_DIGITS) {
            throw new InvalidTelephoneException(
                "telephone must contain between " + MIN_E164_DIGITS + " and " + MAX_E164_DIGITS
                    + " digits after conversion to E.164 form");
        }
        validateNationalNumberLength(e164);
        return e164;
    }

    /**
     * Rejects a normalized E.164 number whose national number (the digits following the country code) does not carry
     * the exact length that country requires. For example {@code +61} numbers must have 9 national digits and
     * {@code +1} numbers must have 10. Country codes with no known rule are left to the generic 8-to-15 digit bounds.
     *
     * @param e164 a normalized E.164 telephone that has already passed the generic digit-count bounds
     * @throws InvalidTelephoneException if the national number length is wrong for the recognized country code
     */
    private void validateNationalNumberLength(String e164) {
        String digits = e164.substring(1);
        for (Map.Entry<String, Integer> rule : NATIONAL_DIGITS_BY_COUNTRY_CODE.entrySet()) {
            String countryCode = rule.getKey();
            if (digits.startsWith(countryCode)) {
                int nationalDigits = digits.length() - countryCode.length();
                int requiredDigits = rule.getValue();
                if (nationalDigits != requiredDigits) {
                    throw new InvalidTelephoneException(
                        "telephone with country code +" + countryCode + " must contain exactly " + requiredDigits
                            + " national digits, but had " + nationalDigits);
                }
                return;
            }
        }
    }

    /**
     * Reduces an already-stored telephone to the canonical E.164 form used for duplicate detection. Unlike
     * {@link #normalize(String)} this never rejects its input, so existing owners are compared regardless of how their
     * number was originally formatted.
     *
     * @param telephone an owner's stored telephone, may be {@code null}
     * @return the canonical E.164 form used for comparison
     */
    public String canonicalize(String telephone) {
        return toE164(telephone);
    }

    /**
     * Converts a raw telephone into E.164 form without enforcing the digit-count bounds. When the value carries a
     * leading {@code '+'} its country code is kept; otherwise the default country code is prepended after dropping a
     * single leading {@code '0'} from the national digits.
     */
    private String toE164(String telephone) {
        if (telephone == null) {
            return "+";
        }
        boolean explicitCountryCode = telephone.trim().startsWith("+");
        String digits = digitsOf(telephone);
        if (explicitCountryCode) {
            return "+" + digits;
        }
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return "+" + DEFAULT_COUNTRY_CODE + digits;
    }

    private String digitsOf(String telephone) {
        return telephone == null ? "" : telephone.replaceAll("\\D", "");
    }
}
