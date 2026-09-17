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

import org.springframework.samples.petclinic.rest.validation.LocalityResolver;
import org.springframework.samples.petclinic.util.Sha256;
import org.springframework.stereotype.Component;

/**
 * Builds an owner's {@code customerCode}, formatted {@code <REGION>-<HASH8>} where REGION is the
 * canonical region derived from the owner's postcode and HASH8 is the first eight upper-case hex
 * characters of the SHA-256 of the owner's normalized telephone concatenated with the last name
 * (e.g. {@code "NSW-1A2B3C4D"}).
 */
@Component
public class CustomerCodeGenerator {

    /** Number of leading hex characters of the SHA-256 digest kept as the identity hash. */
    private static final int HASH_LENGTH = 8;

    /**
     * Build the customer code for the given postcode, normalized telephone and last name.
     *
     * @param postcode            the owner's postcode; its region forms the leading segment
     * @param normalizedTelephone the owner's telephone in canonical (E.164) form; hashed with the
     *                            last name to form the trailing segment
     * @param lastName            the owner's last name; hashed with the telephone
     * @return the formatted customer code
     */
    public String generate(String postcode, String normalizedTelephone, String lastName) {
        String region = LocalityResolver.resolveFromPostcode(postcode);
        String hash = Sha256.hex(normalizedTelephone + lastName)
            .substring(0, HASH_LENGTH).toUpperCase(Locale.ROOT);
        return region + "-" + hash;
    }

    /**
     * Return a customer code that no existing owner already carries. If {@code customerCode} is free
     * it is returned unchanged; otherwise {@code "-<n>"} is appended using the smallest {@code n} of
     * two or more that yields an unused code.
     *
     * @param customerCode the freshly generated code that may collide with an existing owner's code
     * @param isTaken      predicate reporting whether a candidate code is already in use
     * @return a code guaranteed not to collide with an existing owner's code
     */
    public String deduplicate(String customerCode, Predicate<String> isTaken) {
        if (!isTaken.test(customerCode)) {
            return customerCode;
        }
        for (int n = 2; ; n++) {
            String candidate = customerCode + "-" + n;
            if (!isTaken.test(candidate)) {
                return candidate;
            }
        }
    }
}
