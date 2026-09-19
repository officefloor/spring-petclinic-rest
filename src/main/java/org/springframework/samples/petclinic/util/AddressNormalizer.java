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

import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Reduces a postal address to a single canonical form used both for storage/display and for
 * household comparison: leading/trailing whitespace is trimmed, internal runs of whitespace are
 * collapsed to a single space, the result is upper-cased, and common street-type abbreviations are
 * expanded to their full words. Because the stored value is already canonical, any two addresses
 * that differ only in casing, whitespace or abbreviation resolve to the same string.
 */
public final class AddressNormalizer {

    /** Any run of whitespace, collapsed to a single space. */
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    /** Street-type abbreviations expanded (per whitespace-separated word) to their full form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Normalize the given address.
     *
     * @param address the raw address (may be {@code null})
     * @return the trimmed, whitespace-collapsed, upper-cased address with common abbreviations
     * expanded, or {@code null} when {@code address} is {@code null}
     */
    public static String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = WHITESPACE.matcher(address.strip()).replaceAll(" ").toUpperCase(Locale.ROOT);
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
