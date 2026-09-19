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

package org.springframework.samples.petclinic.util;

/**
 * Builds an owner's {@code customerCode} from a city, a last name and a per-city sequence number.
 */
public final class CustomerCodeGenerator {

    private CustomerCodeGenerator() {
    }

    /**
     * Format a customer code as {@code "<CITY3>-<LAST3>-<NNNN>"}, where {@code CITY3} is the
     * upper-cased first three letters of the city, {@code LAST3} the upper-cased first three
     * letters of the last name and {@code NNNN} the per-city sequence number zero-padded to
     * four digits (for example {@code "LON-SMI-0007"}).
     *
     * @param city     the owner's city
     * @param lastName the owner's last name
     * @param sequence the per-city sequence number to encode
     * @return the formatted customer code
     */
    public static String format(String city, String lastName, long sequence) {
        return String.format("%s-%s-%04d", firstThreeUpper(city), firstThreeUpper(lastName), sequence);
    }

    private static String firstThreeUpper(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
