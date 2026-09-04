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

package org.springframework.samples.petclinic.rest.controller;

import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Normalises a raw postal address to the single canonical form under which it is both
 * stored and compared.
 *
 * <p>Keeping this in one place means the stored form and the form used for household
 * duplicate detection and the shared household id can never drift apart: two owners
 * belong to the same household exactly when their addresses normalise to the same value.
 *
 * <p>The canonical form trims surrounding whitespace, collapses internal whitespace runs
 * to a single space, upper-cases every character, and expands the common street-type
 * abbreviations ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}) that
 * appear as whole words. An address consisting only of whitespace normalises to the empty
 * string.
 */
@Component
public class AddressNormalizer {

    /** Whole-word street-type abbreviations expanded to their canonical form (keys are
     *  already upper-cased, matching the point at which they are applied). */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Reduce a raw address to its canonical stored form.
     *
     * @param rawAddress the address as supplied by the client, possibly {@code null} or
     *                   containing irregular whitespace, casing and abbreviations
     * @return the canonical address (trimmed, whitespace-collapsed, upper-cased and with
     *         common abbreviations expanded), or the empty string if {@code rawAddress} is
     *         {@code null} or blank
     */
    public String normalize(String rawAddress) {
        if (rawAddress == null) {
            return "";
        }
        String collapsed = rawAddress.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
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
