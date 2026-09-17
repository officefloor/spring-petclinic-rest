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

package org.springframework.samples.petclinic.rest.assignment;

import java.util.Locale;

import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code customerCode}, formatted {@code '<CITY3>-<LAST3>-<NNNN>'} where CITY3 is
 * the upper-cased first three letters of the city, LAST3 the upper-cased first three letters of the
 * last name, and NNNN a per-city 4-digit zero-padded sequence equal to one more than the number of
 * owners already in that city (e.g. {@code 'LON-SMI-0007'}).
 */
@Component
public class CustomerCodeGenerator {

    /**
     * @param city          the owner's city, used for the CITY3 prefix
     * @param lastName      the owner's last name, used for the LAST3 prefix
     * @param cityOwnerCount the number of owners already stored in that city; the sequence is one greater
     * @return the formatted customer code
     */
    public String generate(String city, String lastName, long cityOwnerCount) {
        return String.format("%s-%s-%04d", abbreviate(city), abbreviate(lastName), cityOwnerCount + 1);
    }

    private String abbreviate(String value) {
        return value.substring(0, Math.min(3, value.length())).toUpperCase(Locale.ROOT);
    }
}
