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

package org.springframework.samples.petclinic.rest.advice;

/**
 * Converts a raw telephone into E.164 form. A leading '+' and country code are kept as given;
 * otherwise country code '+61' is assumed and a single leading '0' is dropped from the national
 * digits. Spaces, dashes and brackets are stripped, and the result must carry 8 to 15 digits after
 * the '+'. Returns {@code null} when no valid E.164 number can be formed.
 */
final class E164Telephone {

    private E164Telephone() {
    }

    static String toE164(String raw) {
        String cleaned = raw.replaceAll("[ ()\\-]", "");
        String digits = cleaned.startsWith("+") ? cleaned.substring(1)
            : "61" + (cleaned.startsWith("0") ? cleaned.substring(1) : cleaned);
        return digits.matches("\\d{8,15}") ? "+" + digits : null;
    }
}
