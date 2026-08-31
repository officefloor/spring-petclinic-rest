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

package org.springframework.samples.petclinic.util;

import java.util.Map;

/**
 * Normalises a postal address to a canonical form for storage and comparison.
 *
 * <p>Whitespace is trimmed and collapsed, the value is upper-cased, and common
 * street-type abbreviations are expanded as whole words ({@code ST -> STREET},
 * {@code RD -> ROAD}, {@code AVE -> AVENUE}). A value that is blank (only
 * whitespace) normalises to the empty string.
 */
public abstract class Addresses {

    private static final Map<String, String> ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    /**
     * @param raw the raw address input, may be {@code null}
     * @return the normalised address, or {@code null} when {@code raw} is {@code null}
     */
    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String token : raw.trim().toUpperCase().split("\\s+")) {
            if (token.isEmpty()) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(token, token));
        }
        return sb.toString();
    }

}
