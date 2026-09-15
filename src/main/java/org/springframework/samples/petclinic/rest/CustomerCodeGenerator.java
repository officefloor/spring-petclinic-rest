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
package org.springframework.samples.petclinic.rest;

import org.springframework.samples.petclinic.util.LocalityResolver;
import org.springframework.samples.petclinic.util.Sha256Hex;
import org.springframework.stereotype.Component;

/**
 * Builds an owner's customer code, formatted {@code '<REGION>-<HASH8>'} where {@code REGION}
 * is the region code derived from the postcode and {@code HASH8} the first 8 upper-case hex
 * characters of SHA-256 over the normalized telephone concatenated with the last name,
 * e.g. {@code 'NSW-3F2A9C1E'}.
 */
@Component
public class CustomerCodeGenerator {

    /** Separator between the region and hash segments of a customer code. */
    private static final char SEPARATOR = '-';

    /** Number of hex characters taken from the SHA-256 digest for the hash segment. */
    private static final int HASH_LENGTH = 8;

    /**
     * Generate a customer code for a new owner.
     *
     * @param region    the region code derived from the owner's postcode; forms the prefix
     * @param telephone the owner's normalized telephone; hashed with the last name to form the suffix
     * @param lastName  the owner's last name; hashed with the telephone to form the suffix
     * @return the formatted customer code
     */
    public String generate(String region, String telephone, String lastName) {
        return region + SEPARATOR + Sha256Hex.upperHexPrefix(telephone + lastName, HASH_LENGTH);
    }

    /**
     * Extract the region segment from a customer code, i.e. the part before the first
     * {@value #SEPARATOR}. Yields {@link LocalityResolver#UNKNOWN} when the code is absent
     * or carries no region segment.
     *
     * @param customerCode a customer code produced by {@link #generate}, or {@code null}
     * @return the region code embedded in {@code customerCode}
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return LocalityResolver.UNKNOWN;
        }
        int separator = customerCode.indexOf(SEPARATOR);
        return separator < 0 ? LocalityResolver.UNKNOWN : customerCode.substring(0, separator);
    }
}
