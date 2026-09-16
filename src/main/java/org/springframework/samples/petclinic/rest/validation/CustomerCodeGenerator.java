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

/**
 * Derives an owner's customer code, formatted {@code <CITY3>-<LAST3>-<NNNN>} where CITY3 is the
 * upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the
 * last name and NNNN is a per-city 4-digit, zero-padded sequence equal to one more than the
 * number of owners already in that city (e.g. {@code LON-SMI-0007}).
 */
public final class CustomerCodeGenerator {

    private CustomerCodeGenerator() {
    }

    /**
     * Builds the customer code for a new owner.
     *
     * @param city           the owner's city
     * @param lastName       the owner's last name
     * @param cityOwnerCount the number of owners already in that city
     * @return the customer code, e.g. {@code "LON-SMI-0007"}
     */
    public static String generate(String city, String lastName, long cityOwnerCount) {
        String city3 = prefix(city);
        String last3 = prefix(lastName);
        return String.format("%s-%s-%04d", city3, last3, cityOwnerCount + 1);
    }

    private static String prefix(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase();
    }
}
