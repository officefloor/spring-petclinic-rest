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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Normalizes an owner's postal address on create.
 *
 * <p>The raw value is trimmed, every run of whitespace is collapsed to a single space,
 * the text is upper-cased, and common street-type abbreviations are expanded to their
 * full words ({@code ST}&rarr;{@code STREET}, {@code RD}&rarr;{@code ROAD},
 * {@code AVE}&rarr;{@code AVENUE}). This canonical form is what gets stored, returned,
 * and compared when detecting duplicate households.
 */
@Component
public class AddressNormalizer {

    /** Whole-word abbreviations expanded to their canonical street type. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Reduce {@code raw} to its canonical address form.
     *
     * @param raw the submitted address value (may be {@code null})
     * @return the normalized address, or an empty string when {@code raw} is {@code null}
     *         or blank
     */
    public String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String collapsed = raw.strip().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
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
