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

/**
 * Normalises a raw telephone string to E.164 form.
 */
final class TelephoneNumber {

    private TelephoneNumber() {
    }

    /**
     * Convert a raw telephone number to E.164: keep a leading {@code '+'} and country
     * code when present; otherwise assume country code {@code '+61'} and drop a single
     * leading {@code '0'} from the national digits. Spaces, dashes and brackets are
     * stripped. Returns {@code null} for a {@code null} input.
     */
    static String toE164(String raw) {
        if (raw == null) {
            return null;
        }
        boolean international = raw.trim().startsWith("+");
        String digits = raw.replaceAll("\\D", "");
        if (international) {
            return "+" + digits;
        }
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return "+61" + digits;
    }
}
