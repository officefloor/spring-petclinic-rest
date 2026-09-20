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

import org.springframework.stereotype.Component;

/**
 * Formats an owner's {@code customerCode} as {@code '<CITY3>-<LAST3>-<NNNN>'}, where {@code CITY3}
 * and {@code LAST3} are the upper-cased first three letters of the city and last name and
 * {@code NNNN} is a zero-padded, per-city 4-digit sequence number (e.g. {@code 'LON-SMI-0007'}).
 */
@Component
public class CustomerCodeGenerator {

    /**
     * Build the customer code for the given city, last name and per-city sequence number.
     *
     * @param city     the owner's city (its first three letters, upper-cased, form the leading prefix)
     * @param lastName the owner's last name (its first three letters, upper-cased, form the middle segment)
     * @param sequence the 4-digit per-city sequence number
     * @return the formatted customer code
     */
    public String generate(String city, String lastName, long sequence) {
        return String.format("%s-%s-%04d", prefix(city), prefix(lastName), sequence);
    }

    private String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
