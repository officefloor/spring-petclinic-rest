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

import org.springframework.samples.petclinic.util.Sha256;

/**
 * The owner customer-code format {@code '<REGION>-<HASH8>'}: the owner's canonical region, a
 * hyphen, and {@code HASH8} — the first 8 upper-case hex characters of SHA-256 over the owner's
 * normalized telephone concatenated with its last name.
 *
 * <p>The code is a stable identity derived purely from owner fields, with no sequence numbers, so
 * every value the application builds from it (the membership number, its check digit and the
 * owner's {@linkplain #regionOf(String) locality}) follows from the same region-and-hash identity.
 * This class owns that format: it both builds a code and reads its region back.
 */
public final class CustomerCode {

    /** Separator between the region and the hash. */
    private static final char SEPARATOR = '-';

    /** Number of leading hex characters of the digest kept as {@code HASH8}. */
    private static final int HASH_LENGTH = 8;

    private CustomerCode() {
    }

    /**
     * Build the {@code '<REGION>-<HASH8>'} customer code for the given region and identity inputs.
     *
     * @param region the owner's canonical region
     * @param normalizedTelephone the owner's normalized (E.164) telephone
     * @param lastName the owner's last name
     * @return the customer code
     */
    public static String of(String region, String normalizedTelephone, String lastName) {
        return region + SEPARATOR + Sha256.hexPrefix(normalizedTelephone + lastName, HASH_LENGTH);
    }

    /**
     * Return the region component of a customer code — the part before the separator.
     *
     * @param customerCode the customer code (may be {@code null})
     * @return the region, or {@code null} when {@code customerCode} is {@code null}
     */
    public static String regionOf(String customerCode) {
        if (customerCode == null) {
            return null;
        }
        int separator = customerCode.indexOf(SEPARATOR);
        return separator < 0 ? customerCode : customerCode.substring(0, separator);
    }
}
