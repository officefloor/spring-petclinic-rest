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
 * Validates an E.164 telephone number, enforcing the national-number length required by its
 * country code: {@code +61} needs 9 national digits and {@code +1} needs 10. Other country
 * codes fall back to the general 8-15 digit rule.
 */
final class E164Telephone {

    private E164Telephone() {
    }

    static boolean isValid(String e164) {
        String digits = e164.startsWith("+") ? e164.substring(1) : "";
        if (!digits.matches("\\d{8,15}")) {
            return false;
        }
        if (e164.startsWith("+61")) {
            return digits.length() == 11; // 2-digit country code + 9 national digits
        }
        if (e164.startsWith("+1")) {
            return digits.length() == 11; // 1-digit country code + 10 national digits
        }
        return true;
    }
}
