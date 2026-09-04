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
 * Normalises raw telephone input to E.164 form.
 */
public final class PhoneNumbers {

    private PhoneNumbers() {
    }

    /**
     * Convert a raw telephone string to E.164. Spaces, dashes and brackets are stripped.
     * A leading '+' and its country code are kept; otherwise country code '+61' is assumed
     * and a single leading '0' is dropped from the national digits.
     *
     * @param raw the caller-supplied telephone value
     * @return the E.164 representation (leading '+' followed by digits)
     */
    public static String toE164(String raw) {
        String digits = raw.replaceAll("\\D", "");
        if (raw.indexOf('+') >= 0) {
            return "+" + digits;
        }
        if (digits.startsWith("0")) {
            digits = digits.substring(1);
        }
        return "+61" + digits;
    }
}
