/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.model;

import java.util.List;

/**
 * Pure, stateless helpers over a telephone number in E.164 form, together with the
 * fixed table of recognised country dialling codes that governs them.
 *
 * <p>Like {@link OwnerDerivations}, these operate on a bare value (here a telephone
 * string) rather than on an {@link Owner}, so they stay stateless and side-effect
 * free. Keeping the E.164 rules and their single country-code table together in one
 * place lets the REST boundary normalise an incoming number to canonical form while
 * the owner derivations read the same stored number back, without either side having
 * to restate what a country code is or how long its national number runs.
 */
public final class Telephones {

    /**
     * A recognised country dialling code and the exact national-number length it
     * pins. The {@code code} is the leading run of digits that identifies the country
     * ({@code "61"} for Australia, {@code "1"} for the NANP), so the country code's
     * own length is simply {@code code.length()}.
     */
    private record CountryCode(String code, int nationalNumberLength) {
    }

    /**
     * The fixed table of recognised country dialling codes, listed longest code first
     * so a lookup matches the most specific code a number begins with. A country code
     * absent from this table carries no pinned national-number length.
     */
    private static final List<CountryCode> COUNTRY_CODES = List.of(
        new CountryCode("61", 9), new CountryCode("1", 10));

    private Telephones() {
    }

    /**
     * Normalise a telephone number to E.164 form: keep a leading '+' and country
     * code when present, otherwise assume country code '+61' and drop a single
     * leading '0' from the national digits. Spaces, dashes and brackets are
     * stripped. The result must contain 8 to 15 digits after the '+'.
     *
     * <p>In addition, the national-number length is validated against the country
     * code: a {@code +61} (Australia) number must have exactly 9 national digits
     * and a {@code +1} (NANP) number must have exactly 10 national digits. A number
     * whose national-number length is wrong for its country code is rejected.
     *
     * @return the E.164 string (e.g. "+61412345678"), or {@code null} if the
     *         input cannot form a valid E.164 number.
     */
    public static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            digits = cleaned.substring(1);
        } else {
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (!digits.matches("\\d{8,15}")) {
            return null;
        }
        if (!nationalNumberLengthValid(digits)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Format an E.164 telephone number for humans: a leading '+' and country
     * code, a space, then the national digits (the digits after the country
     * code) grouped in threes from the left, e.g. {@code "+61412345678"} becomes
     * {@code "+61 412 345 678"}. When the number begins with no recognised
     * country code, all digits after the '+' are grouped in threes with no
     * separate country-code group.
     *
     * @param e164 the stored telephone number in E.164 form (e.g. "+61412345678")
     * @return the human-formatted number, or {@code null} if {@code e164} is
     *         {@code null} or not a '+' followed by digits.
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return null;
        }
        String digits = e164.substring(1);
        if (!digits.matches("\\d+")) {
            return null;
        }
        CountryCode country = countryCodeFor(digits);
        if (country == null) {
            return "+" + groupInThrees(digits);
        }
        String code = country.code();
        String national = digits.substring(code.length());
        return "+" + code + " " + groupInThrees(national);
    }

    /**
     * Group a run of digits into space-separated chunks of three, from the left,
     * so a final short chunk keeps the remaining one or two digits.
     */
    private static String groupInThrees(String digits) {
        StringBuilder grouped = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                grouped.append(' ');
            }
            grouped.append(digits.charAt(i));
        }
        return grouped.toString();
    }

    /**
     * Validate the length of the national number (the digits after the country
     * code) against the country code carried by the given E.164 digit string: a
     * {@code 61} (Australia) number requires exactly 9 national digits and a
     * {@code 1} (NANP) number requires exactly 10 national digits. Country codes
     * without a pinned length are accepted (only the generic 8-15 digit rule
     * applies to them).
     *
     * @param digits the E.164 digits without the leading '+'
     * @return {@code true} if the national-number length is correct for the country
     */
    private static boolean nationalNumberLengthValid(String digits) {
        CountryCode country = countryCodeFor(digits);
        if (country == null) {
            return true;
        }
        return digits.length() - country.code().length() == country.nationalNumberLength();
    }

    /**
     * The recognised country dialling code that the given E.164 digit string (no
     * leading '+') begins with, or {@code null} when it begins with no code in the
     * fixed table. The longest matching code wins.
     */
    private static CountryCode countryCodeFor(String digits) {
        for (CountryCode country : COUNTRY_CODES) {
            if (digits.startsWith(country.code())) {
                return country;
            }
        }
        return null;
    }
}
