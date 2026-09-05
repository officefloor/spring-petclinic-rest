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
package org.springframework.samples.petclinic.service;

import java.util.Locale;
import java.util.Map;

/**
 * Normalises a free-text street address into a canonical form: trimmed, internal
 * whitespace collapsed to single spaces, upper-cased, with common street-type
 * abbreviations expanded (ST-&gt;STREET, RD-&gt;ROAD, AVE-&gt;AVENUE). Rejects an
 * address that is blank once normalised, since address is a required field.
 */
public final class AddressNormalizer {

    private static final Map<String, String> ABBREVIATIONS =
        Map.of("ST", "STREET", "RD", "ROAD", "AVE", "AVENUE");

    private AddressNormalizer() {
    }

    public static String normalize(String address) {
        String cleaned = address == null ? "" : address.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
        if (cleaned.isEmpty()) {
            throw new IllegalArgumentException("Address must not be blank after normalization");
        }
        String[] words = cleaned.split(" ");
        for (int i = 0; i < words.length; i++) {
            words[i] = ABBREVIATIONS.getOrDefault(words[i], words[i]);
        }
        return String.join(" ", words);
    }
}
