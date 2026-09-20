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
 * Normalizes a raw owner address into its canonical, storable form.
 * <p>
 * Surrounding whitespace is trimmed, internal whitespace runs collapse to a single space and the
 * value is upper-cased. Common street-type abbreviations are then expanded to their full words
 * ({@code ST} &rarr; {@code STREET}, {@code RD} &rarr; {@code ROAD}, {@code AVE} &rarr;
 * {@code AVENUE}). A {@code null} or blank input yields an empty string, so the required-field
 * check rejects an address that is blank once normalized.
 */
@Component
public class AddressNormalizer {

    /** Street-type abbreviations expanded to their full words, keyed by the upper-cased token. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Canonicalize {@code rawAddress} for storage and comparison.
     *
     * @param rawAddress the address as supplied by the client (may be {@code null} or blank)
     * @return the trimmed, whitespace-collapsed, upper-cased address with common street-type
     *     abbreviations expanded, or an empty string when the input is {@code null} or blank
     */
    public String normalize(String rawAddress) {
        if (rawAddress == null) {
            return "";
        }
        String cleaned = rawAddress.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (cleaned.isEmpty()) {
            return "";
        }
        String[] tokens = cleaned.split(" ");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]);
        }
        return String.join(" ", tokens);
    }
}
