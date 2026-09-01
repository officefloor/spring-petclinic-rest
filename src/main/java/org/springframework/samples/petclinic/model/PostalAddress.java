/*
 * Copyright 2002-2013 the original author or authors.
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
package org.springframework.samples.petclinic.model;

import java.util.Locale;
import java.util.Map;

/**
 * Normalises a raw postal address: trims and collapses whitespace, upper-cases,
 * and expands common abbreviations ({@code ST->STREET}, {@code RD->ROAD},
 * {@code AVE->AVENUE}).
 */
final class PostalAddress {

    private static final Map<String, String> ABBREVIATIONS = Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private PostalAddress() {
    }

    /** Returns {@code null} for a {@code null} input. */
    static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (String word : raw.strip().split("\\s+")) {
            String upper = word.toUpperCase(Locale.ROOT);
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(ABBREVIATIONS.getOrDefault(upper, upper));
        }
        return sb.toString();
    }
}
