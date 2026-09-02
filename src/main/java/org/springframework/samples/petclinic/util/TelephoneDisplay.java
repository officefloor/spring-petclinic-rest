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
 * Formats a stored E.164 telephone for humans: the country code, a space, then the national digits
 * grouped in threes (e.g. '+61412345678' -> '+61 412 345 678'). Known country codes '+61' and '+1'
 * are split explicitly; any other number keeps a single-digit country code.
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String format(String e164) {
        if (e164 == null) {
            return null;
        }
        String countryCode = e164.startsWith("+61") ? "+61" : e164.startsWith("+1") ? "+1" : e164.substring(0, 2);
        String national = e164.substring(countryCode.length());
        return countryCode + " " + national.replaceAll("(\\d{3})(?=\\d)", "$1 ");
    }
}
