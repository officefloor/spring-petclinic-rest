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
 * Formats a stored E.164 telephone for humans: '+' and country code, a space, then the
 * national digits grouped in threes (e.g. '+61412345678' -> '+61 412 345 678').
 */
public final class TelephoneDisplay {

    private TelephoneDisplay() {
    }

    public static String of(String e164) {
        String digits = e164.substring(1);
        int ccLen = digits.startsWith("61") ? 2 : 1;
        String national = digits.substring(ccLen);
        StringBuilder out = new StringBuilder("+").append(digits, 0, ccLen);
        for (int i = 0; i < national.length(); i += 3) {
            out.append(' ').append(national, i, Math.min(i + 3, national.length()));
        }
        return out.toString();
    }
}
