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

import java.util.Map;

import org.springframework.samples.petclinic.model.Owner;

/**
 * Renders an owner's stored E.164 {@code telephone} for humans as {@code telephoneDisplay}:
 * the country code, a space, then the national digits grouped in threes (e.g. '+61412345678'
 * becomes '+61 412 345 678'). Kept as a small standalone unit so the response mapper can expose
 * the value without growing.
 */
public final class OwnerTelephoneDisplay {

    private OwnerTelephoneDisplay() {
    }

    /** National-number digit count per E.164 country code, used to split the country code off. */
    private static final Map<String, Integer> NATIONAL_DIGITS = Map.of("61", 9, "1", 10);

    public static String of(Owner owner) {
        String e164 = owner.getTelephone();
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        String code = countryCode(digits);
        String grouped = groupInThrees(digits.substring(code.length()));
        return code.isEmpty() ? "+" + grouped : "+" + code + " " + grouped;
    }

    /** The country code whose known national length matches the remaining digits, or "" if none. */
    private static String countryCode(String digits) {
        for (Map.Entry<String, Integer> country : NATIONAL_DIGITS.entrySet()) {
            String code = country.getKey();
            if (digits.startsWith(code) && digits.length() - code.length() == country.getValue()) {
                return code;
            }
        }
        return "";
    }

    /** Joins the digits into space-separated groups of three, left to right. */
    private static String groupInThrees(String national) {
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < national.length(); i++) {
            if (i > 0 && i % 3 == 0) {
                out.append(' ');
            }
            out.append(national.charAt(i));
        }
        return out.toString();
    }
}
