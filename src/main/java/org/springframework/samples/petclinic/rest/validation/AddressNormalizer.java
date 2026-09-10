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

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Turns owner addresses into the single canonical form that gets stored, returned and compared, so the REST
 * controllers do not have to carry the address rules themselves.
 *
 * <p>Normalization trims the value and collapses every run of whitespace to a single space, upper-cases it, and expands
 * a small set of common abbreviations that appear as whole words: {@code ST -> STREET}, {@code RD -> ROAD} and
 * {@code AVE -> AVENUE}. So {@code "  12  main  st "} becomes {@code "12 MAIN STREET"} and {@code "7 elm ave"} becomes
 * {@code "7 ELM AVENUE"}.
 *
 * <p>The same canonical form is used both for the value that is stored and returned and for every address comparison
 * (household duplicate detection and the shared household id), so incidental formatting differences never affect the
 * outcome.
 */
@Component
public class AddressNormalizer {

    /** Whole-word abbreviations expanded during normalization, keyed by their upper-cased form. */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalizes an owner address into its canonical stored form: trimmed, whitespace-collapsed, upper-cased and with
     * common abbreviations expanded. A {@code null} input yields an empty string, so callers can uniformly treat a
     * blank result as a missing address.
     *
     * @param address the raw address value from the request, may be {@code null}
     * @return the normalized canonical address, never {@code null}
     */
    public String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.strip().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        StringBuilder result = new StringBuilder(collapsed.length());
        for (String token : collapsed.split(" ")) {
            if (result.length() > 0) {
                result.append(' ');
            }
            result.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return result.toString();
    }
}
