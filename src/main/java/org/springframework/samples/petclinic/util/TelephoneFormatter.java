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
 * Formats a stored E.164 telephone for humans as '+CC nnn nnn nnn' — the country code,
 * a space, then the national digits grouped in threes (e.g. '+61 412 345 678'). Input
 * that is {@code null} or not in E.164 form (no leading '+') is returned unchanged.
 */
public final class TelephoneFormatter {

    private TelephoneFormatter() {
    }

    public static String toDisplay(String e164) {
        if (e164 == null || !e164.startsWith("+")) {
            return e164;
        }
        int national = e164.startsWith("+1") ? 2 : 3;
        StringBuilder out = new StringBuilder(e164.substring(0, national));
        for (int i = national; i < e164.length(); i += 3) {
            out.append(' ').append(e164, i, Math.min(i + 3, e164.length()));
        }
        return out.toString();
    }
}
