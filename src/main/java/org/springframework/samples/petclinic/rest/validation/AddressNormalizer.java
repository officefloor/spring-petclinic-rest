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

package org.springframework.samples.petclinic.rest.validation;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Normalizes a submitted street address into a single canonical form: whitespace is trimmed and
 * collapsed, the text is upper-cased, and common street-type abbreviations are expanded
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). Applying this once when an
 * owner is created means every consumer - the required-field check, duplicate detection, the shared
 * household id, and the stored value - sees the same normalized address.
 */
@Component
public class AddressNormalizer {

    /** Whole-word abbreviations expanded to their canonical, upper-case form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * @param address the raw, possibly formatted street address (may be {@code null})
     * @return the trimmed, whitespace-collapsed, upper-cased address with abbreviations expanded;
     * an empty string when no meaningful text was supplied
     */
    public String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] words = collapsed.split(" ");
        StringBuilder result = new StringBuilder(collapsed.length());
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(ABBREVIATIONS.getOrDefault(words[i], words[i]));
        }
        return result.toString();
    }
}
