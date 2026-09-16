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
import java.util.function.Predicate;

import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code customerCode}, formatted {@code '<REGION>-<HASH8>'} where
 * REGION is the owner's canonical region (see
 * {@link org.springframework.samples.petclinic.model.RegionResolver}) and HASH8 is the
 * first eight upper-case hexadecimal characters of the SHA-256 hash over the owner's
 * normalized telephone followed by its last name. The identity is derived purely from
 * the owner's own fields, carrying no sequence number.
 */
@Component
public class CustomerCodeGenerator {

    private static final int HASH_LENGTH = 8;

    /**
     * Build the customer code for a new owner.
     *
     * @param region             the owner's canonical region
     * @param normalizedTelephone the owner's normalized (E.164) telephone
     * @param lastName           the owner's last name
     * @return the formatted customer code, e.g. {@code "NSW-1A2B3C4D"}
     */
    public String generate(String region, String normalizedTelephone, String lastName) {
        return region + "-" + hash8(normalizedTelephone, lastName);
    }

    /**
     * Build a customer code that does not collide with any already in use. The code is
     * derived exactly as {@link #generate(String, String, String)}; when that value is
     * already taken, {@code '-<n>'} is appended with the smallest {@code n} of two or more
     * that yields an unused code.
     *
     * @param region              the owner's canonical region
     * @param normalizedTelephone the owner's normalized (E.164) telephone
     * @param lastName            the owner's last name
     * @param isTaken             tests whether a candidate code is already in use
     * @return a customer code not reported as taken by {@code isTaken}
     */
    public String generateUnique(String region, String normalizedTelephone, String lastName,
            Predicate<String> isTaken) {
        String base = generate(region, normalizedTelephone, lastName);
        String candidate = base;
        for (int n = 2; isTaken.test(candidate); n++) {
            candidate = base + "-" + n;
        }
        return candidate;
    }

    private String hash8(String normalizedTelephone, String lastName) {
        String identity = safe(normalizedTelephone) + safe(lastName);
        return Sha256.hex(identity).substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }
}
