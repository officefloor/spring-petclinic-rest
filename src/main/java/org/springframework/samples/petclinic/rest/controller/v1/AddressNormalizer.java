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

package org.springframework.samples.petclinic.rest.controller.v1;

import java.util.Map;

import org.springframework.stereotype.Component;

/**
 * Canonicalises a postal address into the single form used to store, compare and
 * identify owners' addresses, so that matching does not depend on incidental
 * differences in spacing, casing or the use of common abbreviations.
 *
 * <p>The canonical form trims surrounding whitespace, collapses internal runs of
 * whitespace to a single space, upper-cases the result and expands common street-type
 * abbreviations token-by-token ({@code ST -> STREET}, {@code RD -> ROAD},
 * {@code AVE -> AVENUE}). A {@code null} or blank address canonicalises to the empty
 * string.
 */
@Component
public class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS = Map.of(
        "ST", "STREET",
        "RD", "ROAD",
        "AVE", "AVENUE");

    /**
     * Return the canonical form of {@code address} (see class documentation), or the
     * empty string when {@code address} is {@code null} or blank.
     */
    public String normalize(String address) {
        if (address == null) {
            return "";
        }
        String collapsed = address.trim().replaceAll("\\s+", " ").toUpperCase();
        if (collapsed.isEmpty()) {
            return "";
        }
        String[] tokens = collapsed.split(" ");
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
