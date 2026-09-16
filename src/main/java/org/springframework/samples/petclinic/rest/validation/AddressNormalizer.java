/*
 * Copyright 2016-2017 the original author or authors.
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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Map;

/**
 * Normalizes an owner's address to its canonical storage form. Surrounding whitespace is trimmed,
 * internal whitespace runs are collapsed to a single space, and the value is upper-cased. Common
 * street-type abbreviations occurring as whole words are expanded ({@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}). The normalized value is what gets stored, returned and
 * compared, so addresses that differ only in spacing, letter case or abbreviation are treated alike.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Returns the canonical form of the submitted address: trimmed, with internal whitespace runs
     * collapsed to a single space, upper-cased, and with common street-type abbreviations expanded.
     *
     * @param address the submitted address, or {@code null} if none was provided
     * @return the normalized address, or {@code null} if the input was {@code null}
     */
    public static String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
