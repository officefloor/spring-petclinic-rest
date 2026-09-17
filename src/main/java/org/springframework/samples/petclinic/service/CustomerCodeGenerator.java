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
package org.springframework.samples.petclinic.service;

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <CITY3>-<LAST3>-<NNNN>} where CITY3
 * is the upper-cased first three letters of the city, LAST3 the upper-cased first three letters
 * of the last name and NNNN a 4-digit zero-padded per-city sequence number (e.g.
 * {@code "LON-SMI-0007"}).
 */
@Component
public class CustomerCodeGenerator {

    /**
     * Build the customer code for the given city, last name and sequence number.
     *
     * @param city     the owner's city; its first three letters (upper-cased) form the leading prefix
     * @param lastName the owner's last name; its first three letters (upper-cased) form the middle prefix
     * @param sequence the per-city sequence number, rendered as a 4-digit zero-padded suffix
     * @return the formatted customer code
     */
    public String generate(String city, String lastName, long sequence) {
        return String.format("%s-%s-%04d", prefix(city), prefix(lastName), sequence);
    }

    /** The upper-cased first three letters of {@code value}, trimmed. */
    private String prefix(String value) {
        String trimmed = value.trim();
        return trimmed.substring(0, Math.min(3, trimmed.length())).toUpperCase(Locale.ROOT);
    }
}
