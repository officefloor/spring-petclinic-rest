/*
 * Copyright 2016 the original author or authors.
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

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Converts free-form telephone input into E.164 form: keep a leading '+' and country
 * code when present, otherwise assume '+61' and drop a single leading '0' from the
 * national digits. Spaces, dashes and brackets are stripped and 8 to 15 digits must
 * remain after the '+', or an {@link InvalidTelephoneException} is thrown.
 */
public final class E164 {

    private static final Pattern SEPARATORS = Pattern.compile("[\\s()\\-]");

    private static final Pattern EIGHT_TO_FIFTEEN_DIGITS = Pattern.compile("\\d{8,15}");

    /** Country calling code -> required national-number length. */
    private static final Map<String, Integer> NATIONAL_LENGTHS = Map.of("61", 9, "1", 10);

    private E164() {
    }

    /** True unless the digits carry a known country code with the wrong national-number length. */
    private static boolean nationalLengthValid(String digits) {
        for (Map.Entry<String, Integer> country : NATIONAL_LENGTHS.entrySet()) {
            if (digits.startsWith(country.getKey())) {
                return digits.length() - country.getKey().length() == country.getValue();
            }
        }
        return true;
    }

    public static String toE164(String raw) {
        if (raw == null) {
            throw new InvalidTelephoneException(null);
        }
        String trimmed = raw.trim();
        boolean explicit = trimmed.startsWith("+");
        String digits = SEPARATORS.matcher(explicit ? trimmed.substring(1) : trimmed).replaceAll("");
        if (!explicit) {
            if (digits.startsWith("0")) {
                digits = digits.substring(1);
            }
            digits = "61" + digits;
        }
        if (!EIGHT_TO_FIFTEEN_DIGITS.matcher(digits).matches() || !nationalLengthValid(digits)) {
            throw new InvalidTelephoneException(raw);
        }
        return "+" + digits;
    }
}
