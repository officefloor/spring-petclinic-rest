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

import java.util.function.Predicate;

/**
 * Builds and parses an owner's {@code customerCode}, a stable {@code "<REGION>-<HASH8>"} identity
 * where {@code REGION} is the owner's region and {@code HASH8} is the first eight upper-case hex
 * characters of SHA-256 over the normalized telephone concatenated with the last name.
 */
public final class CustomerCodeGenerator {

    /** Separator between the region prefix and the hash suffix. */
    private static final String SEPARATOR = "-";

    /** Number of upper-case hex characters kept from the digest. */
    private static final int HASH_LENGTH = 8;

    private CustomerCodeGenerator() {
    }

    /**
     * Format a customer code as {@code "<REGION>-<HASH8>"} (for example {@code "NSW-1A2B3C4D"}),
     * where {@code HASH8} is the first eight upper-case hex characters of
     * {@code SHA-256(normalizedTelephone + lastName)}.
     *
     * @param region              the owner's region
     * @param normalizedTelephone the owner's normalized (E.164) telephone
     * @param lastName            the owner's last name
     * @return the formatted customer code
     */
    public static String format(String region, String normalizedTelephone, String lastName) {
        return region + SEPARATOR + hash8(normalizedTelephone, lastName);
    }

    /**
     * De-duplicate a formatted customer code against the codes already in use. When {@code baseCode}
     * is free it is returned unchanged; otherwise {@code "-<n>"} is appended using the smallest
     * {@code n >= 2} that yields a code {@code isTaken} reports as free.
     *
     * @param baseCode the freshly formatted customer code (see {@link #format})
     * @param isTaken  tests whether a candidate code already belongs to another owner
     * @return a customer code that {@code isTaken} reports as free
     */
    public static String deduplicate(String baseCode, Predicate<String> isTaken) {
        if (!isTaken.test(baseCode)) {
            return baseCode;
        }
        for (int n = 2; ; n++) {
            String candidate = baseCode + SEPARATOR + n;
            if (!isTaken.test(candidate)) {
                return candidate;
            }
        }
    }

    /**
     * Extract the {@code REGION} prefix from a customer code produced by {@link #format}.
     *
     * @param customerCode the customer code, may be {@code null}
     * @return the region prefix, or {@code null} when the customer code is {@code null}
     */
    public static String region(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int separator = customerCode.indexOf(SEPARATOR);
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }

    private static String hash8(String normalizedTelephone, String lastName) {
        String key = orEmpty(normalizedTelephone) + orEmpty(lastName);
        return Sha256.hex(key).substring(0, HASH_LENGTH).toUpperCase();
    }

    private static String orEmpty(String value) {
        return value == null ? "" : value;
    }
}
