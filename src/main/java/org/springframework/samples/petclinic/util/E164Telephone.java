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

/**
 * Normalizes telephone numbers into <a href="https://en.wikipedia.org/wiki/E.164">E.164</a>
 * form. Spaces, dashes and brackets are stripped. A number that already carries a leading
 * {@code '+'} keeps its explicit country code; otherwise the Australian country code
 * {@code '+61'} is assumed and a single leading {@code '0'} is dropped from the national
 * digits. The result must contain between 8 and 15 digits after the {@code '+'}, matching
 * the E.164 length rule.
 *
 * <p>The same logic backs both the request-time validator (so a number that cannot form
 * valid E.164 is rejected with HTTP 400) and the normalization applied before an
 * {@code Owner} is persisted, so the value a client is told about matches the value that
 * gets stored and compared for duplicates.
 */
public final class E164Telephone {

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
        return "+" + digits;
    }
}
