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

import org.springframework.stereotype.Component;

/**
 * Normalizes an owner's postal address into a single canonical form so that
 * addresses that differ only cosmetically are stored, returned and compared
 * identically.
 *
 * <p>Normalization trims and collapses each run of whitespace to a single space,
 * upper-cases the value and expands common street-type abbreviations
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). An address
 * that is blank after normalization carries no meaning and is rejected by the
 * required-field check.
 */
@Component
public class AddressNormalizer {

    /** Whole-word street-type abbreviations expanded to their canonical form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Convert a raw address into its canonical normalized form.
     *
     * @param address the raw address value (may be {@code null})
     * @return the normalized address, or {@code null} if {@code address} is {@code null}
     */
    public String normalize(String address) {
        if (address == null) {
            return null;
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] words = collapsed.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = ABBREVIATIONS.getOrDefault(words[i], words[i]);
        }
        return String.join(" ", words);
    }

    /**
     * @param address an address value (typically already normalized)
     * @return {@code true} if it carries meaningful, non-blank content
     */
    public boolean isBlank(String address) {
        return address == null || address.isBlank();
    }
}
