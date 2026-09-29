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

/**
 * Utility for normalizing the postal address submitted when creating an owner. The value is
 * trimmed, internal whitespace runs are collapsed to a single space, the result is upper-cased,
 * and common abbreviations are expanded token-by-token ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). The normalized value is what gets stored and returned, and it is also
 * the form used for every address comparison (household-duplicate detection and the shared
 * household id).
 *
 * <p>Because the address is a required field, {@link #normalize(String)} rejects a value that is
 * blank once normalized by raising an {@link InvalidAddressException} so the request is rejected
 * with {@code 400 Bad Request}. {@link #canonical(String)} exposes the same transformation without
 * the required-field check, for comparing addresses that may already be stored in another form.
 */
public final class AddressNormalizer {

    /**
     * Common street-type abbreviations expanded to their full form. Keys are upper-cased so the
     * lookup happens after the value has itself been upper-cased.
     */
    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Normalize the supplied address for storage.
     *
     * @param address the raw address value (may be {@code null})
     * @return the trimmed, whitespace-collapsed, upper-cased address with common abbreviations expanded
     * @throws InvalidAddressException if the value is blank once normalized
     */
    public static String normalize(String address) {
        String canonical = canonical(address);
        if (canonical.isEmpty()) {
            throw new InvalidAddressException("address must not be blank");
        }
        return canonical;
    }

    /**
     * Produce the canonical form of the supplied address for equality comparison, applying the same
     * transformation as {@link #normalize(String)} but returning an empty string (rather than
     * throwing) when the value is {@code null} or blank once normalized.
     *
     * @param address the raw address value (may be {@code null})
     * @return the normalized address, or an empty string when {@code null} or blank
     */
    public static String canonical(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (collapsed.isEmpty()) {
            return "";
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
