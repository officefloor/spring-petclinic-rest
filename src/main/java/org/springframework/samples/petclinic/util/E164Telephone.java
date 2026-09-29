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
package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Normalizes telephone numbers into <a href="https://en.wikipedia.org/wiki/E.164">E.164</a>
 * form. Spaces, dashes and brackets are stripped. A number that already carries a leading
 * {@code '+'} keeps its explicit country code; otherwise the Australian country code
 * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national
 * digits. The result must contain between 8 and 15 digits after the {@code '+'}, matching
 * the E.164 length rule.
 *
 * <p>In addition to the overall length rule, the national number (the digits after the
 * country code) must have the exact length that country expects: {@code '+61'} (Australia)
 * requires 9 national digits and {@code '+1'} (NANP) requires 10. A number carrying one of
 * these country codes with the wrong national-number length cannot form valid E.164 and is
 * rejected.
 *
 * <p>The same logic backs both the request-time validator (so a number that cannot form
 * valid E.164 is rejected with HTTP 400) and the normalization applied before an
 * {@code Owner} is persisted, so the value a client is told about matches the value that
 * gets stored and compared for duplicates.
 */
public final class E164Telephone {

    /**
     * Country calling code to the exact number of national digits it requires. Order is
     * irrelevant: when a number's digits could match more than one code, the longest
     * matching code is selected (see {@link #toE164(String)}), so entries may be listed
     * in any order.
     */
    private static final Map<String, Integer> NATIONAL_LENGTH = Map.of(
            "61", 9,
            "1", 10);

    private E164Telephone() {
    }

    /**
     * Convert a raw telephone number to E.164 form, or return {@code null} if it cannot
     * form a valid E.164 number.
     *
     * @param raw the raw telephone number as supplied by the client (may be {@code null})
     * @return the normalized {@code +<digits>} string, or {@code null} when invalid
     */
    public static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        String cleaned = raw.replaceAll("[\\s()\\-]", "");
        String digits;
        if (cleaned.startsWith("+")) {
            String body = cleaned.substring(1);
            if (!body.matches("\\d+")) {
                return null;
            }
            digits = body;
        }
        else {
            if (!cleaned.matches("\\d+")) {
                return null;
            }
            String national = cleaned.startsWith("0") ? cleaned.substring(1) : cleaned;
            digits = "61" + national;
        }
        if (digits.length() < 8 || digits.length() > 15) {
            return null;
        }
        String code = longestMatchingCountryCode(digits);
        if (code != null && digits.length() - code.length() != NATIONAL_LENGTH.get(code)) {
            return null;
        }
        return "+" + digits;
    }

    /**
     * Format a stored E.164 telephone number for human display as the country calling code,
     * a space, then the national digits grouped in threes (e.g. {@code "+61412345678"} becomes
     * {@code "+61 412 345 678"}). When the country code is not recognised, the whole set of
     * digits after the {@code '+'} is grouped in threes. Returns the input unchanged when it is
     * {@code null} or not a well-formed {@code +<digits>} value.
     *
     * @param e164 a telephone number already normalized to E.164 form
     * @return the human-friendly representation
     */
    public static String toDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        if (!digits.matches("\\d+")) {
            return e164;
        }
        String code = longestMatchingCountryCode(digits);
        String national = code == null ? digits : digits.substring(code.length());
        String grouped = groupInThrees(national);
        if (code == null) {
            return "+" + grouped;
        }
        return "+" + code + (grouped.isEmpty() ? "" : " " + grouped);
    }

    /**
     * Group a run of digits into space-separated blocks of three, counting from the left
     * (e.g. {@code "412345678"} becomes {@code "412 345 678"}).
     */
    private static String groupInThrees(String digits) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digits.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                sb.append(' ');
            }
            sb.append(digits.charAt(i));
        }
        return sb.toString();
    }

    /**
     * Return the longest known country calling code that {@code digits} starts with, or
     * {@code null} if none is known. Selecting the longest match makes the result
     * independent of {@link #NATIONAL_LENGTH}'s iteration order even when one code is a
     * prefix of another.
     */
    private static String longestMatchingCountryCode(String digits) {
        String match = null;
        for (String code : NATIONAL_LENGTH.keySet()) {
            if (digits.startsWith(code) && (match == null || code.length() > match.length())) {
                match = code;
            }
        }
        return match;
    }
}
