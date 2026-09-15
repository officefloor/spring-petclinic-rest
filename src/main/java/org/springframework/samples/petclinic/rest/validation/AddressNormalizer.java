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
 * Normalizes a submitted address to its canonical stored form: surrounding whitespace is
 * trimmed, internal runs of whitespace are collapsed to a single space, the text is
 * upper-cased and common abbreviations are expanded to their full words (e.g. {@code ST}
 * to {@code STREET}). An absent (null) address is left as {@code null}; an address that is
 * blank once trimmed normalizes to an empty string.
 */
@Component
public class AddressNormalizer {

    /** Whole-word abbreviations expanded to their canonical full form, keyed upper-cased. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * @param address the raw submitted address (may be {@code null})
     * @return {@code null} if the input is {@code null}; otherwise the trimmed, whitespace-collapsed,
     *         upper-cased address with common abbreviations expanded (empty when blank)
     */
    public String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder result = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                result.append(' ');
            }
            result.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return result.toString();
    }
}
