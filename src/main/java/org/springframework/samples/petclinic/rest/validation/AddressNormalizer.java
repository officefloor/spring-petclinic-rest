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
import java.util.regex.Pattern;

/**
 * Normalizes an owner's postal address to its canonical stored form. Leading and trailing
 * whitespace is trimmed, every internal run of whitespace is collapsed to a single space, the
 * value is upper-cased, and common street-type abbreviations are expanded to their full words
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). The normalized address is
 * the value that is both persisted and used for every address comparison (household duplicate
 * detection and the shared household identifier), so those comparisons are insensitive to case,
 * spacing and abbreviation.
 */
public final class AddressNormalizer {

    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Common street-type abbreviations expanded to their canonical full words. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Produces the canonical form of the supplied address.
     *
     * @param address the raw address as submitted, may be {@code null}
     * @return the normalized address, or an empty string when {@code address} is {@code null} or
     *     blank
     */
    public static String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = WHITESPACE.matcher(address.trim()).replaceAll(" ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
        StringBuilder normalized = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }
}
