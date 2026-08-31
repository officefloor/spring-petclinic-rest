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

package org.springframework.samples.petclinic.util;

/**
 * Formats a stored E.164 telephone number for humans: the {@code '+'} country code, a space, then
 * the national digits grouped in threes (e.g. {@code '+61412345678'} becomes {@code '+61 412 345
 * 678'}). The country code split mirrors {@link E164}: {@code '+1'} is one digit, everything else
 * (such as Australia's {@code '+61'}) is two.
 */
public abstract class TelephoneDisplay {

    /**
     * @param e164 the stored E.164 number, may be {@code null}
     * @return the human-readable form, or {@code e164} unchanged when {@code null} or not E.164
     */
    public static String format(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        String digits = e164.substring(1);
        int countryLen = digits.startsWith("1") ? 1 : 2;
        StringBuilder out = new StringBuilder("+").append(digits, 0, countryLen);
        for (int i = countryLen; i < digits.length(); i += 3) {
            out.append(' ').append(digits, i, Math.min(i + 3, digits.length()));
        }
        return out.toString();
    }

}
