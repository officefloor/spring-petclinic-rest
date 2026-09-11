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

import org.springframework.samples.petclinic.rest.advice.InvalidAddressException;
import org.springframework.stereotype.Component;

/**
 * Produces the canonical stored form of an owner's postal address.
 * <p>
 * Centralising this here keeps the REST controllers thin: they delegate to
 * {@link #normalize(String)} when canonicalising an incoming value before it is persisted,
 * and because addresses are stored in canonical form every later comparison of addresses
 * (household duplicate detection and the shared household id) operates on the same form and
 * they therefore always agree.
 * <p>
 * The canonical form trims the value, collapses runs of whitespace to a single space and
 * upper-cases it, then expands common street-type abbreviations token-by-token
 * ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}). A value that is blank
 * after normalization carries no address and is rejected with {@link InvalidAddressException}
 * (a 400), so the required-field check is applied to the normalized value rather than the raw
 * input.
 */
@Component
public class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Normalizes the supplied address to its canonical stored form: trimmed, with internal runs
     * of whitespace collapsed to a single space, upper-cased and with common street-type
     * abbreviations expanded ({@code ST -> STREET}, {@code RD -> ROAD}, {@code AVE -> AVENUE}).
     * Values that differ only in letter case, incidental whitespace or the use of these
     * abbreviations therefore normalize to the same result.
     *
     * @param address the raw address value (may be {@code null})
     * @return the normalized address
     * @throws InvalidAddressException if the value is blank after normalization
     */
    public String normalize(String address) {
        String collapsed = address == null ? "" : address.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            throw new InvalidAddressException("Address must not be blank after normalization");
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
        StringBuilder sb = new StringBuilder(collapsed.length());
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return sb.toString();
    }
}
