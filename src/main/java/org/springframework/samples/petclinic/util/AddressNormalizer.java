/*
 * Copyright 2002-2017 the original author or authors.
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
 * Normalizes free-form postal addresses into a single canonical form so a value is stored,
 * returned and compared in exactly one shape.
 *
 * <p>The rules, applied in order: trim surrounding whitespace and collapse internal runs of
 * whitespace to a single space; upper-case; and expand common street-type abbreviations as whole
 * words ({@code ST->STREET}, {@code RD->ROAD}, {@code AVE->AVENUE}). For example
 * {@code '  12  main  st '} normalizes to {@code '12 MAIN STREET'}. A value that is blank after
 * trimming and collapsing has no canonical form.
 *
 * <p>This is the single source of truth shared by the inbound {@code AddressValidation} constraint
 * (which rejects an address that is blank after normalization), {@code OwnerMapper} (which stores
 * the normalized string) and the household comparisons in the service layer, so a value that
 * validates is exactly one that normalizes.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    /**
     * Convert a raw address to its canonical form, or return {@code null} when it has none.
     *
     * @param raw the inbound address (may be {@code null})
     * @return the normalized address (e.g. {@code '12 MAIN STREET'}), or {@code null} if {@code raw}
     * is {@code null} or is blank after trimming and collapsing whitespace
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String collapsed = raw.trim().replaceAll("\\s+", " ");
        if (collapsed.isEmpty()) {
            return null;
        }
        String[] tokens = collapsed.toUpperCase(Locale.ROOT).split(" ");
        StringBuilder normalized = new StringBuilder();
        for (int i = 0; i < tokens.length; i++) {
            if (i > 0) {
                normalized.append(' ');
            }
            normalized.append(ABBREVIATIONS.getOrDefault(tokens[i], tokens[i]));
        }
        return normalized.toString();
    }
}
