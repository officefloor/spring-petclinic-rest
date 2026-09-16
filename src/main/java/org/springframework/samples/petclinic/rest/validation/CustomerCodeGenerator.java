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

import java.util.Locale;

/**
 * Derives an owner's customer code, formatted {@code <REGION>-<HASH8>} where REGION is the owner's
 * region code (derived from the postcode, see
 * {@link org.springframework.samples.petclinic.model.RegionResolver RegionResolver}) and HASH8 is
 * the first eight upper-case hexadecimal characters of the SHA-256 digest of the owner's normalized
 * telephone concatenated with the last name (e.g. {@code NSW-1A2B3C4D}). The code carries no
 * sequence number, so it is stable for a given region and (telephone, last name) pair.
 */
public final class CustomerCodeGenerator {

    /** Number of leading hex characters of the SHA-256 digest kept as the code's hash segment. */
    private static final int HASH_LENGTH = 8;

    private static final char SEPARATOR = '-';

    private CustomerCodeGenerator() {
    }

    /**
     * Builds the customer code for a new owner.
     *
     * @param region              the owner's region code
     * @param normalizedTelephone the owner's telephone in its normalized storage form
     * @param lastName            the owner's last name
     * @return the customer code, e.g. {@code "NSW-1A2B3C4D"}
     */
    public static String generate(String region, String normalizedTelephone, String lastName) {
        String hash8 = Sha256.hex(orEmpty(normalizedTelephone) + orEmpty(lastName))
            .substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
        return region + SEPARATOR + hash8;
    }

    /**
     * Return the REGION segment of a customer code (the text before the separator).
     *
     * @param customerCode a customer code produced by {@link #generate}
     * @return the region segment
     */
    public static String regionOf(String customerCode) {
        int separator = customerCode.indexOf(SEPARATOR);
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
