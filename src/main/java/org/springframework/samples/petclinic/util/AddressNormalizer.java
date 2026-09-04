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

package org.springframework.samples.petclinic.util;

import java.util.Locale;
import java.util.Map;

/**
 * Normalization of owner postal addresses. Kept separate from the owner controller and the
 * {@code Owner} model so the rule for shaping an address lives in one place, as pure functions with
 * no web or persistence dependencies. Mirrors {@link TelephoneNormalizer}, which does the same for
 * telephones.
 *
 * <p>A submitted address is reduced to its canonical form, which is the value stored and returned:
 * it is trimmed, every run of whitespace is collapsed to a single space, it is upper-cased, and
 * common street-type abbreviations are expanded to their full words ({@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}). Expansion is applied per whole word, so an
 * abbreviation embedded in a longer word (e.g. the {@code ST} in {@code EAST}) is left untouched.
 */
public abstract class AddressNormalizer {

    /** A run of one or more whitespace characters, collapsed to a single space. */
    private static final String WHITESPACE_RUN = "\\s+";

    /** Whole-word (upper-case) abbreviations expanded to their full street type. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Reduce a submitted address to its canonical form, which is the value to be stored and returned:
     * trimmed, every run of whitespace collapsed to a single space, upper-cased, and common street-type
     * abbreviations expanded ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). An
     * address that is empty or whitespace-only reduces to the empty string.
     *
     * @param rawAddress the submitted address (non-null)
     * @return the canonical address
     */
    public static String normalize(String rawAddress) {
        String collapsed = rawAddress.trim().replaceAll(WHITESPACE_RUN, " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return collapsed;
        }
        String[] words = collapsed.split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < words.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(words[i], words[i]));
        }
        return sb.toString();
    }

}
